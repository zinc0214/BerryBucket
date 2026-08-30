package com.zinc.waver.ui.presentation.login

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.zinc.common.models.AccessTokenDto
import com.zinc.common.models.CheckUserStatusRequest
import com.zinc.common.models.CheckUserStatusResponse
import com.zinc.common.models.CommonResponse
import com.zinc.common.models.CommonResponse2
import com.zinc.common.models.CreateProfileRequest
import com.zinc.common.models.CheckUserLimitResponse
import com.zinc.common.models.JoinResponse
import com.zinc.common.models.LoadMyWaveBadgeResponse
import com.zinc.common.models.LoadMyWaveInfoResponse
import com.zinc.common.models.LoadTokenByEmailRequest
import com.zinc.common.models.LoadTokenByEmailResponse
import com.zinc.common.models.ProfileResponse
import com.zinc.common.models.RefreshTokenResponse
import com.zinc.datastore.login.PreferenceDataStoreModule
import com.zinc.domain.models.GoogleEmailInfo
import com.zinc.domain.models.SubscriptionStartRequest
import com.zinc.domain.models.UpdateProfileRequest
import com.zinc.domain.repository.LoginRepository
import com.zinc.domain.repository.MoreRepository
import com.zinc.domain.usecases.login.CreateProfile
import com.zinc.domain.usecases.login.LoginByEmail
import com.zinc.domain.usecases.more.CheckAlreadyUsedNickname
import com.zinc.waver.ui.presentation.login.model.NicknameCheckState
import com.zinc.waver.util.FcmTokenRegister
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

