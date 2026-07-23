# 웨이버플러스 구독 체크 API 전환 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 웨이버플러스 구독 여부 판단을 DataStore 저장 방식에서 `/waver/user/check/limit` API 호출 방식으로 전환한다.

**Architecture:** `GET /waver/user/check/limit` 응답(`premiumStatus`, `imageLimit/imageUsed`, `togetherLimit/togetherUsed`)을 `UserLimitInfo` 공용 모델로 받고, 판단 로직(`isPremium`, `canUseImage`, `canUseTogether`)을 모델 헬퍼로 제공한다. 구독 여부가 필요한 3개 소비처(Home 로그인 시점, More 화면, Write 화면)가 각각 `CheckUserLimit` 유즈케이스를 호출하며, DataStore의 `hasWaverPlus` 관련 코드는 전부 삭제한다.

**Tech Stack:** Kotlin, Retrofit, Hilt, Coroutines/LiveData, JUnit4

**Spec:** `docs/superpowers/specs/2026-07-23-waver-plus-check-api-design.md`

## Global Constraints

- 판단 규칙: `canUseImage = isPremium || imageUsed < imageLimit`, `canUseTogether = isPremium || togetherUsed < togetherLimit`, `isPremium = premiumStatus == ACTIVE`
- 더보기(More) 화면 뱃지/배너 기준은 `premiumStatus == ACTIVE`만 사용 (무료 횟수 무관)
- API 실패 시 비구독·사용불가 취급 (기존 DataStore 기본값 `false`와 동일한 폴백)
- 캐싱 없음: 매 체크 시점마다 API 호출
- `PremiumStatus`는 기존 `ProfileInfo.PremiumStatus`(NONE/ACTIVE/EXPIRED, `common/src/main/java/com/zinc/common/models/Profile.kt:35`) 재사용
- 커밋 메시지는 기존 저장소 스타일(한국어, `[웨이버플러스] ...`)을 따른다

---

### Task 1: 공용 모델 `UserLimitInfo` + 단위 테스트

**Files:**
- Modify: `common/build.gradle.kts` (dependencies 블록)
- Create: `common/src/main/java/com/zinc/common/models/CheckUserLimit.kt`
- Test: `common/src/test/java/com/zinc/common/models/UserLimitInfoTest.kt`

**Interfaces:**
- Consumes: `ProfileInfo.PremiumStatus` (기존, `com.zinc.common.models.Profile.kt`)
- Produces:
  - `CheckUserLimitResponse(data: UserLimitInfo, success: Boolean, code: String, message: String)`
  - `UserLimitInfo(premiumStatus: ProfileInfo.PremiumStatus, imageLimit: Int, imageUsed: Int, togetherLimit: Int, togetherUsed: Int)`
    - `val isPremium: Boolean`, `val canUseImage: Boolean`, `val canUseTogether: Boolean`
    - `companion object { val NONE: UserLimitInfo }` — API 실패 폴백용 (전부 사용 불가)

- [ ] **Step 1: common 모듈에 junit 테스트 의존성 추가**

`common/build.gradle.kts`의 dependencies 블록 마지막에 추가:

```kotlin
    // test
    testImplementation(libs.test.junit)
```

- [ ] **Step 2: 실패하는 테스트 작성**

`common/src/test/java/com/zinc/common/models/UserLimitInfoTest.kt` 생성:

