# 로그인 시점 FCM 토큰 서버 등록 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 로그인 완료 시점과 FCM 토큰 갱신 시점에 `/waver/user/fcm-token` 으로 토큰을 전송해 푸시 알림이 수신되게 한다.

**Architecture:** 기존 `LoginByEmail` 이 따르는 계층(WaverApi → LoginRepository → UseCase)을 그대로 확장하고, app 모듈에 싱글톤 `FcmTokenRegister` 를 두어 "FCM 토큰 조회 + 서버 전송" 을 한 곳에 모은다. 호출부 네 곳은 `fcmTokenRegister.register()` 한 줄만 추가한다. 전송은 fire-and-forget 이라 실패해도 로그인 흐름에 영향을 주지 않는다.

**Tech Stack:** Kotlin, Retrofit2 + GsonConverterFactory, Hilt, DataStore Preferences, Firebase Messaging, Coroutines

## Global Constraints

- 요청: `POST /waver/user/fcm-token`, 바디는 객체로 감싸지 않은 문자열(`@Body token: String` → `"eyJhbGci..."`), 응답은 `CommonResponse2`.
- 인증 헤더는 `TokenInterceptor` 가 자동으로 붙인다. 코드에서 직접 넣지 않는다.
- 새 라이브러리 의존성을 추가하지 않는다. FCM 토큰 조회는 `suspendCancellableCoroutine` 으로 감싼다 (`kotlinx-coroutines-play-services` 는 이 프로젝트에 없다).
- `common` 모듈은 건드리지 않는다. 요청 모델이 없고 `CommonResponse2` 는 이미 존재한다.
- 전송 실패는 절대 로그인/가입 흐름을 막지 않는다. 로그만 남긴다.
- 커밋 메시지는 이 저장소 관례를 따른다: `[영역] 한국어 요약`.

## 테스트 전략

이 저장소에는 `common` 모듈의 모델 단위 테스트만 있고, ViewModel·Repository·Retrofit 배선에 대한
테스트 인프라가 없다 (`kotlinx-coroutines-test` 미포함). 이번 변경은 전부 배선 코드이므로
단위 테스트를 추가하려면 새 테스트 의존성과 디스패처 주입 구조가 필요해 승인된 스펙의 범위를 벗어난다.

따라서 각 태스크의 검증은 **컴파일 통과**이고, 최종 검증은 Task 4 의 **실기기 수동 확인**이다.
이는 스펙의 "검증" 절에서 합의된 방식이다.

## File Structure

| 파일 | 책임 |
|---|---|
| `data/src/main/java/com/zinc/data/api/WaverApi.kt` (수정) | Retrofit 엔드포인트 선언 |
| `domain/src/main/java/com/zinc/domain/repository/LoginRepository.kt` (수정) | 도메인 인터페이스 |
| `data/src/main/java/com/zinc/data/repository/LoginRepositoryImpl.kt` (수정) | API 위임 구현 |
| `domain/src/main/java/com/zinc/domain/usecases/login/UpdateFcmToken.kt` (생성) | 유스케이스 |
| `app/src/main/java/com/zinc/waver/util/FcmTokenRegister.kt` (생성) | FCM 토큰 조회 + 전송 + 실패 흡수 |
| `app/.../login/LoginViewModel.kt` (수정) | 로그인 성공 시 호출 |
| `app/.../login/JoinEmailViewModel.kt` (수정) | 기존 회원 로그인 성공 시 호출 |
| `app/.../login/JoinNickNameViewModel.kt` (수정) | 가입 후 로그인 성공 시 호출 |
| `app/src/main/java/com/zinc/waver/MyFirebaseMessagingService.kt` (수정) | 토큰 갱신 시 호출 |

---

### Task 1: 데이터/도메인 계층

`/waver/user/fcm-token` 을 호출할 수 있는 경로를 만든다. 이 태스크만으로는 아무도 호출하지 않지만,
컴파일이 통과하면 계층 배선이 맞다는 뜻이다.

**Files:**
- Modify: `data/src/main/java/com/zinc/data/api/WaverApi.kt` (74-80번째 줄 `requestLogin` 아래)
- Modify: `domain/src/main/java/com/zinc/domain/repository/LoginRepository.kt`
- Modify: `data/src/main/java/com/zinc/data/repository/LoginRepositoryImpl.kt`
- Create: `domain/src/main/java/com/zinc/domain/usecases/login/UpdateFcmToken.kt`

**Interfaces:**
- Consumes: 없음 (첫 태스크)
- Produces: `class UpdateFcmToken`, `suspend operator fun invoke(token: String): CommonResponse2`

