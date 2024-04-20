package com.zinc.berrybucket.ui_other.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zinc.berrybucket.ui.viewmodel.CommonViewModel
import com.zinc.common.models.OtherProfileHomeData
import com.zinc.datastore.login.LoginPreferenceDataStoreModule
import com.zinc.domain.usecases.other.LoadOtherInfo
import com.zinc.domain.usecases.other.RequestFollowUser
import com.zinc.domain.usecases.other.RequestUnfollowUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OtherViewModel @Inject constructor(
    private val loadOtherInfo: LoadOtherInfo,
    private val followUser: RequestFollowUser,
    private val unfollowUser: RequestUnfollowUser,
    loginPreferenceDataStoreModule: LoginPreferenceDataStoreModule
) : CommonViewModel(loginPreferenceDataStoreModule) {

    private val _loadFail = MutableLiveData<Boolean>()
    val loadFail: LiveData<Boolean> get() = _loadFail

    private val _otherHomeData = MutableLiveData<OtherProfileHomeData>()
    val otherHomeData: LiveData<OtherProfileHomeData> get() = _otherHomeData

    fun loadOtherInfo(userId: String) {
        _loadFail.value = false
        viewModelScope.launch(CEH(_loadFail, true)) {
            runCatching {
                accessToken.value?.let { token ->
                    val result = loadOtherInfo(token, userId)
                    Log.e("ayhan", "loadOtherInfo : $result")
                    if (result == null || !result.isSuccess || result.data == null) {
                        _loadFail.value = true
                    } else {
                        _otherHomeData.value = result.data
                    }
                }
            }.getOrElse {
                _loadFail.value = true
            }
        }
    }

    fun changeFollowStatus(userId: String, follow: Boolean) {
        viewModelScope.launch(CEH(_loadFail, true)) {
            runCatching {
                accessToken.value?.let { token ->
                    if (follow) {
                        val result = followUser.invoke(token, userId)
                        if (result.success) {
                            updateProfile(true)
                            _loadFail.value = false
                        } else {
                            _loadFail.value = true
                        }
                    } else {
                        val result = unfollowUser.invoke(token, userId)
                        if (result.success) {
                            updateProfile(false)
                            _loadFail.value = false
                        } else {
                            _loadFail.value = true
                        }
                    }
                }
            }
        }
    }

    private fun updateProfile(updateFollow: Boolean) {
        if (otherHomeData.value == null) return

        val topProfile = otherHomeData.value!!.profile
        val updateProfile = topProfile.copy(isFollowed = updateFollow)
        val updateData = otherHomeData.value?.copy(profile = updateProfile)

        _otherHomeData.value = updateData
    }
}