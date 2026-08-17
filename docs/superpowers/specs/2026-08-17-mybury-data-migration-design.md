---
name: mybury-data-migration-design
description: 회원가입 시 join API의 myburyYn 값에 따라 마이버리 데이터 연결 화면을 노출하고 이관을 요청하는 기능
metadata:
  type: design
  date: 2026-08-17
  revision: 1
---

# 마이버리 데이터 이관 연결 화면 설계

## 목표

마이버리(이전 서비스) 기존 회원이 웨이버에 신규 가입할 때, 마이버리에 쌓아둔 버킷리스트 데이터를 웨이버로 옮길 수 있게 한다.

- `/waver/user/join` 응답으로 내려오는 `myburyYn`이 `"Y"`이면 데이터 연결 화면을 노출한다.
- 사용자가 "웨이버에서 이어서 진행하기"를 선택하면 `/waver/user/migration`으로 이관을 요청한다.
- 이관은 서버 스케줄러가 비동기로 순차 처리하므로, 앱은 요청 접수 결과만 안내한다.

## 현재 상태

가입 흐름은 `app` 모듈의 `com.zinc.waver.ui.presentation.login` 패키지에 있다.

```
JoinScreen
├── JoinEmailScreen        (구글 이메일 확인)
├── JoinCreateProfile1     (닉네임 + 프로필 이미지)
└── JoinCreateProfile2     (한 줄 소개) → JoinNickNameViewModel.join()
                                            ├── CreateProfile   → POST /waver/user/join
                                            └── LoginByEmail    → POST /waver/login (accessToken 저장)
                                                  ↓ goToLogin = true
                                            WelcomePopupScreen → 메인
```

`JoinResponse.data`는 `Any?`로 선언되어 있어 응답 본문의 어떤 필드도 읽을 수 없다. 소비처가 한 곳도 없으므로(`grep JoinResponse` 결과 선언·시그니처뿐) 타입을 바꿔도 기존 동작에 영향이 없다.

## 1. API / 모델 계층

프로젝트는 Retrofit + Gson을 쓰고 `@SerializedName`을 전혀 사용하지 않는다. 프로퍼티명이 곧 JSON 키이므로 이름을 정확히 맞춘다.

### `common/src/main/java/com/zinc/common/models/Login.kt`

```kotlin
data class JoinResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: JoinData?
) : Serializable

data class JoinData(
    val myburyYn: YesOrNo?
) : Serializable {
    fun isMyBuryUser() = myburyYn?.isYes() == true
}
```

응답 형태:

```json
{
  "success": true,
  "code": "...",
  "message": "...",
  "data": { "myburyYn": "Y" }
}
```

기존 `YesOrNo` enum(`Y`/`N`, `isYes()`/`isNo()`)을 재사용한다. 필드가 누락되거나 알 수 없는 값이면 Gson이 `null`을 채우고 `isMyBuryUser()`가 `false`가 되어, 마이버리 회원이 아닌 사용자의 흐름은 기존과 동일하게 유지된다.

### `data/src/main/java/com/zinc/data/api/WaverApi.kt`

```kotlin
// 마이버리 데이터 이관 요청
@POST("/waver/user/migration")
suspend fun requestMyBuryMigration(): CommonResponse2
```

응답에 `data`가 없으므로 `data` 필드가 없는 `CommonResponse2`(success/code/message)를 사용한다. 인증은 기존 OkHttp 인터셉터가 붙이는 `Authorization` 헤더에 의존한다.

### 도메인 계층

`CreateProfile` 유즈케이스와 같은 패턴으로 얇게 추가한다.

- `LoginRepository.requestMyBuryMigration(): CommonResponse2`
- `LoginRepositoryImpl` — `waverApi.requestMyBuryMigration()` 위임
- `domain/usecases/login/RequestMyBuryMigration.kt` — `operator fun invoke()`

## 2. 이관 응답 코드 처리

| 코드 | 서버 의미 | 앱 동작 |
|---|---|---|
| `success == true` | 이관 요청 접수 (스케줄러가 순차 처리) | 팝업 "데이터 이관을 진행합니다." |
| `8201` | 이미 이관 완료 | 팝업 "이미 데이터 이관이 완료되었습니다." |
| `8202` | 이미 요청됨 | 팝업 "이미 데이터 이관 진행중입니다." |
| `8200` | 마이버리 회원이 아님 | 팝업 없이 다음 단계로 진행 |
| 그 외 코드 / 네트워크 오류 | — | 팝업 없이 다음 단계로 진행 |

팝업은 기존 `CommonDialogView`(확인 버튼 1개)를 사용한다. 확인을 누르면 데이터 연결 화면을 닫고 환영 팝업으로 넘어간다.

이관이 비동기이므로 앱은 최종 결과를 알 수 없고, 실패해도 가입 자체는 이미 완료된 상태다. 따라서 어떤 경우에도 가입 흐름을 막지 않는다. `8200`은 `myburyYn == "Y"`일 때만 이 화면이 뜨므로 정상적으로는 발생하지 않는 방어 케이스다.

## 3. 화면: `MyBuryConnectScreen`

위치: `app/src/main/java/com/zinc/waver/ui/presentation/login/MyBuryConnectScreen.kt`

`Dialog`가 아닌 **풀스크린 Composable**로 만든다. 디자인이 상태바까지 덮는 전면 화면이고, 기존 `WelcomePopupScreen`(320dp `Dialog`)과 성격이 다르다.

### 레이아웃