```kotlin
package com.zinc.common.models

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserLimitInfoTest {

    private fun info(
        status: ProfileInfo.PremiumStatus,
        imageLimit: Int = 1,
        imageUsed: Int = 0,
        togetherLimit: Int = 3,
        togetherUsed: Int = 0
    ) = UserLimitInfo(
        premiumStatus = status,
        imageLimit = imageLimit,
        imageUsed = imageUsed,
        togetherLimit = togetherLimit,
        togetherUsed = togetherUsed
    )

    @Test
    fun `ACTIVE 구독자는 횟수와 무관하게 모두 사용 가능`() {
        val limit = info(
            ProfileInfo.PremiumStatus.ACTIVE,
            imageLimit = 1, imageUsed = 1,
            togetherLimit = 3, togetherUsed = 3
        )
        assertTrue(limit.isPremium)
        assertTrue(limit.canUseImage)
        assertTrue(limit.canUseTogether)
    }

    @Test
    fun `NONE 이어도 무료 횟수가 남아있으면 사용 가능`() {
        val limit = info(ProfileInfo.PremiumStatus.NONE, imageUsed = 0, togetherUsed = 2)
        assertFalse(limit.isPremium)
        assertTrue(limit.canUseImage)
        assertTrue(limit.canUseTogether)
    }

    @Test
    fun `EXPIRED 이고 무료 횟수를 소진하면 사용 불가`() {
        val limit = info(
            ProfileInfo.PremiumStatus.EXPIRED,
            imageLimit = 1, imageUsed = 1,
            togetherLimit = 3, togetherUsed = 3
        )
        assertFalse(limit.canUseImage)
        assertFalse(limit.canUseTogether)
    }

    @Test
    fun `이미지만 소진한 경우 이미지는 불가, 함께하기는 가능`() {
        val limit = info(
            ProfileInfo.PremiumStatus.NONE,
            imageLimit = 1, imageUsed = 1,
            togetherLimit = 3, togetherUsed = 0
        )
        assertFalse(limit.canUseImage)
        assertTrue(limit.canUseTogether)
    }

    @Test
    fun `NONE 폴백은 모두 사용 불가`() {
        assertFalse(UserLimitInfo.NONE.isPremium)
        assertFalse(UserLimitInfo.NONE.canUseImage)
        assertFalse(UserLimitInfo.NONE.canUseTogether)
    }
}
```

- [ ] **Step 3: 테스트 실패 확인**

Run: `./gradlew :common:testDebugUnitTest --tests "com.zinc.common.models.UserLimitInfoTest"`
Expected: FAIL — `Unresolved reference: UserLimitInfo` 컴파일 에러

- [ ] **Step 4: 모델 구현**

`common/src/main/java/com/zinc/common/models/CheckUserLimit.kt` 생성:

```kotlin
package com.zinc.common.models

import kotlinx.serialization.Serializable

@Serializable
data class CheckUserLimitResponse(
    val data: UserLimitInfo,
    val success: Boolean,
    val code: String,
    val message: String
)

@Serializable
data class UserLimitInfo(
    val premiumStatus: ProfileInfo.PremiumStatus,
    val imageLimit: Int,
    val imageUsed: Int,
    val togetherLimit: Int,
    val togetherUsed: Int
) {
    val isPremium: Boolean get() = premiumStatus == ProfileInfo.PremiumStatus.ACTIVE
    val canUseImage: Boolean get() = isPremium || imageUsed < imageLimit
    val canUseTogether: Boolean get() = isPremium || togetherUsed < togetherLimit

    companion object {
        // API 실패 등 확인 불가 시 사용하는 폴백 (모두 사용 불가)
        val NONE = UserLimitInfo(
            premiumStatus = ProfileInfo.PremiumStatus.NONE,
            imageLimit = 0,
            imageUsed = 0,
            togetherLimit = 0,
            togetherUsed = 0
        )
    }
}
```

- [ ] **Step 5: 테스트 통과 확인**

Run: `./gradlew :common:testDebugUnitTest --tests "com.zinc.common.models.UserLimitInfoTest"`
Expected: PASS (5 tests)

- [ ] **Step 6: 커밋**

```bash
git add common/build.gradle.kts common/src/main/java/com/zinc/common/models/CheckUserLimit.kt common/src/test/java/com/zinc/common/models/UserLimitInfoTest.kt
git commit -m "[웨이버플러스] 구독/무료혜택 한도 모델 UserLimitInfo 추가"
```

---

### Task 2: API·Repository·유즈케이스 추가

**Files:**
- Modify: `data/src/main/java/com/zinc/data/api/WaverApi.kt:334-340` (startSubscription 아래)
- Modify: `domain/src/main/java/com/zinc/domain/repository/MoreRepository.kt`
- Modify: `data/src/main/java/com/zinc/data/repository/MoreRepositoryImpl.kt`
- Create: `domain/src/main/java/com/zinc/domain/usecases/more/CheckUserLimit.kt`

