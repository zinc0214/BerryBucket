package com.zinc.waver.ui.presentation.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zinc.common.models.CheckUserStatusRequest
import com.zinc.common.models.CheckUserStatusResponse
import com.zinc.datastore.login.PreferenceDataStoreModule
import com.zinc.domain.models.GoogleEmailInfo
import com.zinc.domain.usecases.login.CheckUserStatus
import com.zinc.domain.usecases.login.CreateProfile
import com.zinc.domain.usecases.login.LoginByEmail
import com.zinc.waver.ui.viewmodel.CommonViewModel
import com.zinc.waver.util.FcmTokenRegister
import com.zinc.waver.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JoinEmailViewModel @Inject constructor(
    private val loginByEmail: LoginByEmail,
    private val createProfile: CreateProfile,
    private val checkUserState: CheckUserStatus,
    private val preferenceDataStoreModule: PreferenceDataStoreModule,
    private val fcmTokenRegister: FcmTokenRegister,
) : CommonViewModel() {

    private val _failEmailCheck = SingleLiveEvent<Boolean>()
    val failEmailCheck: LiveData<Boolean> get() = _failEmailCheck

    private val _goToMakeNickName = SingleLiveEvent<GoogleEmailInfo>()
    val goToMakeNickName: LiveData<GoogleEmailInfo> get() = _goToMakeNickName

    private val _isAlreadyUsedEmail = SingleLiveEvent<Boolean>()
    val isAlreadyUsedEmail: LiveData<Boolean> get() = _isAlreadyUsedEmail

    private val _isDeletedUser = MutableLiveData<Boolean>()
    val isDeletedUser: LiveData<Boolean> get() = _isDeletedUser

    private val _goToLogin = MutableLiveData<Boolean>()
    val goToLogin: LiveData<Boolean> get() = _goToLogin

    fun goToLogin(emailInfo: GoogleEmailInfo) {
        viewModelScope.launch(ceh(_failEmailCheck, true)) {
            _failEmailCheck.value = false
            val res = loginByEmail(emailInfo.uid)
            if (res.success) {
                res.data.accessToken.let { token ->
                    preferenceDataStoreModule.setAccessToken("Bearer $token")
                }
                // accessToken 저장 이후여야 한다. TokenInterceptor 가 DataStore 에서 읽어 헤더를 붙인다.
                fcmTokenRegister.register()
                _isAlreadyUsedEmail.value = true
            } else {
                _goToMakeNickName.value = emailInfo
            }
        }
    }

    fun checkUserStatus(emailInfo: GoogleEmailInfo) {
        viewModelScope.launch(ceh(_failEmailCheck, true)) {
            _failEmailCheck.value = false
            val request = CheckUserStatusRequest(
                uid = emailInfo.uid,
                email = emailInfo.email
            )
            val res = checkUserState(request)
            if (res.success) {
                when (res.data.status) {
                    CheckUserStatusResponse.Status.ACTIVE -> {
                        _isAlreadyUsedEmail.value = true
                    }

                    CheckUserStatusResponse.Status.WITHDRAWN -> {
                        _isDeletedUser.value = true
                    }
                }
            } else {
                goToLogin(emailInfo)
            }
        }
    }

    fun savedLoginEmail(emailInfo: GoogleEmailInfo) {
        viewModelScope.launch {
            preferenceDataStoreModule.setLoginEmail(emailInfo.email)
            preferenceDataStoreModule.setLoginEmailUid(emailInfo.uid)
            _goToLogin.value = true
        }
    }
}