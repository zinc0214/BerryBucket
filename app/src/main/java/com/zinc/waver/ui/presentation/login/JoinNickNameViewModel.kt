package com.zinc.waver.ui.presentation.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zinc.common.models.CreateProfileRequest
import com.zinc.datastore.login.PreferenceDataStoreModule
import com.zinc.domain.models.GoogleEmailInfo
import com.zinc.domain.usecases.login.CreateProfile
import com.zinc.domain.usecases.login.LoginByEmail
import com.zinc.domain.usecases.more.CheckAlreadyUsedNickname
import com.zinc.waver.ui.presentation.login.model.NicknameCheckState
import com.zinc.waver.ui.viewmodel.CommonViewModel
import com.zinc.waver.util.FcmTokenRegister
import com.zinc.waver.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class JoinNickNameViewModel @Inject constructor(
    private val createProfile: CreateProfile,
    private val loginByEmail: LoginByEmail,
    private val checkAlreadyUsedNickname: CheckAlreadyUsedNickname,
    private val preferenceDataStoreModule: PreferenceDataStoreModule,
    private val fcmTokenRegister: FcmTokenRegister,
) : CommonViewModel() {

    // 검사한 닉네임을 결과에 함께 담으므로, 값을 null 로 되돌려 옵저버를 다시 깨우는 우회가 필요 없다.
    private val _nicknameCheckState = MutableLiveData<NicknameCheckState>(NicknameCheckState.Idle)
    val nicknameCheckState: LiveData<NicknameCheckState> get() = _nicknameCheckState

    // 발화 시점 = 가입 + 로그인 + accessToken 저장까지 모두 끝난 뒤. 값은 마이버리 기존 회원 여부.
    private val _joinSucceed = SingleLiveEvent<Boolean>()
    val joinSucceed: LiveData<Boolean> get() = _joinSucceed

    private val _failJoin = SingleLiveEvent<Boolean>()
    val failJoin: LiveData<Boolean> get() = _failJoin

    fun join(
        emailInfo: GoogleEmailInfo,
        nickName: String,
        bio: String? = null,
        image: File? = null
    ) {
        viewModelScope.launch(ceh(_failJoin, true)) {
            _failJoin.value = false
            createNewProfile(emailInfo, nickName, bio, image)
        }
    }

    fun checkIsAlreadyUsedName(name: String) {
        if (_nicknameCheckState.value == NicknameCheckState.Checking) return

        _nicknameCheckState.value = NicknameCheckState.Checking
        viewModelScope.launch(ceh(_nicknameCheckState, NicknameCheckState.Failed)) {
            val res = checkAlreadyUsedNickname.invoke(name)
            _nicknameCheckState.value = when {
                res.success -> NicknameCheckState.Available(name)
                res.code == DUPLICATED_NICKNAME_CODE -> NicknameCheckState.Duplicated(name)
                else -> NicknameCheckState.Failed
            }
        }
    }

    private fun createNewProfile(
        emailInfo: GoogleEmailInfo,
        nickName: String,
        bio: String? = null,
        image: File? = null
    ) {
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
            if (res.code == DUPLICATED_NICKNAME_CODE) {
                // 중복 검사를 통과한 뒤에도 서버가 막을 수 있다(그 사이 선점). 1단계와 같은 상태로 되돌린다.
                _nicknameCheckState.value = NicknameCheckState.Duplicated(nickName)
            } else if (res.success) {
                goToLogin(
                    emailInfo = emailInfo,
                    isMyBuryUser = res.data?.isMyBuryUser() == true
                )
            } else {
                _failJoin.value = true
            }
        }
    }

    private fun goToLogin(emailInfo: GoogleEmailInfo, isMyBuryUser: Boolean) {
        viewModelScope.launch(ceh(_failJoin, true)) {
            val res = loginByEmail(emailInfo.uid)
            val token = res.data?.accessToken
            if (res.success && token != null) {
                preferenceDataStoreModule.setLoginEmail(emailInfo.email)
                preferenceDataStoreModule.setLoginEmailUid(emailInfo.uid)
                preferenceDataStoreModule.setAccessToken("Bearer $token")
                // accessToken 저장 이후여야 한다. TokenInterceptor 가 DataStore 에서 읽어 헤더를 붙인다.
                fcmTokenRegister.register()
                // accessToken 저장 이후에 발화해야 한다. 이관 API 가 인증을 요구한다.
                _joinSucceed.value = isMyBuryUser
            } else {
                _failJoin.value = true
            }
        }
    }

    companion object {
        private const val DUPLICATED_NICKNAME_CODE = "6001"
    }
}
