# 마이버리 데이터 이관 연결 화면 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 회원가입 시 `/waver/user/join` 응답의 `myburyYn`이 `"Y"`이면 마이버리 데이터 연결 화면을 띄우고, 사용자가 선택하면 `/waver/user/migration`으로 데이터 이관을 요청한다.

**Architecture:** 순수 로직(응답 파싱, 이관 결과 코드 매핑)은 `common` 모듈에 두어 JVM 유닛테스트로 검증한다. 네트워크는 기존 `WaverApi` → `LoginRepository` → UseCase 3단 구조를 그대로 따른다. 화면은 `app` 모듈의 기존 가입 흐름(`JoinScreen`)에 풀스크린 Composable로 끼워 넣고, 이관 요청과 결과 팝업은 화면 내부에서 완결시켜 호출자는 "끝났다"만 알게 한다.

**Tech Stack:** Kotlin, Jetpack Compose, Retrofit + Gson, Hilt, LiveData, JUnit4

## Global Constraints

- 프로젝트는 Gson을 쓰면서 `@SerializedName`을 **한 곳도** 사용하지 않는다. 프로퍼티명이 곧 JSON 키다. 필드명은 `myburyYn` (전부 소문자 `bury`) 로 정확히 맞춘다.
- 이관 API(`POST /waver/user/migration`)는 인증이 필요하다. 이 화면은 `loginByEmail()`이 성공해 accessToken을 DataStore에 저장한 **이후**에만 노출되어야 한다. 순서를 앞당기면 401이 발생한다.
- 이관은 서버 스케줄러가 비동기로 처리한다. 앱은 요청 접수 결과만 안내하고, 어떤 결과에서도 가입 흐름을 막지 않는다.
- 문자열은 `app/src/main/res/values/strings.xml`(영어, default)과 `app/src/main/res/values-ko/strings.xml`(한국어) **양쪽 모두** 추가한다. 이 프로젝트의 default locale은 영어다.
- 새 drawable 에셋을 추가하지 않는다. 기존 리소스만 사용한다.
- 텍스트는 `androidx.compose.material.Text`가 아니라 프로젝트 공통 컴포넌트 `MyText`를 쓰고, 폰트 크기는 `dpToSp(...)`로 지정한다.
- 커밋 메시지는 이 저장소 관례에 따라 한국어로 `[영역] 요약` 형태로 쓴다.
- 빌드 검증 명령은 `./gradlew :app:assembleDebug` 다.

---

### Task 1: `JoinResponse` 타입화 및 `myburyYn` 파싱

`JoinResponse.data`가 `Any?`로 선언되어 있어 응답 본문의 어떤 필드도 읽을 수 없다. `grep JoinResponse` 결과 소비처는 선언과 시그니처뿐이므로 타입을 바꿔도 기존 동작에 영향이 없다.

**Files:**
- Modify: `common/src/main/java/com/zinc/common/models/Login.kt:6-11`
- Test: `common/src/test/java/com/zinc/common/models/JoinResponseTest.kt` (create)

**Interfaces:**
- Consumes: `YesOrNo` enum (`common/src/main/java/com/zinc/common/models/Common.kt:16` — `Y`, `N`, `isYes()`, `isNo()`)
- Produces:
  - `JoinResponse(success: Boolean, code: String, message: String, data: JoinData?)`
  - `JoinData(myburyYn: YesOrNo?)` with `fun isMyBuryUser(): Boolean`

`common` 모듈은 이미 Gson(`libs.google.gson`)과 JUnit(`libs.test.junit`)을 test 의존성으로 갖고 있다. 새 의존성 추가는 필요 없다.

- [ ] **Step 1: 실패하는 테스트 작성**

`common/src/test/java/com/zinc/common/models/JoinResponseTest.kt` 를 새로 만든다. 기존 `UserLimitInfoTest`와 같은 위치, 같은 스타일(백틱 한글 테스트명)이다.

```kotlin
package com.zinc.common.models

import com.google.gson.Gson
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class JoinResponseTest {

    private val gson = Gson()

    private fun parse(json: String): JoinResponse =
        gson.fromJson(json, JoinResponse::class.java)

    @Test
    fun `myburyYn 이 Y 면 마이버리 회원으로 판정한다`() {
        val response = parse(
            """{"success":true,"code":"200","message":"ok","data":{"myburyYn":"Y"}}"""
        )
        assertTrue(response.data?.isMyBuryUser() == true)
    }

    @Test
    fun `myburyYn 이 N 이면 마이버리 회원이 아니다`() {
        val response = parse(
            """{"success":true,"code":"200","message":"ok","data":{"myburyYn":"N"}}"""
        )
        assertFalse(response.data?.isMyBuryUser() == true)
    }

    @Test
    fun `data 가 아예 없으면 마이버리 회원이 아니다`() {
        val response = parse("""{"success":true,"code":"200","message":"ok"}""")
        assertNull(response.data)
        assertFalse(response.data?.isMyBuryUser() == true)
    }

    @Test
    fun `data 안에 myburyYn 이 없으면 마이버리 회원이 아니다`() {
        val response = parse("""{"success":true,"code":"200","message":"ok","data":{}}""")
        assertFalse(response.data?.isMyBuryUser() == true)
    }

    @Test
    fun `알 수 없는 myburyYn 값은 마이버리 회원이 아니다`() {
        val response = parse(
            """{"success":true,"code":"200","message":"ok","data":{"myburyYn":"MAYBE"}}"""
        )
        assertFalse(response.data?.isMyBuryUser() == true)
    }
}
```