```
┌─────────────────────────────┐
│ ✕                           │  btn_40_close, 흰색 tint, statusBarsPadding
│                             │
│   [mybury] ∿ [waver]        │  Row: mybury_logo · mybury_to_waver · ic_launcher
│                             │
│      버킷리스트              │  Gray1, Bold, 24dp, center
│   이제 함께 즐기세요          │
│                             │  배경: bg_membership_login (ContentScale.Crop)
├─────────────────────────────┤
│  버킷리스트는 웨이버에서 계속돼요  │  Gray10, Bold, 18dp
│                             │
│  마이버리 데이터(버킷리스트)를    │  Main4, Bold, 15dp
│  웨이버에 옮길 수 있습니다.      │
│                             │
│  이후 마이버리에 작성한 내용은    │  Gray6, 14dp
│  저장되지 않아요. 버킷리스트는   │
│  웨이버에서 이어서 작성해 주세요. │
│                             │
│ ┌─────────────────────────┐ │
│ │ 웨이버에서 이어서 진행하기 │ │  Main4 배경, Gray1 텍스트, Rounded 8dp
│ └─────────────────────────┘ │
│ ┌─────────────────────────┐ │
│ │      새롭게 시작하기      │ │  흰 배경, Gray3 보더, Gray10 텍스트
│ └─────────────────────────┘ │
└─────────────────────────────┘   navigationBarsPadding
```

### 에셋

모두 기존 리소스를 사용한다. 새 에셋은 추가하지 않는다.

| 용도 | 리소스 |
|---|---|
| 상단 그라데이션 배경 | `CommonR.drawable.bg_membership_login` |
| 좌측 마이버리 로고 | `CommonR.drawable.mybury_logo` |
| 두 로고를 잇는 물결 | `CommonR.drawable.mybury_to_waver` |
| 우측 웨이버 로고 | `R.mipmap.ic_launcher` (앱 런처 아이콘, 디자인과 동일) |
| 닫기 버튼 | `CommonR.drawable.btn_40_close` |

### 인터페이스

```kotlin
@Composable
fun MyBuryConnectScreen(
    onFinished: () -> Unit,   // 다음 단계(환영 팝업)로 진행
)
```

콜백이 하나뿐인 이유: 세 갈래 분기(이관 / 새로 시작 / 닫기)가 모두 같은 곳으로 수렴하고, 이관 요청과 결과 팝업은 화면 내부에서 끝난다. 호출자인 `JoinScreen`은 "이 화면이 끝났다"만 알면 된다.

내부 동작:

| 조작 | 동작 |
|---|---|
| ✕ 닫기 / 새롭게 시작하기 | 즉시 `onFinished()` |
| 웨이버에서 이어서 진행하기 | 이관 요청 → 팝업 대상 코드면 팝업 노출 후 확인 시 `onFinished()`, 팝업 없는 코드면 즉시 `onFinished()` |

이관 요청과 응답 코드 매핑은 `MyBuryConnectScreen` 내부에서 `hiltViewModel()`로 얻는 `MyBuryConnectViewModel`이 담당한다. 요청 중에는 버튼 중복 클릭을 막는다.

문자열 리소스(화면 텍스트 6개 + 팝업 메시지 3개)는 `app/src/main/res/values/strings.xml`에 추가하고, 프로젝트 관례대로 `@Preview`를 붙인다.

## 4. 가입 흐름 연결

### `JoinNickNameViewModel`

`createNewProfile()`에서 `res.data?.isMyBuryUser()` 결과를 보관하고, `goToLogin` 신호와 함께 화면에 전달한다. `_goToLogin: SingleLiveEvent<Boolean>`은 그대로 두고 별도 `LiveData<Boolean>`으로 노출하여, accessToken 저장이 끝난 뒤 화면이 값을 읽도록 한다.

### `JoinCreateProfile2`

`goToMain: () -> Unit` → `goToMain: (isMyBuryUser: Boolean) -> Unit`으로 시그니처를 변경한다.

### `JoinScreen`

```
join 성공 → loginByEmail → accessToken 저장
  │
  ├ myburyYn = Y → [MyBuryConnectScreen]
  │                   ├ 이어서 진행하기 → POST /waver/user/migration
  │                   │                     └ 결과 팝업 → 확인 ─┐
  │                   ├ 새롭게 시작하기 ─────────────────────┤
  │                   └ ✕ 닫기 ─────────────────────────────┤
  │                                                        ↓
  └ myburyYn = N ────────────────────────→ [환영 팝업] → 메인
```

**순서 제약**: 이관 API는 인증이 필요하다. 이 화면은 `goToLogin()`에서 accessToken을 DataStore에 저장한 *이후*에 노출되므로 토큰이 이미 준비된 상태다. 화면 노출 시점을 join 성공 직후로 앞당기면 401이 발생하므로 이 순서를 유지해야 한다.

## 5. 검증

- `./gradlew :app:assembleDebug` 빌드 확인
- `common` 모듈에 Gson 파싱 유닛테스트 추가 (`UserLimitInfoTest`와 같은 위치)
  - `data.myburyYn = "Y"` → `isMyBuryUser() == true`
  - `data.myburyYn = "N"` → `false`
  - `data` 누락 / `myburyYn` 누락 / 알 수 없는 값 → `false`
- 이관 응답 코드 → 팝업 메시지 매핑 유닛테스트
- `MyBuryConnectScreen` `@Preview` 렌더링 확인

## 범위에서 제외

- 이관 진행 상황 조회/표시 (서버 스케줄러가 비동기 처리, 조회 API 없음)
- 가입 이후 마이페이지 등에서 이관을 다시 요청하는 진입점
- 마이버리 앱 쪽 변경
