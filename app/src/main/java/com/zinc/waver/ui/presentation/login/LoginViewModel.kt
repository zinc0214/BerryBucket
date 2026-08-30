package com.zinc.waver.ui.presentation.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zinc.datastore.login.PreferenceDataStoreModule
import com.zinc.domain.usecases.login.LoginByEmail
import com.zinc.waver.ui.viewmodel.CommonViewModel
import com.zinc.waver.util.FcmTokenRegister
import com.zinc.waver.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginByEmail: LoginByEmail,
    private val preferenceDataStoreModule: PreferenceDataStoreModule,
    private val fcmTokenRegister: FcmTokenRegister,
) : CommonViewModel() {

    private val _loginFail = SingleLiveEvent<Boolean>()
    val loginFail: LiveData<Boolean> get() = _loginFail

    private val _needToStartJoin = SingleLiveEvent<Boolean>()
    val needToStartJoin: LiveData<Boolean> get() = _needToStartJoin

    private val _needToStartLoadToken = SingleLiveEvent<String>()
    val needToStartLoadToken: LiveData<String> get() = _needToStartLoadToken

    var isLoginChecked = false

    private val _goToMain = MutableLiveData<Boolean>()
    val goToMain: LiveData<Boolean> get() = _goToMain

    fun checkHasLoginEmail() {
        isLoginChecked = true
        _needToStartJoin.value = false
        viewModelScope.launch {
            // 한 번만 확인하면 되는 값이다. collect 로 계속 구독하면 이후 DataStore 쓰기마다 다시 발화한다.
            val savedUid = preferenceDataStoreModule.loadLoginedEmailUid.first()
            if (savedUid.isNotEmpty()) {
                _needToStartLoadToken.value = savedUid
            } else {
                // uid 가 없으면 로그인할 수단이 없다. 빈 uid 로 토큰을 요청하면 반드시 실패한다.
                _needToStartJoin.value = true
            }
        }
    }

    fun loadLoginToken(emailUid: String) {
        _loginFail.value = false
        viewModelScope.launch(ceh(_loginFail, true)) {
            val result = loginByEmail(emailUid)
            // 성공 여부를 확인하기 전에 data 를 건드리지 않는다. 실패 응답에는 data 가 없다.
            val token = result.data?.accessToken
            if (result.success && token != null) {
                preferenceDataStoreModule.setAccessToken("Bearer $token")
                preferenceDataStoreModule.setLoginEmailUid(emailUid)
                // accessToken 저장 이후여야 한다. TokenInterceptor 가 DataStore 에서 읽어 헤더를 붙인다.
                fcmTokenRegister.register()
                _goToMain.value = true
            } else {
                Log.e("ayhan", "loadLoginToken fail : code=${result.code}, message=${result.message}")
                _loginFail.value = true
            }
        }
    }
}