마지막 두 테스트가 중요하다. Gson은 알 수 없는 enum 이름과 누락된 필드를 모두 `null`로 채우므로, 서버가 필드를 안 내려주거나 예상 못 한 값을 주더라도 마이버리 회원이 아닌 사용자의 가입 흐름이 기존과 동일하게 유지되는지를 확인한다.

- [ ] **Step 2: 테스트가 실패하는지 확인**

Run: `./gradlew :common:test --tests "com.zinc.common.models.JoinResponseTest"`

Expected: 컴파일 실패. `JoinData` 를 찾을 수 없고, `response.data` 가 `Any?` 타입이라 `isMyBuryUser()` 를 호출할 수 없다는 에러가 난다.

- [ ] **Step 3: 최소 구현 작성**

`common/src/main/java/com/zinc/common/models/Login.kt` 의 `JoinResponse` 선언(6~11행)을 아래로 교체한다. `import java.io.Serializable` 은 파일 상단에 이미 있다.

교체 전:

```kotlin
data class JoinResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: Any? // 에러 아닐 때는 수정해야 할 듯...
) : Serializable
```

교체 후:

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
    // 마이버리 기존 회원 여부. 필드 누락이나 알 수 없는 값이면 false 로 떨어진다.
    fun isMyBuryUser() = myburyYn?.isYes() == true
}
```

`YesOrNo` 는 같은 패키지(`com.zinc.common.models`)의 `Common.kt` 에 있으므로 import 가 필요 없다.

- [ ] **Step 4: 테스트가 통과하는지 확인**

Run: `./gradlew :common:test --tests "com.zinc.common.models.JoinResponseTest"`

Expected: 5개 테스트 전부 PASS

- [ ] **Step 5: 전체 빌드 확인**

Run: `./gradlew :app:assembleDebug`

Expected: BUILD SUCCESSFUL. `JoinResponse.data` 를 읽는 코드가 없으므로 타입 변경으로 깨지는 곳이 없어야 한다. 만약 컴파일 에러가 난다면 `data` 를 읽는 곳을 찾아 `data?.myburyYn` 기준으로 고친다.

- [ ] **Step 6: 커밋**

```bash
git add common/src/main/java/com/zinc/common/models/Login.kt \
        common/src/test/java/com/zinc/common/models/JoinResponseTest.kt
git commit -m "[가입] join 응답에 myburyYn 필드 추가 및 JoinResponse 타입화"
```

---

### Task 2: 이관 API 계층과 결과 코드 매핑

`POST /waver/user/migration` 을 호출하는 3단 구조(Api → Repository → UseCase)를 추가하고, 응답 코드를 화면이 쓸 결과 타입으로 매핑한다. 매핑 로직을 `common` 에 두어 유닛테스트로 검증한다.

**Files:**
- Create: `common/src/main/java/com/zinc/common/models/MyBuryMigration.kt`
- Modify: `data/src/main/java/com/zinc/data/api/WaverApi.kt` (68행 `crateProfile` 선언 바로 뒤)
- Modify: `domain/src/main/java/com/zinc/domain/repository/LoginRepository.kt`
- Modify: `data/src/main/java/com/zinc/data/repository/LoginRepositoryImpl.kt`
- Create: `domain/src/main/java/com/zinc/domain/usecases/login/RequestMyBuryMigration.kt`
- Test: `common/src/test/java/com/zinc/common/models/MyBuryMigrationResultTest.kt` (create)

**Interfaces:**
- Consumes: `CommonResponse2(success: Boolean, code: String, message: String)` (`common/src/main/java/com/zinc/common/models/Common.kt:10`)
- Produces:
  - `enum class MyBuryMigrationResult { REQUESTED, ALREADY_DONE, ALREADY_REQUESTED, NONE }` with `companion object { fun from(response: CommonResponse2): MyBuryMigrationResult }`
  - `WaverApi.requestMyBuryMigration(): CommonResponse2`
  - `LoginRepository.requestMyBuryMigration(): CommonResponse2`
  - `class RequestMyBuryMigration` with `suspend operator fun invoke(): CommonResponse2`

- [ ] **Step 1: 실패하는 테스트 작성**

`common/src/test/java/com/zinc/common/models/MyBuryMigrationResultTest.kt` 를 새로 만든다.

```kotlin
package com.zinc.common.models

import org.junit.Assert.assertEquals
import org.junit.Test

class MyBuryMigrationResultTest {

    private fun response(success: Boolean, code: String) =
        CommonResponse2(success = success, code = code, message = "")

    @Test
    fun `요청이 접수되면 REQUESTED`() {
        assertEquals(
            MyBuryMigrationResult.REQUESTED,
            MyBuryMigrationResult.from(response(success = true, code = "200"))
        )
    }

    @Test
    fun `8201 은 이미 이관이 완료된 상태`() {
        assertEquals(
            MyBuryMigrationResult.ALREADY_DONE,
            MyBuryMigrationResult.from(response(success = false, code = "8201"))
        )
    }

    @Test
    fun `8202 는 이미 이관이 요청된 상태`() {
        assertEquals(
            MyBuryMigrationResult.ALREADY_REQUESTED,
            MyBuryMigrationResult.from(response(success = false, code = "8202"))
        )
    }

