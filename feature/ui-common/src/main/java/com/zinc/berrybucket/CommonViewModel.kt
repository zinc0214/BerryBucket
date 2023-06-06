package com.zinc.berrybucket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zinc.berrybucket.util.SingleLiveEvent
import com.zinc.datastore.login.LoginPreferenceDataStoreModule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
open class CommonViewModel @Inject constructor(
    private val loginPreferenceDataStoreModule: LoginPreferenceDataStoreModule
) : ViewModel() {

    var accessToken = SingleLiveEvent<String>()
    var refreshToken = SingleLiveEvent<String>()

    init {
        loadToken()
    }

    fun loadToken() {
        viewModelScope.launch {
            accessToken.value =
                "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjQsImlhdCI6MTY3ODQ2MTgwMywiZXhwIjoxNzQ1NzI2MTgwM30.RG-TKPJR3UbLBXD-O9269gyNLv21G9KIBP1Q6SNaeCU"
//            loginPreferenceDataStoreModule.loadAccessToken.collectLatest {
//                accessToken.value = it
//            }
//
//            loginPreferenceDataStoreModule.loadRefreshToken.collectLatest {
//                refreshToken.value = it
//            }
        }
    }
}