**Interfaces:**
- Consumes: `CheckUserLimitResponse` (Task 1)
- Produces: `CheckUserLimit` 유즈케이스 — `suspend operator fun invoke(): CheckUserLimitResponse`

- [ ] **Step 1: WaverApi에 엔드포인트 추가**

`WaverApi.kt`의 `startSubscription` 선언 아래(인터페이스 닫는 `}` 앞)에 추가:

```kotlin
    // 웨이버 플러스 구독 상태 / 무료 혜택 한도 확인
    @GET("/waver/user/check/limit")
    suspend fun checkUserLimit(): CheckUserLimitResponse
```

import 추가: `import com.zinc.common.models.CheckUserLimitResponse` (기존 import 블록의 알파벳 순서에 맞춰 삽입)

- [ ] **Step 2: MoreRepository 인터페이스에 메서드 추가**

`MoreRepository.kt`의 `startSubscription` 아래에 추가:

```kotlin
    suspend fun checkUserLimit(): CheckUserLimitResponse
```

import 추가: `import com.zinc.common.models.CheckUserLimitResponse`

- [ ] **Step 3: MoreRepositoryImpl 구현 추가**

`MoreRepositoryImpl.kt`의 `startSubscription` 구현 아래에 추가:

```kotlin
    override suspend fun checkUserLimit(): CheckUserLimitResponse {
        return waverApi.checkUserLimit()
    }
```

import 추가: `import com.zinc.common.models.CheckUserLimitResponse`

- [ ] **Step 4: CheckUserLimit 유즈케이스 생성**

`domain/src/main/java/com/zinc/domain/usecases/more/CheckUserLimit.kt` 생성 (기존 `StartSubscription.kt` 패턴):

```kotlin
package com.zinc.domain.usecases.more

import com.zinc.domain.repository.MoreRepository
import javax.inject.Inject

class CheckUserLimit @Inject constructor(
    private val moreRepository: MoreRepository
) {
    suspend operator fun invoke() = moreRepository.checkUserLimit()
}
```

- [ ] **Step 5: 컴파일 확인**