    @Test
    fun `8200 마이버리 회원이 아니면 안내 없이 넘어간다`() {
        assertEquals(
            MyBuryMigrationResult.NONE,
            MyBuryMigrationResult.from(response(success = false, code = "8200"))
        )
    }

    @Test
    fun `알 수 없는 실패 코드는 안내 없이 넘어간다`() {
        assertEquals(
            MyBuryMigrationResult.NONE,
            MyBuryMigrationResult.from(response(success = false, code = "9999"))
        )
    }

    @Test
    fun `성공 응답은 코드와 무관하게 REQUESTED 로 판정한다`() {
        assertEquals(
            MyBuryMigrationResult.REQUESTED,
            MyBuryMigrationResult.from(response(success = true, code = "8201"))
        )
    }
}
```

마지막 테스트는 `success` 판정을 코드 검사보다 **먼저** 하도록 순서를 못 박는 것이다.

- [ ] **Step 2: 테스트가 실패하는지 확인**

Run: `./gradlew :common:test --tests "com.zinc.common.models.MyBuryMigrationResultTest"`

Expected: 컴파일 실패. `MyBuryMigrationResult` 를 찾을 수 없다는 에러.

- [ ] **Step 3: 결과 타입 구현**

`common/src/main/java/com/zinc/common/models/MyBuryMigration.kt` 를 새로 만든다.

```kotlin
package com.zinc.common.models

// 마이버리 데이터 이관 요청 결과.
// 이관은 서버 스케줄러가 비동기로 처리하므로 앱은 요청 접수 결과만 알 수 있다.
enum class MyBuryMigrationResult {
    REQUESTED,          // 이관 요청 접수됨
    ALREADY_DONE,       // 8201 - 이미 이관 완료
    ALREADY_REQUESTED,  // 8202 - 이미 이관 요청됨
    NONE;               // 8200 / 그 외 코드 / 네트워크 오류 - 안내 없이 진행

    companion object {
        fun from(response: CommonResponse2): MyBuryMigrationResult = when {
            response.success -> REQUESTED
            response.code == "8201" -> ALREADY_DONE
            response.code == "8202" -> ALREADY_REQUESTED
            else -> NONE
        }
    }
}
```

- [ ] **Step 4: 테스트가 통과하는지 확인**

Run: `./gradlew :common:test --tests "com.zinc.common.models.MyBuryMigrationResultTest"`

Expected: 6개 테스트 전부 PASS

- [ ] **Step 5: API 선언 추가**

`data/src/main/java/com/zinc/data/api/WaverApi.kt` 에서 `crateProfile` 선언이 끝나는 68행(`): JoinResponse`) 바로 다음에 아래를 넣는다.

```kotlin
    // 마이버리 데이터 이관 요청 (mybury 기존 회원만 요청 가능, 스케줄러가 순차 이관)
    @POST("/waver/user/migration")
    suspend fun requestMyBuryMigration(): CommonResponse2
```

`CommonResponse2` 는 이 파일 14행에 이미 import 되어 있다. `@POST` 도 47행에 이미 import 되어 있다. 새 import 는 필요 없다.

- [ ] **Step 6: Repository 인터페이스와 구현 추가**

`domain/src/main/java/com/zinc/domain/repository/LoginRepository.kt` 의 인터페이스 본문 마지막 줄(`checkUserStatus` 선언) 뒤에 추가한다.

```kotlin
    suspend fun requestMyBuryMigration(): CommonResponse2
```

같은 파일 상단 import 에 추가한다.

```kotlin
import com.zinc.common.models.CommonResponse2
```

`data/src/main/java/com/zinc/data/repository/LoginRepositoryImpl.kt` 의 `checkUserStatus` 구현 뒤에 추가한다.

```kotlin
    override suspend fun requestMyBuryMigration(): CommonResponse2 {
        return waverApi.requestMyBuryMigration()
    }
```

같은 파일 상단 import 에 추가한다.

```kotlin
import com.zinc.common.models.CommonResponse2
```

- [ ] **Step 7: UseCase 추가**

`domain/src/main/java/com/zinc/domain/usecases/login/RequestMyBuryMigration.kt` 를 새로 만든다. 같은 폴더의 `CreateProfile.kt` 와 동일한 패턴이다. Hilt 는 `@Inject constructor` 만으로 주입되므로 별도 모듈 등록이 필요 없다.

```kotlin
package com.zinc.domain.usecases.login

import com.zinc.common.models.CommonResponse2
import com.zinc.domain.repository.LoginRepository
import javax.inject.Inject

// 마이버리 데이터 이관 요청
class RequestMyBuryMigration @Inject constructor(
    private val loginRepository: LoginRepository
) {
    suspend operator fun invoke(): CommonResponse2 =
        loginRepository.requestMyBuryMigration()
}
```

- [ ] **Step 8: 빌드와 테스트 확인**

Run: `./gradlew :common:test :app:assembleDebug`

Expected: 테스트 PASS, BUILD SUCCESSFUL

- [ ] **Step 9: 커밋**

```bash
git add common/src/main/java/com/zinc/common/models/MyBuryMigration.kt \
        common/src/test/java/com/zinc/common/models/MyBuryMigrationResultTest.kt \
        data/src/main/java/com/zinc/data/api/WaverApi.kt \
        data/src/main/java/com/zinc/data/repository/LoginRepositoryImpl.kt \
        domain/src/main/java/com/zinc/domain/repository/LoginRepository.kt \
        domain/src/main/java/com/zinc/domain/usecases/login/RequestMyBuryMigration.kt