/**
 * 가입 2단계 뷰모델. 닉네임 중복 검사 상태 전이와 가입 실패 처리를 검증한다.
 *
 * DataStore / FCM 은 이 테스트가 지나는 경로에서 호출되지 않으므로 stub 없이 mock 만 넘긴다.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class JoinNickNameViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val dispatcher = UnconfinedTestDispatcher()

    private lateinit var moreRepository: FakeMoreRepository
    private lateinit var loginRepository: FakeLoginRepository
    private lateinit var viewModel: JoinNickNameViewModel

    private val emailInfo = GoogleEmailInfo(email = "waver@test.com", uid = "uid-1")

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        moreRepository = FakeMoreRepository()
        loginRepository = FakeLoginRepository()
        viewModel = JoinNickNameViewModel(
            createProfile = CreateProfile(loginRepository),
            loginByEmail = LoginByEmail(loginRepository),
            checkAlreadyUsedNickname = CheckAlreadyUsedNickname(moreRepository),
            preferenceDataStoreModule = mock(PreferenceDataStoreModule::class.java),
            fcmTokenRegister = mock(FcmTokenRegister::class.java),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `사용 가능한 닉네임은 검사한 닉네임과 함께 Available 로 끝난다`() = runTest {
        moreRepository.nicknameResult = success()

        viewModel.checkIsAlreadyUsedName("웨이버")

        assertEquals(NicknameCheckState.Available("웨이버"), viewModel.nicknameCheckState.value)
    }

    @Test
    fun `이미 쓰는 닉네임은 Duplicated 로 끝난다`() = runTest {
        moreRepository.nicknameResult = fail("6001")

        viewModel.checkIsAlreadyUsedName("웨이버")

        assertEquals(NicknameCheckState.Duplicated("웨이버"), viewModel.nicknameCheckState.value)
    }

    @Test
    fun `6001 이 아닌 실패는 Failed 로 끝난다`() = runTest {
        moreRepository.nicknameResult = fail("5000")

        viewModel.checkIsAlreadyUsedName("웨이버")

        assertEquals(NicknameCheckState.Failed, viewModel.nicknameCheckState.value)
    }

    /**
     * 중복 판정 뒤 다른 닉네임으로 다시 검사하면 그 닉네임의 Available 이 남아야 한다.
     * 예전 구현은 결과 사이에 null 을 끼워넣어(delay 100ms) 화면이 대기 중이던 입력값을 잃어버렸다.
     */
    @Test
    fun `중복 판정 뒤 다른 닉네임으로 재검사하면 새 닉네임이 통과한다`() = runTest {
        moreRepository.nicknameResult = fail("6001")
        viewModel.checkIsAlreadyUsedName("중복닉네임")
        assertEquals(NicknameCheckState.Duplicated("중복닉네임"), viewModel.nicknameCheckState.value)

        moreRepository.nicknameResult = success()
        viewModel.checkIsAlreadyUsedName("새닉네임")

        assertEquals(NicknameCheckState.Available("새닉네임"), viewModel.nicknameCheckState.value)
    }

    /**
     * 검사 결과는 "언젠가 통과했다"가 아니라 "이 닉네임이 통과했다"여야 한다.
     * 이걸 구분하지 못하면 2단계에서 뒤로 온 뒤 닉네임을 바꿔도 검사 없이 통과한다.
     */
    @Test
    fun `통과한 닉네임이 아니면 통과 상태로 보지 않는다`() = runTest {
        moreRepository.nicknameResult = success()
        viewModel.checkIsAlreadyUsedName("통과한닉네임")

        val state = viewModel.nicknameCheckState.value!!
        assertEquals(true, state.isAvailableFor("통과한닉네임"))
        assertEquals(false, state.isAvailableFor("바꾼닉네임"))
    }

    @Test
    fun `검사가 끝나기 전에는 같은 요청을 다시 보내지 않는다`() = runTest {
        // 응답을 붙잡아 두어 실제로 "검사 중"인 상태를 만든다.
        val gate = CompletableDeferred<Unit>()
        moreRepository.gate = gate
        moreRepository.nicknameResult = success()

        viewModel.checkIsAlreadyUsedName("웨이버")
        assertEquals(NicknameCheckState.Checking, viewModel.nicknameCheckState.value)

        viewModel.checkIsAlreadyUsedName("웨이버")
        assertEquals(1, moreRepository.nicknameCallCount)

        gate.complete(Unit)
        assertEquals(NicknameCheckState.Available("웨이버"), viewModel.nicknameCheckState.value)
    }

    @Test
    fun `가입 요청이 6001 로 막히면 그 닉네임이 Duplicated 가 된다`() = runTest {
        loginRepository.joinResult = JoinResponse(
            success = false, code = "6001", message = "duplicated", data = null
        )

        viewModel.join(emailInfo = emailInfo, nickName = "웨이버")

        assertEquals(NicknameCheckState.Duplicated("웨이버"), viewModel.nicknameCheckState.value)
    }

    @Test
    fun `가입 요청이 실패하면 failJoin 이 뜬다`() = runTest {
        loginRepository.joinResult = JoinResponse(
            success = false, code = "5000", message = "error", data = null
        )

        viewModel.join(emailInfo = emailInfo, nickName = "웨이버")

        assertEquals(true, viewModel.failJoin.value)
    }

    /**
     * 가입은 됐지만 로그인 응답에 accessToken 이 없는 경우.
     * data 를 논널로 두면 여기서 NPE 가 나 실패 안내 대신 크래시로 끝난다.
     */
    @Test
    fun `로그인 응답에 data 가 없으면 크래시 대신 failJoin 이 뜬다`() = runTest {
        loginRepository.joinResult = JoinResponse(
            success = true, code = "200", message = "ok", data = null
        )
        loginRepository.loginResult = LoadTokenByEmailResponse(
            data = null, success = true, code = "200", message = "ok"
        )

        viewModel.join(emailInfo = emailInfo, nickName = "웨이버")

        assertEquals(true, viewModel.failJoin.value)
    }

    private fun success() = CommonResponse2(success = true, code = "200", message = "ok")

    private fun fail(code: String) =
        CommonResponse2(success = false, code = code, message = "fail")

    private class FakeMoreRepository : MoreRepository {
        var nicknameResult = CommonResponse2(success = true, code = "200", message = "ok")
        var nicknameCallCount = 0

        /** 세팅하면 응답이 여기서 멈춘다. 검사 중 상태를 만들기 위한 장치. */
        var gate: CompletableDeferred<Unit>? = null

        override suspend fun checkAlreadyUsedNickName(name: String): CommonResponse2 {
            nicknameCallCount++
            gate?.await()
            return nicknameResult
        }

        override suspend fun loadProfileInfo(): ProfileResponse = notUsed()
        override suspend fun updateProfileInfo(request: UpdateProfileRequest): CommonResponse =
            notUsed()

        override suspend fun loadMyBadgeInfo(): LoadMyWaveBadgeResponse = notUsed()
        override suspend fun loadMyWaveInfo(): LoadMyWaveInfoResponse = notUsed()
        override suspend fun requestWithdrawal(): CommonResponse = notUsed()
        override suspend fun updateMyBadge(badgeId: Int): CommonResponse = notUsed()
        override suspend fun startSubscription(request: SubscriptionStartRequest): CommonResponse =
            notUsed()

        override suspend fun checkUserLimit(): CheckUserLimitResponse = notUsed()
    }

    private class FakeLoginRepository : LoginRepository {
        var joinResult = JoinResponse(success = true, code = "200", message = "ok", data = null)
        var loginResult = LoadTokenByEmailResponse(
            data = AccessTokenDto("token"), success = true, code = "200", message = "ok"
        )

        override suspend fun createProfile(createProfileRequest: CreateProfileRequest) = joinResult

        override suspend fun requestLogin(loginRequest: LoadTokenByEmailRequest) = loginResult

        override suspend fun refreshToken(): RefreshTokenResponse = notUsed()
        override suspend fun checkUserStatus(
            checkUserStatusRequest: CheckUserStatusRequest
        ): CheckUserStatusResponse = notUsed()

        override suspend fun requestMyBuryMigration(): CommonResponse2 = notUsed()
        override suspend fun updateFcmToken(token: String): CommonResponse2 = notUsed()
    }
}

private fun notUsed(): Nothing = throw UnsupportedOperationException("이 테스트에서는 호출되지 않는다")