- [ ] **Step 1: WaverApi 에 엔드포인트 추가**

`WaverApi.kt` 의 `requestLogin` 선언 바로 아래에 넣는다. `@Body`, `@POST`, `CommonResponse2` 는
파일 상단에 이미 import 되어 있으므로 import 추가는 필요 없다.

```kotlin
    // FCM 토큰 등록 (푸시 알림 수신용)
    @POST("/waver/user/fcm-token")
    suspend fun updateFcmToken(@Body token: String): CommonResponse2
```

- [ ] **Step 2: LoginRepository 인터페이스에 추가**

`LoginRepository.kt` 의 `requestMyBuryMigration()` 아래에 넣는다. `CommonResponse2` 는 이미 import 되어 있다.

```kotlin
    suspend fun updateFcmToken(token: String): CommonResponse2
```

- [ ] **Step 3: LoginRepositoryImpl 에 구현 추가**

`LoginRepositoryImpl.kt` 의 `requestMyBuryMigration()` 구현 아래에 넣는다. `CommonResponse2` 는 이미 import 되어 있다.

```kotlin
    override suspend fun updateFcmToken(token: String): CommonResponse2 {
        return waverApi.updateFcmToken(token)
    }
```

- [ ] **Step 4: 유스케이스 생성**

`domain/src/main/java/com/zinc/domain/usecases/login/UpdateFcmToken.kt` 를 새로 만든다.
같은 디렉터리의 `LoginByEmail.kt` 와 동일한 형태다.

```kotlin
package com.zinc.domain.usecases.login

import com.zinc.domain.repository.LoginRepository
import javax.inject.Inject

// FCM 토큰 등록
class UpdateFcmToken @Inject constructor(
    private val loginRepository: LoginRepository
) {
    suspend operator fun invoke(token: String) =
        loginRepository.updateFcmToken(token)
}
```

- [ ] **Step 5: 컴파일 확인**

Run: `./gradlew :domain:compileDebugKotlin :data:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

`LoginRepositoryImpl` 이 인터페이스를 다 구현하지 않으면 여기서 실패한다.

- [ ] **Step 6: 커밋**

```bash
git add data/src/main/java/com/zinc/data/api/WaverApi.kt \
        domain/src/main/java/com/zinc/domain/repository/LoginRepository.kt \
        data/src/main/java/com/zinc/data/repository/LoginRepositoryImpl.kt \
        domain/src/main/java/com/zinc/domain/usecases/login/UpdateFcmToken.kt
git commit -m "[알림] FCM 토큰 등록 API 및 유스케이스 추가"
```

---

### Task 2: FcmTokenRegister

FCM 토큰을 조회해 서버로 보내는 싱글톤. 호출부 네 곳이 공유한다.

**Files:**
- Create: `app/src/main/java/com/zinc/waver/util/FcmTokenRegister.kt`

**Interfaces:**
- Consumes: `UpdateFcmToken.invoke(token: String)` (Task 1), `PreferenceDataStoreModule.loadAccessToken: Flow<String>` (기존)
- Produces: `class FcmTokenRegister`, `fun register(token: String? = null)` — 논-서스펜드, 즉시 반환

- [ ] **Step 1: FcmTokenRegister 작성**

`app/src/main/java/com/zinc/waver/util/FcmTokenRegister.kt` 를 새로 만든다.

```kotlin
package com.zinc.waver.util

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.zinc.datastore.login.PreferenceDataStoreModule
import com.zinc.domain.usecases.login.UpdateFcmToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 푸시 알림 수신용 FCM 토큰을 서버에 등록한다.
 *
 * 로그인 직후와 FCM 토큰 갱신 시점에 호출된다.
 * 전송 실패는 삼킨다. 푸시는 부가 기능이므로 로그인 흐름을 막아서는 안 된다.
 */