git commit -m "[가입] 마이버리 데이터 이관 API 및 응답 코드 매핑 추가"
```

---

### Task 3: `MyBuryConnectScreen` 화면과 ViewModel

데이터 연결 화면을 만든다. `Dialog` 가 아닌 풀스크린 Composable이다. 디자인이 상태바까지 덮는 전면 화면이고, 기존 `WelcomePopupScreen`(320dp `Dialog`)과 성격이 다르다.

이관 요청과 결과 팝업은 화면 내부에서 완결시키고, 호출자에게는 `onFinished()` 하나만 노출한다. 세 갈래 분기(이관 / 새로 시작 / 닫기)가 모두 같은 곳으로 수렴하므로 콜백을 나눌 이유가 없다.

**Files:**
- Modify: `app/src/main/res/values/strings.xml` (`</resources>` 직전)
- Modify: `app/src/main/res/values-ko/strings.xml` (`</resources>` 직전)
- Create: `app/src/main/java/com/zinc/waver/ui/presentation/login/MyBuryConnectViewModel.kt`
- Create: `app/src/main/java/com/zinc/waver/ui/presentation/login/MyBuryConnectScreen.kt`

**Interfaces:**
- Consumes:
  - `MyBuryMigrationResult` + `MyBuryMigrationResult.from(...)` (Task 2)
  - `RequestMyBuryMigration` UseCase with `suspend operator fun invoke(): CommonResponse2` (Task 2)
  - `CommonViewModel` (`com.zinc.waver.ui.viewmodel.CommonViewModel`) — `fun <T> ceh(event: MutableLiveData<T>, value: T?)` 로 코루틴 예외를 LiveData 값으로 흘려준다
  - `MyText`, `dpToSp`, `CommonDialogView`, `DialogButtonInfo`, 테마 컬러 `Gray1/Gray3/Gray6/Gray10/Main4`
- Produces:
  - `@Composable fun MyBuryConnectScreen(onFinished: () -> Unit)`
  - `class MyBuryConnectViewModel` — `val migrationResult: LiveData<MyBuryMigrationResult?>`, `fun requestMigration()`

에셋은 전부 기존 리소스를 쓴다.

| 용도 | 리소스 | 크기 |
|---|---|---|
| 상단 그라데이션 배경 | `CommonR.drawable.bg_membership_login` | 360×360 vector |
| 좌측 마이버리 로고 | `CommonR.drawable.mybury_logo` | 48×48 vector |
| 두 로고를 잇는 물결 | `CommonR.drawable.mybury_to_waver` | 68×64 vector |
| 우측 웨이버 로고 | `R.drawable.playstore` | webp, 앱 아이콘 |
| 닫기 버튼 | `CommonR.drawable.btn_40_close` | 40×40 vector, 검은 stroke |

`btn_40_close` 는 검은색 stroke 이므로 `Icon(..., tint = Gray1)` 으로 흰색으로 바꿔 쓴다.

- [ ] **Step 1: 문자열 리소스 추가 (한국어)**

`app/src/main/res/values-ko/strings.xml` 의 `</resources>` 직전에 넣는다.

```xml

    <!-- 마이버리 데이터 연결 -->
    <string name="myBuryConnectCloseDesc">닫기</string>
    <string name="myBuryConnectHeroTitle">버킷리스트\n이제 함께 즐기세요</string>
    <string name="myBuryConnectTitle">버킷리스트는 웨이버에서 계속돼요</string>
    <string name="myBuryConnectHighlight">마이버리 데이터(버킷리스트)를\n웨이버에 옮길 수 있습니다.</string>
    <string name="myBuryConnectDescription">이후 마이버리에 작성한 내용은 저장되지 않아요.\n버킷리스트는 웨이버에서 이어서 작성해 주세요.</string>
    <string name="myBuryConnectMigrateButton">웨이버에서 이어서 진행하기</string>
    <string name="myBuryConnectFreshStartButton">새롭게 시작하기</string>
    <string name="myBuryMigrationRequested">데이터 이관을 진행합니다.</string>
    <string name="myBuryMigrationAlreadyDone">이미 데이터 이관이 완료되었습니다.</string>
    <string name="myBuryMigrationAlreadyRequested">이미 데이터 이관 진행중입니다.</string>
```

- [ ] **Step 2: 문자열 리소스 추가 (영어, default)**

`app/src/main/res/values/strings.xml` 의 `</resources>` 직전에 넣는다. 이 프로젝트의 default locale 은 영어이므로 빠뜨리면 한국어 외 환경에서 문자열이 비어 보인다.

```xml

    <!-- MyBury data connect -->
    <string name="myBuryConnectCloseDesc">Close</string>
    <string name="myBuryConnectHeroTitle">Your bucket list\nis better together now</string>
    <string name="myBuryConnectTitle">Your bucket list continues on Waver</string>
    <string name="myBuryConnectHighlight">You can move your MyBury data\n(bucket list) over to Waver.</string>
    <string name="myBuryConnectDescription">Anything you write in MyBury from now on won\'t be saved.\nPlease continue your bucket list on Waver.</string>
    <string name="myBuryConnectMigrateButton">Continue on Waver</string>
    <string name="myBuryConnectFreshStartButton">Start fresh</string>
    <string name="myBuryMigrationRequested">Starting your data migration.</string>
    <string name="myBuryMigrationAlreadyDone">Your data migration is already complete.</string>
    <string name="myBuryMigrationAlreadyRequested">Your data migration is already in progress.</string>
