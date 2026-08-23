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
import kotlinx.coroutines.flow.collectLatest
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

    fun checkHasLoginEmail(retryEmail: String) {
        isLoginChecked = true
        _needToStartJoin.value = false
        viewModelScope.launch {
            preferenceDataStoreModule.loadLoginedEmailUid.collectLatest {

                _needToStartJoin.value = false

                Log.e("ayhan", "checkHasLoginEmail : $it")
                if (it.isNotEmpty()) {
                    _needToStartLoadToken.value = it
                    _needToStartJoin.value = false
                } else if (retryEmail.isNotEmpty()) {
                    _needToStartLoadToken.value = it
                    _needToStartJoin.value = false
                } else {
                    _needToStartJoin.value = true
                }
            }
        }
    }

    fun loadLoginToken(emailUid: String) {
        Log.e("ayhan", "emailUid : $emailUid")
        _loginFail.value = false
        viewModelScope.launch(ceh(_loginFail, true)) {
            val result = loginByEmail(emailUid)
            Log.e("ayhan", "loadUserToken result : ${result.data.accessToken}")
            if (result.success) {
                val data = result.data.accessToken
                preferenceDataStoreModule.setAccessToken("Bearer $data")
                preferenceDataStoreModule.setLoginEmailUid(emailUid)
                // accessToken 저장 이후여야 한다. TokenInterceptor 가 DataStore 에서 읽어 헤더를 붙인다.
                fcmTokenRegister.register()
                _goToMain.value = true
            } else {
                _loginFail.value = true
            }
        }
    }
}
