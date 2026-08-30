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
            // 한 번만 읽으면 안 된다. 가입 화면에서 "로그인하러 가기"를 누르면 uid 가 그때 저장되는데,
            // 이 구독이 그 값을 받아 로그인으로 이어주는 유일한 경로다.
            preferenceDataStoreModule.loadLoginedEmailUid.collect { savedUid ->
                if (savedUid.isNotEmpty()) {
                    _needToStartJoin.value = false
                    _needToStartLoadToken.value = savedUid
                } else {
                    // uid 가 없으면 로그인할 수단이 없다. 빈 uid 로 토큰을 요청하면 반드시 실패한다.
                    _needToStartJoin.value = true
                }
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