```

- [ ] **Step 3: ViewModel 작성**

`app/src/main/java/com/zinc/waver/ui/presentation/login/MyBuryConnectViewModel.kt` 를 새로 만든다.

```kotlin
package com.zinc.waver.ui.presentation.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zinc.common.models.MyBuryMigrationResult
import com.zinc.domain.usecases.login.RequestMyBuryMigration
import com.zinc.waver.ui.viewmodel.CommonViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyBuryConnectViewModel @Inject constructor(
    private val requestMyBuryMigration: RequestMyBuryMigration
) : CommonViewModel() {

    private val _migrationResult = MutableLiveData<MyBuryMigrationResult?>(null)
    val migrationResult: LiveData<MyBuryMigrationResult?> get() = _migrationResult

    // 중복 요청 방지. 결과가 나오면 화면이 곧 닫히므로 되돌릴 필요가 없다.
    private var isRequesting = false

    fun requestMigration() {
        if (isRequesting) return
        isRequesting = true

        // 네트워크 오류는 NONE 으로 흘려서 안내 없이 다음 단계로 넘어가게 한다.
        viewModelScope.launch(ceh(_migrationResult, MyBuryMigrationResult.NONE)) {
            _migrationResult.value = MyBuryMigrationResult.from(requestMyBuryMigration())
        }
    }
}
```

- [ ] **Step 4: 화면 작성**

`app/src/main/java/com/zinc/waver/ui/presentation/login/MyBuryConnectScreen.kt` 를 새로 만든다.

```kotlin
package com.zinc.waver.ui.presentation.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.zinc.common.models.MyBuryMigrationResult
import com.zinc.waver.R
import com.zinc.waver.model.DialogButtonInfo
import com.zinc.waver.ui.design.theme.Gray1
import com.zinc.waver.ui.design.theme.Gray10
import com.zinc.waver.ui.design.theme.Gray3
import com.zinc.waver.ui.design.theme.Gray6
import com.zinc.waver.ui.design.theme.Main4
import com.zinc.waver.ui.presentation.component.MyText
import com.zinc.waver.ui.presentation.component.dialog.CommonDialogView
import com.zinc.waver.ui.util.dpToSp
import com.zinc.waver.ui_common.R as CommonR

/**
 * 마이버리 기존 회원(join 응답의 myburyYn == Y)에게 데이터 이관을 안내하는 화면.
 *
 * 이관 요청과 결과 팝업은 이 화면 안에서 끝난다. 호출자는 화면이 끝났다는 것만 알면 되므로
 * 콜백은 [onFinished] 하나뿐이다.
 */
@Composable
fun MyBuryConnectScreen(onFinished: () -> Unit) {
    val viewModel: MyBuryConnectViewModel = hiltViewModel()
    val migrationResult by viewModel.migrationResult.observeAsState()

    var isRequesting by remember { mutableStateOf(false) }

    // 안내할 내용이 없는 결과(8200 / 기타 코드 / 네트워크 오류)는 팝업 없이 바로 다음 단계로.
    LaunchedEffect(migrationResult) {
        if (migrationResult == MyBuryMigrationResult.NONE) {
            onFinished()
        }
    }

    MyBuryConnectContent(
        isRequesting = isRequesting,
        onMigrateClicked = {
            isRequesting = true
            viewModel.requestMigration()
        },
        onSkipClicked = onFinished
    )

    migrationResult?.messageRes()?.let { messageRes ->
        CommonDialogView(
            message = stringResource(id = messageRes),
            dismissAvailable = false,
            rightButtonInfo = DialogButtonInfo(
                text = CommonR.string.confirm,
                color = Main4
            ),
            rightButtonEvent = onFinished
        )
    }
}

private fun MyBuryMigrationResult.messageRes(): Int? = when (this) {
    MyBuryMigrationResult.REQUESTED -> R.string.myBuryMigrationRequested
    MyBuryMigrationResult.ALREADY_DONE -> R.string.myBuryMigrationAlreadyDone
    MyBuryMigrationResult.ALREADY_REQUESTED -> R.string.myBuryMigrationAlreadyRequested
    MyBuryMigrationResult.NONE -> null
}

@Composable
private fun MyBuryConnectContent(
    isRequesting: Boolean,
    onMigrateClicked: () -> Unit,
    onSkipClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Gray1)
    ) {
        MyBuryConnectHeroView(
            onCloseClicked = onSkipClicked,
            modifier = Modifier.weight(1f)
        )
        MyBuryConnectGuideView(
            isRequesting = isRequesting,
            onMigrateClicked = onMigrateClicked,
            onSkipClicked = onSkipClicked
        )
    }
}