Run: `./gradlew :domain:compileDebugKotlin :data:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: 커밋**

```bash
git add data/src/main/java/com/zinc/data/api/WaverApi.kt domain/src/main/java/com/zinc/domain/repository/MoreRepository.kt data/src/main/java/com/zinc/data/repository/MoreRepositoryImpl.kt domain/src/main/java/com/zinc/domain/usecases/more/CheckUserLimit.kt
git commit -m "[웨이버플러스] /waver/user/check/limit API 연결 및 CheckUserLimit 유즈케이스 추가"
```

---

### Task 3: MoreViewModel — DataStore 구독 → API 호출

**Files:**
- Modify: `feature/ui-more/src/main/java/com/zinc/waver/ui_more/viewModel/MoreViewModel.kt:99-105`

**Interfaces:**
- Consumes: `CheckUserLimit` 유즈케이스 (Task 2)
- Produces: `MoreViewModel.checkHasWaverPlus()` — 기존 시그니처 유지, `hasWaverPlus: LiveData<Boolean>`의 의미가 `premiumStatus == ACTIVE`로 변경 (화면 코드 변경 없음)

- [ ] **Step 1: 생성자에 유즈케이스 주입 추가**

`MoreViewModel.kt` 생성자에 `checkUserLimit` 추가 (`preferenceDataStoreModule`은 `loadLoginEmail`에서 계속 사용하므로 유지):

```kotlin
@HiltViewModel
class MoreViewModel @Inject constructor(
    private val loadProfileInfo: LoadProfileInfo,
    private val updateProfileInfo: UpdateProfileInfo,
    private val checkAlreadyUsedNickname: CheckAlreadyUsedNickname,
    private val checkUserLimit: CheckUserLimit,
    private val preferenceDataStoreModule: PreferenceDataStoreModule,
) : CommonViewModel() {
```

import 추가:

```kotlin
import com.zinc.common.models.ProfileInfo
import com.zinc.domain.usecases.more.CheckUserLimit
```

- [ ] **Step 2: checkHasWaverPlus()를 API 호출로 교체**

기존 (99-105행):

```kotlin
    fun checkHasWaverPlus() {
        viewModelScope.launch {
            preferenceDataStoreModule.loadHasWaverPlus.collectLatest { value ->
                _hasWaverPlus.value = value
            }
        }
    }
```

변경 후:

```kotlin
    fun checkHasWaverPlus() {
        viewModelScope.launch(ceh(_hasWaverPlus, false)) {
            val response = checkUserLimit.invoke()
            _hasWaverPlus.value =
                response.success && response.data.premiumStatus == ProfileInfo.PremiumStatus.ACTIVE
        }
    }
```

(`collectLatest` import는 `loadLoginEmail`에서 계속 사용하므로 유지)

- [ ] **Step 3: 컴파일 확인**

Run: `./gradlew :feature:ui-more:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: 커밋**

```bash
git add feature/ui-more/src/main/java/com/zinc/waver/ui_more/viewModel/MoreViewModel.kt
git commit -m "[더보기] 웨이버플러스 뱃지 판단을 DataStore에서 check/limit API로 변경"
```

---

### Task 4: Write 플로우 — 이미지/함께하기 무료 횟수 반영

**Files:**
- Modify: `feature/ui-write/src/main/java/com/zinc/waver/ui_write/viewmodel/WriteBucketListViewModel.kt`
- Modify: `feature/ui-write/src/main/java/com/zinc/waver/ui_write/presentation/WriteScreen1.kt`
- Modify: `feature/ui-write/src/main/java/com/zinc/waver/ui_write/presentation/WriteScreen2.kt`
- Modify: `feature/ui-write/src/main/java/com/zinc/waver/ui_write/presentation/options/ImageScreen.kt:87-130`
- Modify: `feature/ui-common/src/main/java/com/zinc/waver/model/Write.kt:158-161`

**Interfaces:**
- Consumes: `CheckUserLimit` 유즈케이스, `UserLimitInfo`(Task 1: `canUseImage`, `canUseTogether`, `UserLimitInfo.NONE`)
- Produces:
  - `WriteBucketListViewModel.userLimitInfo: LiveData<UserLimitInfo>`, `WriteBucketListViewModel.checkUserLimit()` (기존 `hasWaverPlus`/`checkHasWaverPlus()` 대체)
  - `WriteOptionsType2.getFriendsEnableType(canUse: Boolean)` (파라미터명 변경, 로직 동일)
  - `AddImageItem(canUseImage: Boolean, addButtonClicked: () -> Unit)` (파라미터명 변경)

- [ ] **Step 1: WriteBucketListViewModel 교체**

생성자: `preferenceDataStoreModule` 제거, `checkUserLimitUseCase` 추가:

```kotlin
@HiltViewModel
class WriteBucketListViewModel @Inject constructor(
    private val addNewBucketList: AddNewBucketList,
    private val loadBucketDetail: LoadBucketDetail,
    private val loadKeyWord: LoadKeyWord,
    private val loadFriends: LoadFriends,
    private val loadCategoryList: LoadCategoryList,
    private val checkUserLimitUseCase: CheckUserLimit,
) : CommonViewModel() {
```

LiveData 교체 — 기존 (56-57행):

```kotlin
    private val _hasWaverPlus = MutableLiveData<Boolean>()
    val hasWaverPlus: LiveData<Boolean> get() = _hasWaverPlus
```

변경 후:

```kotlin
    private val _userLimitInfo = MutableLiveData<UserLimitInfo>()
    val userLimitInfo: LiveData<UserLimitInfo> get() = _userLimitInfo
```

함수 교체 — 기존 (169-175행):

```kotlin
    fun checkHasWaverPlus() {
        viewModelScope.launch {
            preferenceDataStoreModule.loadHasWaverPlus.collectLatest { value ->
                _hasWaverPlus.value = value
            }
        }
    }
```

변경 후:

```kotlin
    fun checkUserLimit() {
        viewModelScope.launch(ceh(_userLimitInfo, UserLimitInfo.NONE)) {
            val response = checkUserLimitUseCase.invoke()
            _userLimitInfo.value = if (response.success) {
                response.data
            } else {
                UserLimitInfo.NONE
            }
        }
    }
```

import 정리:
- 제거: `import com.zinc.datastore.login.PreferenceDataStoreModule`, `import kotlinx.coroutines.flow.collectLatest`
- 추가: `import com.zinc.common.models.UserLimitInfo`, `import com.zinc.domain.usecases.more.CheckUserLimit`

- [ ] **Step 2: WriteScreen1 — 이미지 추가 가능 여부로 변경**

기존 (93-95행):

```kotlin
    val hasWaverPlusAsState by viewModel.hasWaverPlus.observeAsState()

    var hasWaverPlus by remember { mutableStateOf(false) }
```

변경 후:

```kotlin
    val userLimitInfoAsState by viewModel.userLimitInfo.observeAsState()

    var canUseImage by remember { mutableStateOf(false) }
```

기존 (128행 부근) `LaunchedEffect(Unit, showWaverPlus.value)` 안의 `viewModel.checkHasWaverPlus()` → `viewModel.checkUserLimit()` 로 변경.

기존 (146-148행):

```kotlin
    LaunchedEffect(hasWaverPlusAsState) {
        hasWaverPlus = hasWaverPlusAsState ?: false
    }
```

변경 후:

```kotlin
    LaunchedEffect(userLimitInfoAsState) {
        canUseImage = userLimitInfoAsState?.canUseImage ?: false
    }
```

기존 (448-453행):

```kotlin
                                        AddImageItem(hasWaverPlus = hasWaverPlus) {
                                            if (hasWaverPlus) {
                                                selectedOption = IMAGE
                                            } else {
                                                showWaverPlus.value = true
                                            }
```

변경 후:

```kotlin
                                        AddImageItem(canUseImage = canUseImage) {
                                            if (canUseImage) {
                                                selectedOption = IMAGE
                                            } else {
                                                showWaverPlus.value = true
                                            }
```

- [ ] **Step 3: WriteScreen2 — 함께하기 가능 여부로 변경**

기존 (76행):

```kotlin
    val hasWaverPlusAsState by viewModel.hasWaverPlus.observeAsState()
```

변경 후:

```kotlin
    val userLimitInfoAsState by viewModel.userLimitInfo.observeAsState()
```

기존 (85, 88행):

```kotlin
    var hasWaverPlus by remember { mutableStateOf(false) }
    val showWaverPlus = remember { mutableStateOf(false) }
    var friendOption =
        remember { mutableStateOf(FRIENDS(enableType = getFriendsEnableType(hasWaverPlus))) }
```

변경 후:

```kotlin
    var canUseTogether by remember { mutableStateOf(false) }
    val showWaverPlus = remember { mutableStateOf(false) }
    var friendOption =
        remember { mutableStateOf(FRIENDS(enableType = getFriendsEnableType(canUseTogether))) }
```

기존 (90-92행) `LaunchedEffect(Unit, showWaverPlus.value)` 안의 `viewModel.checkHasWaverPlus()` → `viewModel.checkUserLimit()` 로 변경.

기존 (121-125행):

```kotlin
    LaunchedEffect(hasWaverPlusAsState) {
        hasWaverPlus = hasWaverPlusAsState ?: false
        friendOption.value = FRIENDS(enableType = getFriendsEnableType(hasWaverPlus))
        Log.e("ayhan", "hasWaverPlus : $hasWaverPlus, friendOption : $friendOption")
    }
```

변경 후:

```kotlin
    LaunchedEffect(userLimitInfoAsState) {
        canUseTogether = userLimitInfoAsState?.canUseTogether ?: false
        friendOption.value = FRIENDS(enableType = getFriendsEnableType(canUseTogether))
        Log.e("ayhan", "canUseTogether : $canUseTogether, friendOption : $friendOption")
    }
```

- [ ] **Step 4: Write.kt / ImageScreen.kt 파라미터명 변경**

`Write.kt` 기존 (158-160행):

```kotlin
    fun getFriendsEnableType(hasWaver: Boolean) =
        if (hasWaver) Enable
        else NoWaverPlus
```

변경 후:

```kotlin
    fun getFriendsEnableType(canUse: Boolean) =
        if (canUse) Enable
        else NoWaverPlus
```

`ImageScreen.kt` 기존 (88-92행):

```kotlin
fun AddImageItem(
    hasWaverPlus: Boolean,
    addButtonClicked: () -> Unit
) {
    val borderColor = if (hasWaverPlus) Gray3 else Main4
```

변경 후:

```kotlin
fun AddImageItem(
    canUseImage: Boolean,
    addButtonClicked: () -> Unit
) {
    val borderColor = if (canUseImage) Gray3 else Main4
```

같은 파일 112행 `if (!hasWaverPlus) {` → `if (!canUseImage) {`
같은 파일 127행 프리뷰 `AddImageItem(hasWaverPlus = false) {` → `AddImageItem(canUseImage = false) {`

- [ ] **Step 5: 컴파일 확인**

Run: `./gradlew :feature:ui-write:compileDebugKotlin :feature:ui-common:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: 커밋**

```bash
git add feature/ui-write feature/ui-common/src/main/java/com/zinc/waver/model/Write.kt
git commit -m "[버킷리스트 추가] 이미지/함께하기 사용 가능 여부를 check/limit API 기반으로 변경"
```

---

### Task 5: 로그인 시점 체크 전환 + DataStore 로직 제거

**Files:**
- Modify: `app/src/main/java/com/zinc/waver/ui/presentation/HomeViewModel.kt`
- Modify: `app/src/main/java/com/zinc/waver/ui/presentation/HomeActivity.kt:100,417-431`
- Modify: `datastore/src/main/java/com/zinc/datastore/login/PreferenceDataStoreModule.kt:27,89-97`

**Interfaces:**
- Consumes: `CheckUserLimit` 유즈케이스 (Task 2)
- Produces: `HomeViewModel.checkUserLimit()` (기존 `loadProfileInfo()`·`updateWaverPlus()` 대체). `PreferenceDataStoreModule`에서 `loadHasWaverPlus`/`setHasWaverPlus` 삭제 — 이후 어떤 코드도 참조하지 않아야 함.

- [ ] **Step 1: HomeViewModel 교체**

전체 파일을 다음으로 교체 (`LoadProfileInfo` 의존 제거, DataStore 저장 제거):

```kotlin
package com.zinc.waver.ui.presentation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.zinc.datastore.login.PreferenceDataStoreModule
import com.zinc.domain.models.BillingCycle
import com.zinc.domain.models.SubscriptionStartRequest
import com.zinc.domain.usecases.more.CheckUserLimit
import com.zinc.domain.usecases.more.StartSubscription
import com.zinc.waver.ui.viewmodel.CommonViewModel
import com.zinc.waver.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val preferenceDataStoreModule: PreferenceDataStoreModule,
    private val checkUserLimitUseCase: CheckUserLimit,
    private val startSubscription: StartSubscription
) : CommonViewModel() {
    private val _logoutSucceed = SingleLiveEvent<Boolean>()
    val logoutSucceed: LiveData<Boolean> get() = _logoutSucceed

    private val _doNothing = SingleLiveEvent<Nothing>()

    // 이번 세션에 서버로 구독 시작을 이미 알린 subscribeId(purchaseToken) 목록
    private val notifiedSubscribeIds = mutableSetOf<String>()

    fun logout() {
        viewModelScope.launch {
            preferenceDataStoreModule.clearLoginEmail()
            _logoutSucceed.value = true
        }
    }

    // 로그인/앱 시작 시 구독 상태 확인 (판단은 각 화면에서 API 호출로 수행)
    fun checkUserLimit() {
        viewModelScope.launch(ceh(_doNothing, null)) {
            val response = checkUserLimitUseCase.invoke()
            Log.d("HomeViewModel", "checkUserLimit: ${response.data}")
        }
    }

    // 구독 완료(구매/앱 시작 시 활성 구독 감지) 시 서버에 구독 시작을 알린다.
    // 앱 시작 시 YEAR/MONTH 두 설정이 같은 구매를 매칭하므로, 세션당 같은 subscribeId는 한 번만 전송한다.
    fun notifySubscriptionStarted(billingCycle: BillingCycle, subscribeId: String) {
        if (!notifiedSubscribeIds.add(subscribeId)) return
        viewModelScope.launch(ceh(_doNothing, null)) {
            startSubscription(
                SubscriptionStartRequest(
                    billingCycle = billingCycle,
                    subscribeId = subscribeId
                )
            )
        }
    }
}
```

- [ ] **Step 2: HomeActivity 수정**

100행: `viewModel.loadProfileInfo()` → `viewModel.checkUserLimit()`

407행 주석 `// 서버 전송이 성공하면 그 안에서 hasWaverPlus=true 로 저장한다` → `// 서버에 구독 시작을 알린다` 로 변경

