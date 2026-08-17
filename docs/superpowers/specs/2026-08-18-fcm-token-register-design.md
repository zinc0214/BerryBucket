# 로그인 시점 FCM 토큰 서버 등록 설계

## 배경

푸시 알림 수신에 필요한 FCM 토큰을 서버로 전송하는 코드가 없다. 현재 상태:

- `WaverApplication` / `HomeActivity` — 토큰을 조회해 로그만 남긴다.
- `MyFirebaseMessagingService.onNewToken` — `// TODO: 서버로 토큰 전송` 만 있다.

서버가 사용자별 토큰을 모르므로 푸시가 발송되지 않는다.

## 목표

로그인이 완료된 시점과 FCM 토큰이 갱신된 시점에 `/waver/user/fcm-token` 으로 토큰을 전송한다.

## API 스펙

```
POST /waver/user/fcm-token
Content-Type: application/json
Authorization: Bearer <accessToken>

"eyJhbGciOiJIUzI1NiJ9..."
```

바디는 객체로 감싸지 않은 문자열이다. `GsonConverterFactory` 를 쓰고 있으므로 Retrofit
`@Body token: String` 이 위 형태로 직렬화된다. 응답은 다른 단순 API 와 동일하게
`CommonResponse2(success, code, message)` 로 받는다.

인증 헤더는 `TokenInterceptor` 가 DataStore 의 accessToken 을 자동으로 붙이므로 별도 처리가 없다.

**미확정 사항**: 서버가 따옴표 없는 완전 raw 문자열(`text/plain`)을 요구할 가능성이 있다.
실제 요청에서 400 이 나면 `@Body token: RequestBody` 로 전환한다 (WaverApi 한 줄 변경).

## 계층 구조

기존 `LoginByEmail` 경로를 그대로 따른다.

| 계층 | 추가/변경 |
|---|---|
| `data/api/WaverApi.kt` | `@POST("/waver/user/fcm-token") suspend fun updateFcmToken(@Body token: String): CommonResponse2` |
| `domain/repository/LoginRepository.kt` | `suspend fun updateFcmToken(token: String): CommonResponse2` |
| `data/repository/LoginRepositoryImpl.kt` | 위임 구현 |
| `domain/usecases/login/UpdateFcmToken.kt` | 신규 유스케이스 |
| `app/.../util/FcmTokenRegister.kt` | 신규 — 토큰 조회 + 전송 |

`common` 모듈은 건드리지 않는다. 요청 모델이 없고 `CommonResponse2` 는 이미 있다.

## FcmTokenRegister

호출부가 네 곳(로그인 3 + 서비스 1)이라 전송 로직을 한 곳에 모은다.

```kotlin
@Singleton
class FcmTokenRegister @Inject constructor(
    private val updateFcmToken: UpdateFcmToken,
    private val preferenceDataStoreModule: PreferenceDataStoreModule,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun register(token: String? = null) {
        scope.launch {
            runCatching {
                if (preferenceDataStoreModule.loadAccessToken.first().isEmpty()) return@launch
                updateFcmToken(token ?: fetchFcmToken())
            }.onFailure { Log.e("FCM_TOKEN", "전송 실패", it) }
        }
    }
}
```

설계 판단 세 가지:

- **자체 CoroutineScope** — `viewModelScope` 를 쓰면 로그인 직후 화면 전환으로 ViewModel 이
  clear 될 때 요청이 취소된다. 싱글톤 스코프라 전환과 무관하게 완주한다.
- **논-서스펜드 + 예외 삼킴** — FCM 전송 실패가 로그인 흐름을 막거나 지연시켜서는 안 된다.
  푸시는 부가 기능이므로 실패 시 로그만 남기고 조용히 넘어간다.
- **accessToken 비어있으면 조기 반환** — `onNewToken` 은 앱 최초 설치 직후 등 미로그인
  상태에서도 발화한다. 인증 없는 요청을 걸러낸다.

FCM 토큰 조회는 `kotlinx-coroutines-play-services` 의존성을 새로 추가하지 않고
`suspendCancellableCoroutine` 으로 `FirebaseMessaging.getInstance().token` 을 감싼다.

## 호출 지점

네 곳 모두 `fcmTokenRegister.register()` 한 줄이다.

| 위치 | 시점 |
|---|---|
| `LoginViewModel.loadLoginToken` | `setLoginEmailUid` 직후 |
| `JoinEmailViewModel` | `setAccessToken` 직후 |
| `JoinNickNameViewModel.goToLogin` | `setAccessToken` 직후 |
| `MyFirebaseMessagingService.onNewToken` | `register(token)` — 기존 TODO 제거 |

accessToken 저장 이후여야 한다. `TokenInterceptor` 가 DataStore 에서 읽어 헤더를 붙이기 때문이다.

`MyFirebaseMessagingService` 는 주입을 받기 위해 `@AndroidEntryPoint` 가 필요하다.

## 검증

순수 배선 코드라 `common` 모듈의 모델 테스트 같은 검증 대상이 없다. 다음으로 확인한다.

1. `./gradlew assembleDebug` 통과
2. 실기기 로그인 후 네트워크 로그에서 `POST /waver/user/fcm-token` 요청과 응답 확인
3. 미로그인 상태(로그아웃 후 앱 재설치)에서 `onNewToken` 이 발화해도 요청이 나가지 않는지 확인

## 범위 밖

- 재시도 로직 — 실패 시 다음 로그인 또는 토큰 갱신 때 다시 전송된다.
- 로그아웃 시 서버에서 토큰 삭제 — 별도 API 이며 요청 범위에 없다.
- `WaverApplication` / `HomeActivity` 의 기존 토큰 로깅 코드 — 그대로 둔다.