@Singleton
class FcmTokenRegister @Inject constructor(
    private val updateFcmToken: UpdateFcmToken,
    private val preferenceDataStoreModule: PreferenceDataStoreModule,
) {
    // viewModelScope 를 쓰면 로그인 직후 화면 전환으로 ViewModel 이 clear 될 때 요청이 취소된다.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * @param token 이미 알고 있는 FCM 토큰. null 이면 FirebaseMessaging 에서 조회한다.
     */
    fun register(token: String? = null) {
        scope.launch {
            runCatching {
                // onNewToken 은 미로그인 상태에서도 발화한다. 인증 없는 요청을 걸러낸다.
                if (preferenceDataStoreModule.loadAccessToken.first().isEmpty()) {
                    Log.d(TAG, "미로그인 상태이므로 FCM 토큰을 전송하지 않는다")
                    return@launch
                }
                val res = updateFcmToken(token ?: fetchFcmToken())
                Log.d(TAG, "FCM 토큰 전송 : success=${res.success}, code=${res.code}")
            }.onFailure {
                Log.e(TAG, "FCM 토큰 전송 실패", it)
            }
        }
    }

    private suspend fun fetchFcmToken(): String = suspendCancellableCoroutine { cont ->
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener { cont.resumeWithException(it) }
    }

    companion object {
        private const val TAG = "FCM_TOKEN"
    }
}
```

주의: `return@launch` 는 `runCatching` 이 inline 함수라 동작한다. 성공 경로의 조기 반환이므로
`onFailure` 는 실행되지 않는다.

- [ ] **Step 2: 컴파일 확인**

Run: `./gradlew :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 커밋**

```bash
git add app/src/main/java/com/zinc/waver/util/FcmTokenRegister.kt
git commit -m "[알림] FCM 토큰 등록 담당 FcmTokenRegister 추가"
```

---

### Task 3: 호출 지점 배선

네 곳에서 `FcmTokenRegister` 를 호출한다. 세 ViewModel 은 accessToken 저장 직후,
서비스는 토큰 갱신 시점이다.

**Files:**
- Modify: `app/src/main/java/com/zinc/waver/ui/presentation/login/LoginViewModel.kt`
- Modify: `app/src/main/java/com/zinc/waver/ui/presentation/login/JoinEmailViewModel.kt`
- Modify: `app/src/main/java/com/zinc/waver/ui/presentation/login/JoinNickNameViewModel.kt`
- Modify: `app/src/main/java/com/zinc/waver/MyFirebaseMessagingService.kt`

**Interfaces:**
- Consumes: `FcmTokenRegister.register(token: String? = null)` (Task 2)
- Produces: 없음 (마지막 배선 태스크)

- [ ] **Step 1: LoginViewModel 배선**

import 추가:

```kotlin
import com.zinc.waver.util.FcmTokenRegister
```

생성자에 파라미터 추가:

```kotlin
class LoginViewModel @Inject constructor(
    private val loginByEmail: LoginByEmail,
    private val preferenceDataStoreModule: PreferenceDataStoreModule,
    private val fcmTokenRegister: FcmTokenRegister,
) : CommonViewModel() {
```

`loadLoginToken` 의 `setLoginEmailUid(emailUid)` 와 `_goToMain.value = true` 사이에 한 줄 추가:

```kotlin
                preferenceDataStoreModule.setAccessToken("Bearer $data")
                preferenceDataStoreModule.setLoginEmailUid(emailUid)
                // accessToken 저장 이후여야 한다. TokenInterceptor 가 DataStore 에서 읽어 헤더를 붙인다.
                fcmTokenRegister.register()
                _goToMain.value = true
```

- [ ] **Step 2: JoinEmailViewModel 배선**

import 추가:

```kotlin
import com.zinc.waver.util.FcmTokenRegister
```

생성자에 파라미터 추가:

```kotlin
class JoinEmailViewModel @Inject constructor(
    private val loginByEmail: LoginByEmail,
    private val createProfile: CreateProfile,
    private val checkUserState: CheckUserStatus,
    private val preferenceDataStoreModule: PreferenceDataStoreModule,
    private val fcmTokenRegister: FcmTokenRegister,
) : CommonViewModel() {
```

`goToLogin` 함수의 `if (res.success)` 블록 안, `_isAlreadyUsedEmail.value = true` 앞에 추가:

```kotlin
            if (res.success) {
                res.data.accessToken.let { token ->
                    preferenceDataStoreModule.setAccessToken("Bearer $token")
                }
                // accessToken 저장 이후여야 한다. TokenInterceptor 가 DataStore 에서 읽어 헤더를 붙인다.
                fcmTokenRegister.register()
                _isAlreadyUsedEmail.value = true
```

- [ ] **Step 3: JoinNickNameViewModel 배선**

import 추가:

```kotlin
import com.zinc.waver.util.FcmTokenRegister
```

생성자에 파라미터 추가:

```kotlin
class JoinNickNameViewModel @Inject constructor(
    private val createProfile: CreateProfile,
    private val loginByEmail: LoginByEmail,
    private val checkAlreadyUsedNickname: CheckAlreadyUsedNickname,
    private val preferenceDataStoreModule: PreferenceDataStoreModule,
    private val fcmTokenRegister: FcmTokenRegister,
) : CommonViewModel() {
```