기존 (417-431행):

```kotlin
            alreadyPurchased = { purchased, subscribeId ->
                if (purchased && subscribeId != null) {
                    // 활성 구독 감지 → 서버에 알리고, 성공 시 hasWaverPlus=true 저장 (세션당 1회, best-effort 주기)
                    viewModel.notifySubscriptionStarted(
                        billingCycle = when (type) {
                            WaverPlusType.YEAR -> BillingCycle.YEARLY
                            WaverPlusType.MONTH -> BillingCycle.MONTHLY
                        },
                        subscribeId = subscribeId
                    )
                } else if (!purchased) {
                    // 활성 구독 없음 → 플래그 해제 (서버와 무관)
                    viewModel.updateWaverPlus(false)
                }
            })
```

변경 후 (`updateWaverPlus` 분기 삭제 — 구독 여부는 API로만 판단하므로 클라 플래그 해제 불필요):

```kotlin
            alreadyPurchased = { purchased, subscribeId ->
                if (purchased && subscribeId != null) {
                    // 활성 구독 감지 → 서버에 알림 (세션당 1회, best-effort 주기)
                    viewModel.notifySubscriptionStarted(
                        billingCycle = when (type) {
                            WaverPlusType.YEAR -> BillingCycle.YEARLY
                            WaverPlusType.MONTH -> BillingCycle.MONTHLY
                        },
                        subscribeId = subscribeId
                    )
                }
            })
```