@Composable
private fun MyBuryConnectHeroView(
    onCloseClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Image(
            painter = painterResource(id = CommonR.drawable.bg_membership_login),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = CommonR.drawable.btn_40_close),
                contentDescription = stringResource(id = R.string.myBuryConnectCloseDesc),
                tint = Gray1,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 8.dp, top = 8.dp)
                    .clickable { onCloseClicked() }
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = CommonR.drawable.mybury_logo),
                    contentDescription = null,
                    modifier = Modifier.size(76.dp)
                )
                Image(
                    painter = painterResource(id = CommonR.drawable.mybury_to_waver),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(horizontal = 2.dp)
                        .width(56.dp)
                        .height(52.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.playstore),
                    contentDescription = null,
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(20.dp))
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            MyText(
                text = stringResource(id = R.string.myBuryConnectHeroTitle),
                color = Gray1,
                fontSize = dpToSp(24.dp),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = dpToSp(34.dp)
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun MyBuryConnectGuideView(
    isRequesting: Boolean,
    onMigrateClicked: () -> Unit,
    onSkipClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Gray1)
            .navigationBarsPadding()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(36.dp))

        MyText(
            text = stringResource(id = R.string.myBuryConnectTitle),
            color = Gray10,
            fontSize = dpToSp(18.dp),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        MyText(
            text = stringResource(id = R.string.myBuryConnectHighlight),
            color = Main4,
            fontSize = dpToSp(15.dp),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = dpToSp(22.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        MyText(
            text = stringResource(id = R.string.myBuryConnectDescription),
            color = Gray6,
            fontSize = dpToSp(14.dp),
            textAlign = TextAlign.Center,
            lineHeight = dpToSp(21.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        MyBuryConnectButton(
            text = stringResource(id = R.string.myBuryConnectMigrateButton),
            textColor = Gray1,
            backgroundColor = Main4,
            borderColor = null,
            enabled = !isRequesting,
            onClicked = onMigrateClicked
        )

        Spacer(modifier = Modifier.height(12.dp))

        MyBuryConnectButton(
            text = stringResource(id = R.string.myBuryConnectFreshStartButton),
            textColor = Gray10,
            backgroundColor = Gray1,
            borderColor = Gray3,
            enabled = !isRequesting,
            onClicked = onSkipClicked
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun MyBuryConnectButton(
    text: String,
    textColor: Color,
    backgroundColor: Color,
    borderColor: Color?,
    enabled: Boolean,
    onClicked: () -> Unit
) {
    val shape = RoundedCornerShape(8.dp)
    MyText(
        text = text,
        color = textColor,
        fontSize = dpToSp(16.dp),
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(color = backgroundColor, shape = shape)
            .then(
                if (borderColor == null) Modifier
                else Modifier.border(width = 1.dp, color = borderColor, shape = shape)
            )
            .clickable(enabled = enabled) { onClicked() }
            .padding(vertical = 18.dp)
    )
}

@Preview(showBackground = true, heightDp = 780)
@Composable
private fun MyBuryConnectContentPreview() {
    MyBuryConnectContent(
        isRequesting = false,
        onMigrateClicked = {},
        onSkipClicked = {}
    )
}
```

- [ ] **Step 5: 빌드 확인**

Run: `./gradlew :app:assembleDebug`

Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Preview 렌더링 확인**

Android Studio 에서 `MyBuryConnectScreen.kt` 를 열고 `MyBuryConnectContentPreview` 가 렌더링되는지 본다. 디자인과 비교해 확인할 것:

- 상단 그라데이션이 화면 위쪽 절반을 채우고 좌상단 X 가 흰색인지
- 마이버리 로고 · 물결 · 웨이버 로고가 가로로 나란히 놓이는지 (디자인처럼 살짝 겹치게 하려면 물결 `Image` 에 `Modifier.offset(x = ...)` 을 추가해 조정한다)
- 하단 흰 영역의 텍스트 3단(검정 굵게 / 파랑 굵게 / 회색)과 버튼 2개(파랑 채움 / 흰 배경 + 회색 보더)가 순서대로 보이는지

- [ ] **Step 7: 커밋**

```bash
git add app/src/main/res/values/strings.xml \
        app/src/main/res/values-ko/strings.xml \
        app/src/main/java/com/zinc/waver/ui/presentation/login/MyBuryConnectViewModel.kt \
        app/src/main/java/com/zinc/waver/ui/presentation/login/MyBuryConnectScreen.kt
git commit -m "[가입] 마이버리 데이터 연결 화면 추가"
```

---

### Task 4: 가입 흐름에 연결

`join` 성공 후 `loginByEmail()` 로 accessToken 을 저장한 다음에만 데이터 연결 화면을 띄운다. 이관 API 가 인증을 요구하므로 이 순서가 필수다.

**핵심 설계 판단:** `isMyBuryUser` 를 별도 LiveData 로 노출하지 않고 `createNewProfile` → `goToLogin` 함수 인자로 넘긴다. 그러면 "두 LiveData 중 어느 게 먼저 도착하는가" 하는 순서 문제가 아예 생기지 않고, `_joinSucceed` 는 accessToken 저장이 끝난 뒤 딱 한 번 발화한다.

**Files:**
- Modify: `app/src/main/java/com/zinc/waver/ui/presentation/login/JoinNickNameViewModel.kt:32-33, 71-117`
- Modify: `app/src/main/java/com/zinc/waver/ui/presentation/login/JoinCreateProfile2.kt:61-86`
- Modify: `app/src/main/java/com/zinc/waver/ui/presentation/login/JoinScreen.kt:21-77`

**Interfaces:**
- Consumes:
  - `JoinData.isMyBuryUser(): Boolean` (Task 1)
  - `@Composable fun MyBuryConnectScreen(onFinished: () -> Unit)` (Task 3)
- Produces:
  - `JoinNickNameViewModel.joinSucceed: LiveData<Boolean>` — 발화하면 가입+로그인+토큰 저장이 모두 끝난 상태이고, 값은 `isMyBuryUser` 다. (기존 `goToLogin: LiveData<Boolean>` 를 대체한다)
  - `JoinCreateProfile2(emailInfo, createProfileInfo, goToMain: (isMyBuryUser: Boolean) -> Unit, goToBack: () -> Unit)`

- [ ] **Step 1: ViewModel 의 완료 이벤트에 `isMyBuryUser` 를 실어보내기**

`JoinNickNameViewModel.kt` 32~33행의 선언을 교체한다.

교체 전:

```kotlin
    private val _goToLogin = SingleLiveEvent<Boolean>()
    val goToLogin: LiveData<Boolean> get() = _goToLogin
```

교체 후:

```kotlin
    // 발화 시점 = 가입 + 로그인 + accessToken 저장까지 모두 끝난 뒤. 값은 마이버리 기존 회원 여부.
    private val _joinSucceed = SingleLiveEvent<Boolean>()
    val joinSucceed: LiveData<Boolean> get() = _joinSucceed
```

- [ ] **Step 2: `createNewProfile` 과 `goToLogin` 을 교체**

같은 파일 71~117행의 두 private 함수를 아래로 교체한다.

```kotlin
    private fun createNewProfile(
        emailInfo: GoogleEmailInfo,
        nickName: String,
        bio: String? = null,
        image: File? = null
    ) {
        Log.e("ayhan", "createNewProfile called :$emailInfo")
        viewModelScope.launch(ceh(_failJoin, true)) {
            _failJoin.value = false

            val res = createProfile(
                CreateProfileRequest(
                    email = emailInfo.email,
                    uid = emailInfo.uid,
                    name = nickName,
                    bio = bio,
                    profileImage = image
                )
            )
            Log.e("ayhan", "createNewProfile success : $res")
            if (res.code == "6001") {
                _isAlreadyUsedNickName.value = true
            } else if (res.success) {
                goToLogin(
                    emailInfo = emailInfo,
                    isMyBuryUser = res.data?.isMyBuryUser() == true
                )
            } else {
                Log.e("ayhan", "createNewProfile fail : $res")
                _failJoin.value = true
            }
        }
    }

    private fun goToLogin(emailInfo: GoogleEmailInfo, isMyBuryUser: Boolean) {
        viewModelScope.launch(ceh(_failJoin, true)) {
            val res = loginByEmail(emailInfo.uid)
            if (res.success) {
                preferenceDataStoreModule.setLoginEmail(emailInfo.email)
                preferenceDataStoreModule.setLoginEmailUid(emailInfo.uid)
                res.data.accessToken.let { token ->
                    preferenceDataStoreModule.setAccessToken("Bearer $token")
                }
                // accessToken 저장 이후에 발화해야 한다. 이관 API 가 인증을 요구한다.
                _joinSucceed.value = isMyBuryUser
            } else {
                _failJoin.value = true
            }
        }
    }
```

**주의:** 기존 `createNewProfile` 에 있던 `_goToLogin.value = false` 줄은 **반드시 삭제**한다. 이제 이벤트 값이 `isMyBuryUser` 를 의미하므로, `false` 를 미리 넣으면 네트워크 호출 전에 "가입 완료(마이버리 회원 아님)" 로 오해되어 메인으로 곧장 넘어가 버린다. 위 코드는 이미 그 줄이 빠진 상태다.

- [ ] **Step 3: `JoinCreateProfile2` 의 콜백 시그니처 변경**

`JoinCreateProfile2.kt` 61~86행을 아래로 교체한다.

교체 전:

```kotlin
@Composable
fun JoinCreateProfile2(
    emailInfo: GoogleEmailInfo,
    createProfileInfo: CreateProfileInfo,
    goToMain: () -> Unit,
    goToBack: () -> Unit
) {
    val createUserViewModel: JoinNickNameViewModel = hiltViewModel()

    val failJoinAsState by createUserViewModel.failJoin.observeAsState()
    val goToLoginAsState by createUserViewModel.goToLogin.observeAsState()
    val isAlreadyUsedNickNameAsState by createUserViewModel.isAlreadyUsedNickName.observeAsState()

    var showErrorPopup by remember { mutableStateOf(false) }
    LaunchedEffect(goToLoginAsState) {
        if (goToLoginAsState == true) {
            goToMain()
        }
    }
```

교체 후:

```kotlin
@Composable
fun JoinCreateProfile2(
    emailInfo: GoogleEmailInfo,
    createProfileInfo: CreateProfileInfo,
    goToMain: (isMyBuryUser: Boolean) -> Unit,
    goToBack: () -> Unit
) {
    val createUserViewModel: JoinNickNameViewModel = hiltViewModel()

    val failJoinAsState by createUserViewModel.failJoin.observeAsState()
    val joinSucceedAsState by createUserViewModel.joinSucceed.observeAsState()
    val isAlreadyUsedNickNameAsState by createUserViewModel.isAlreadyUsedNickName.observeAsState()

    var showErrorPopup by remember { mutableStateOf(false) }
    LaunchedEffect(joinSucceedAsState) {
        joinSucceedAsState?.let { isMyBuryUser -> goToMain(isMyBuryUser) }
    }
```

`joinSucceed` 는 `SingleLiveEvent` 이므로 초기값이 `null` 이고 값이 설정될 때 한 번만 발화한다. 따라서 `?.let` 으로 null 을 걸러내면 된다.

- [ ] **Step 4: `JoinScreen` 에 분기 추가**

`JoinScreen.kt` 의 `JoinScreen` 함수 본문(21~77행)을 아래로 교체한다.

풀스크린인 `MyBuryConnectScreen` 이 단계 화면 위에 겹쳐 그려지도록 전체를 `Box` 로 감싼다. 감싸지 않으면 부모 컨테이너에 따라 아래로 밀려 쌓일 수 있다.

```kotlin
) {

    var emailLoginSucceed by remember {
        mutableStateOf(false)
    }

    var isFirstCreate by remember { mutableStateOf(false) }
    var createProfileInfo by remember { mutableStateOf(CreateProfileInfo()) }

    Log.e("ayhan", "createProfileInfo : $createProfileInfo")

    val joinTryEmail: MutableState<GoogleEmailInfo?> = remember { mutableStateOf(null) }

    var showMyBuryConnect by remember { mutableStateOf(false) }
    var showBadgePopup by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (emailLoginSucceed.not()) {
            JoinEmailScreen(goToNexPage = {
                joinTryEmail.value = it
                emailLoginSucceed = true
            }, goToBack = {
                goToBack()
            }, goToLogin = goToLogin)
        } else if (isFirstCreate.not()) {
            JoinCreateProfile1(
                createProfileInfo = createProfileInfo,
                goToNext = { info ->
                    isFirstCreate = true
                    createProfileInfo = info
                },
                addImageAction = addImageAction
            )
        } else if (isFirstCreate) {
            joinTryEmail.value?.let {
                JoinCreateProfile2(
                    emailInfo = it,
                    createProfileInfo = createProfileInfo,
                    goToMain = { isMyBuryUser ->
                        // 마이버리 기존 회원이면 데이터 연결 화면을 먼저 보여준다.
                        if (isMyBuryUser) {
                            showMyBuryConnect = true
                        } else {
                            showBadgePopup = true
                        }
                    },
                    goToBack = {
                        isFirstCreate = false
                    }
                )
            }
        }

        if (showMyBuryConnect) {
            MyBuryConnectScreen(
                onFinished = {
                    showMyBuryConnect = false
                    showBadgePopup = true
                }
            )
        }
    }

    if (showBadgePopup) {
        WelcomePopupScreen(
            gotoStart = {
                goToMain()
            },
            goToBadgeInfo = {
                goToBadgeInfo()
            }
        )
    }
}
```

같은 파일 상단 import 에 추가한다.

```kotlin
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
```

`WelcomePopupScreen` 은 `Dialog` 이므로 `Box` 밖에 두어도 자연스럽게 위에 뜬다.

- [ ] **Step 5: 빌드 확인**

Run: `./gradlew :app:assembleDebug`

Expected: BUILD SUCCESSFUL

- [ ] **Step 6: 전체 테스트 확인**

Run: `./gradlew :common:test`

Expected: Task 1, 2 에서 추가한 11개 테스트 전부 PASS

- [ ] **Step 7: 실기기/에뮬레이터 수동 확인**

서버가 `myburyYn: "Y"` 를 내려주는 계정으로 신규 가입을 진행해 확인한다.

1. 가입 완료 → 데이터 연결 화면이 뜨는지
2. "웨이버에서 이어서 진행하기" → "데이터 이관을 진행합니다." 팝업 → 확인 → 환영 팝업 → 메인
3. 같은 흐름을 다시 요청하면 `8202` 로 "이미 데이터 이관 진행중입니다." 가 뜨는지
4. "새롭게 시작하기" 와 X 닫기 → 이관 요청 없이 바로 환영 팝업 → 메인
5. `myburyYn: "N"` 계정으로 가입 → 데이터 연결 화면 없이 기존과 동일하게 환영 팝업 → 메인

`myburyYn: "Y"` 계정을 준비하기 어려우면 `JoinNickNameViewModel.goToLogin` 의 `_joinSucceed.value = isMyBuryUser` 를 임시로 `_joinSucceed.value = true` 로 바꿔 화면과 팝업 흐름만 먼저 확인하고, **확인 후 반드시 되돌린다.**

- [ ] **Step 8: 커밋**

```bash
git add app/src/main/java/com/zinc/waver/ui/presentation/login/JoinNickNameViewModel.kt \
        app/src/main/java/com/zinc/waver/ui/presentation/login/JoinCreateProfile2.kt \
        app/src/main/java/com/zinc/waver/ui/presentation/login/JoinScreen.kt
git commit -m "[가입] 마이버리 기존 회원에게 데이터 연결 화면 노출"
```

---

## 범위에서 제외

스펙과 동일하다.

- 이관 진행 상황 조회/표시 (서버 스케줄러가 비동기 처리, 조회 API 없음)
- 가입 이후 마이페이지 등에서 이관을 다시 요청하는 진입점
- 마이버리 앱 쪽 변경
