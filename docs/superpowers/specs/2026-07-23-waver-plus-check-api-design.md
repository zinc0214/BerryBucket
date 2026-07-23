# 웨이버플러스 구독 체크: DataStore → API 전환 설계

## 배경

현재 웨이버플러스 구독 여부는 클라이언트 DataStore(`hasWaverPlus` boolean)에 저장해두고 각 화면이 이를 구독하는 방식이다. 이를 제거하고, 구독 여부가 필요한 모든 시점(로그인 포함)에 `/waver/user/check/limit` API를 호출해 서버 기준으로 판단하도록 변경한다.

## API

`GET /waver/user/check/limit`

```json
{
  "success": true,
  "code": "2000",
  "message": "SUCCESS",
  "data": {
    "premiumStatus": "EXPIRED",
    "imageLimit": 1,
    "imageUsed": 0,
    "togetherLimit": 3,
    "togetherUsed": 0
  }
}
```

- `premiumStatus`: NONE / ACTIVE / EXPIRED
- `imageLimit` / `imageUsed`: 이미지 첨부 무료 제공/사용 횟수
- `togetherLimit` / `togetherUsed`: 함께하기(친구 태그) 무료 제공/사용 횟수

## 판단 규칙 (사용자 확정)

- **기능 사용 가능 여부**: `ACTIVE`이거나, NONE/EXPIRED여도 무료 횟수가 남아있으면 사용 가능
  - `canUseImage = isPremium || imageUsed < imageLimit`
  - `canUseTogether = isPremium || togetherUsed < togetherLimit`
  - 횟수를 소진한 경우에만 웨이버플러스 안내(가입 유도) 노출
- **더보기 화면 뱃지/배너**: `premiumStatus == ACTIVE`일 때만 Plus 뱃지 표시, NONE/EXPIRED는 가입 유도 배너 표시 (무료 횟수와 무관)
- **캐싱 없음**: 매 화면 진입/체크 시점마다 API 호출 (사용 횟수가 실시간으로 변하므로)

## 변경 사항

### 1. 데이터 레이어

- `common` 모델: `CheckUserLimitResponse` + `UserLimitInfo` 추가
  - `UserLimitInfo`에 `isPremium`, `canUseImage`, `canUseTogether` 헬퍼 제공
  - `PremiumStatus`는 기존 `ProfileInfo.PremiumStatus`(NONE/ACTIVE/EXPIRED)를 재사용
- `WaverApi`: `GET /waver/user/check/limit` 추가
- `MoreRepository` / `MoreRepositoryImpl`: `checkUserLimit()` 추가
- 유즈케이스: `domain/usecases/more/CheckUserLimit` 추가

### 2. 소비처

| 위치 | 변경 |
|---|---|
| `HomeViewModel` | `loadProfileInfo()`의 DataStore 저장 제거. `updateWaverPlus()` 삭제. `notifySubscriptionStarted()`의 `setHasWaverPlus(true)` 제거 (구독 시작 알림 API는 유지) |
| `HomeActivity` | `updateWaverPlus(false)` 호출 제거. 로그인/앱 시작 시 체크는 API 호출 기반으로 유지 |
| `MoreViewModel` | `checkHasWaverPlus()`를 DataStore 구독 → `CheckUserLimit` API 호출로 변경. `hasWaverPlus = premiumStatus == ACTIVE` |
| `WriteBucketListViewModel` | `checkHasWaverPlus()`를 API 호출로 변경, `UserLimitInfo` 기반 상태 노출 |
| `WriteScreen1` (이미지) | `canUseImage`면 이미지 추가 가능, 아니면 웨이버플러스 안내. `AddImageItem` 뱃지도 `canUseImage` 기준 (사용 가능하면 뱃지 숨김) |
| `WriteScreen2` (친구 태그) | `canUseTogether`면 `EnableType.Enable`, 아니면 `NoWaverPlus` |
| `PreferenceDataStoreModule` | `waverPlusKey`, `loadHasWaverPlus`, `setHasWaverPlus` 삭제 |

### 3. 에러 처리

API 실패 시 기존 `ceh` 패턴을 따라 비구독자(사용 불가) 취급 — 기존 DataStore 기본값(`?: false`)과 동일한 폴백.

## 테스트

- 각 소비처 화면에서 상태별(ACTIVE / 횟수 남은 NONE·EXPIRED / 횟수 소진) UI 동작 확인
- 빌드 및 기존 테스트 통과 확인