- [ ] **Step 3: PreferenceDataStoreModule에서 waverPlus 관련 삭제**

`PreferenceDataStoreModule.kt`에서 다음 3개 삭제:

27행:

```kotlin
    private val waverPlusKey = booleanPreferencesKey("waverPlusKey")
```

89-97행:

```kotlin
    val loadHasWaverPlus: Flow<Boolean> = loginDataStore.data.map { preferences ->
        preferences[waverPlusKey] ?: false
    }

    suspend fun setHasWaverPlus(hasWaverPlus: Boolean) {
        loginDataStore.edit { preferences ->
            preferences[waverPlusKey] = hasWaverPlus
        }
    }
```

`booleanPreferencesKey` import(6행)도 더 이상 사용처가 없으므로 삭제:

```kotlin
import androidx.datastore.preferences.core.booleanPreferencesKey
```

- [ ] **Step 4: 잔존 참조 확인**

Run: `grep -rn "loadHasWaverPlus\|setHasWaverPlus\|updateWaverPlus\|checkHasWaverPlus" --include="*.kt" app datastore feature domain data common | grep -v "/build/"`
Expected: `MoreViewModel.kt`의 `checkHasWaverPlus`(API 기반, Task 3에서 변경한 것)와 `MoreScreen.kt`의 호출부만 출력. `loadHasWaverPlus`/`setHasWaverPlus`/`updateWaverPlus`는 0건.

- [ ] **Step 5: 컴파일 확인**

Run: `./gradlew :datastore:compileDebugKotlin :app:compileDebugKotlin`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: 커밋**

```bash
git add app/src/main/java/com/zinc/waver/ui/presentation/HomeViewModel.kt app/src/main/java/com/zinc/waver/ui/presentation/HomeActivity.kt datastore/src/main/java/com/zinc/datastore/login/PreferenceDataStoreModule.kt
git commit -m "[웨이버플러스] 로그인 시점 체크를 API로 전환하고 DataStore hasWaverPlus 로직 제거"
```

---

### Task 6: 전체 빌드 검증

**Files:** 없음 (검증 전용)

- [ ] **Step 1: 단위 테스트 전체 실행**

Run: `./gradlew :common:testDebugUnitTest`
Expected: BUILD SUCCESSFUL, 5 tests passed

- [ ] **Step 2: 전체 빌드**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: 최종 확인**

Run: `git status`
Expected: 이 작업으로 인한 미커밋 변경 없음 (기존 작업 트리에 있던 무관한 변경은 그대로 둔다)