`goToLogin` 함수의 `setAccessToken` 블록 바로 다음 줄에 추가한다. 이 파일에는 기존
`// accessToken 저장 이후에 발화해야 한다` 주석과 `[TEST]` 로그가 있으니 지우지 말고 그 앞에 넣는다:

```kotlin
                res.data.accessToken.let { token ->
                    preferenceDataStoreModule.setAccessToken("Bearer $token")
                }
                fcmTokenRegister.register()
```

- [ ] **Step 4: MyFirebaseMessagingService 배선**

import 추가:

```kotlin
import com.zinc.waver.util.FcmTokenRegister
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
```

클래스 선언에 `@AndroidEntryPoint` 를 붙이고 필드 주입을 추가한 뒤 `onNewToken` 의 TODO 를 대체한다:

```kotlin
@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var fcmTokenRegister: FcmTokenRegister

    companion object {
        private const val CHANNEL_ID = "waver_notification_channel"
        private const val CHANNEL_NAME = "Waver Notifications"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_TOKEN", "Refreshed token: $token")
        fcmTokenRegister.register(token)
    }
```

`AndroidManifest.xml` 은 수정하지 않는다. 서비스 선언은 이미 있고 `@AndroidEntryPoint` 는
매니페스트 변경을 요구하지 않는다. `WaverApplication` 에 `@HiltAndroidApp` 이 이미 붙어 있다.

- [ ] **Step 5: 전체 빌드 확인**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

Hilt 주입 그래프가 깨지면 여기서 실패한다.

- [ ] **Step 6: 커밋**

```bash
git add app/src/main/java/com/zinc/waver/ui/presentation/login/LoginViewModel.kt \
        app/src/main/java/com/zinc/waver/ui/presentation/login/JoinEmailViewModel.kt \
        app/src/main/java/com/zinc/waver/ui/presentation/login/JoinNickNameViewModel.kt \
        app/src/main/java/com/zinc/waver/MyFirebaseMessagingService.kt
git commit -m "[알림] 로그인 및 토큰 갱신 시점에 FCM 토큰 서버 전송"
```

---

### Task 4: 실기기 검증

빌드만으로는 요청 형식(문자열 바디)이 서버 스펙과 맞는지 알 수 없다. 실제 요청을 확인한다.

**Files:** 없음 (검증만)

**Interfaces:**
- Consumes: Task 3 까지의 전체 동작
- Produces: 없음

- [ ] **Step 1: 앱 설치 후 로그인**

Run: `./gradlew installDebug`
그리고 실기기에서 로그인한다.

- [ ] **Step 2: 요청 로그 확인**

Run: `adb logcat -s FCM_TOKEN okhttp.OkHttpClient`

기대하는 것:
- `FCM 토큰 전송 : success=true, code=...` 로그
- `POST /waver/user/fcm-token` 요청, 바디는 따옴표로 감싼 토큰 문자열

- [ ] **Step 3: 400/415 응답이면 raw 바디로 전환**

서버가 따옴표 없는 완전 raw 문자열을 요구하는 경우에만 수행한다. `WaverApi.kt` 를 아래로 바꾼다:

```kotlin
    // FCM 토큰 등록 (푸시 알림 수신용)
    @POST("/waver/user/fcm-token")
    suspend fun updateFcmToken(@Body token: RequestBody): CommonResponse2
```

import 추가: `import okhttp3.RequestBody`

그리고 `LoginRepositoryImpl.kt` 의 구현을 바꾼다:

```kotlin
    override suspend fun updateFcmToken(token: String): CommonResponse2 {
        return waverApi.updateFcmToken(token.toRequestBody("text/plain".toMediaType()))
    }
```

import 추가:

```kotlin
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
```

`LoginRepository` 인터페이스와 그 위 계층은 그대로다. 변경 후 Step 1-2 를 다시 수행한다.

- [ ] **Step 4: 미로그인 상태 확인**

앱을 삭제 후 재설치하고 로그인하지 않은 채 `adb logcat -s FCM_TOKEN` 을 본다.

기대: `미로그인 상태이므로 FCM 토큰을 전송하지 않는다` 로그가 보이고,
`POST /waver/user/fcm-token` 요청은 나가지 않는다.

- [ ] **Step 5: Step 3 을 수행했다면 커밋**

```bash
git add data/src/main/java/com/zinc/data/api/WaverApi.kt \
        data/src/main/java/com/zinc/data/repository/LoginRepositoryImpl.kt
git commit -m "[알림] FCM 토큰 전송 바디를 text/plain 으로 변경"
```
