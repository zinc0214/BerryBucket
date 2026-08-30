package com.zinc.waver.ui_more.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zinc.common.models.ProfileInfo
import com.zinc.common.utils.TAG
import com.zinc.datastore.login.PreferenceDataStoreModule
import com.zinc.domain.models.UpdateProfileRequest
import com.zinc.domain.usecases.more.CheckAlreadyUsedNickname
import com.zinc.domain.usecases.more.CheckUserLimit
import com.zinc.domain.usecases.more.LoadProfileInfo
import com.zinc.domain.usecases.more.UpdateProfileInfo
import com.zinc.waver.ui.presentation.model.NicknameCheckState
import com.zinc.waver.ui.viewmodel.CommonViewModel
import com.zinc.waver.ui_more.models.UIMoreMyProfileInfo
import com.zinc.waver.ui_more.models.toUi
import com.zinc.waver.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class MoreViewModel @Inject constructor(
    private val loadProfileInfo: LoadProfileInfo,
    private val updateProfileInfo: UpdateProfileInfo,
    private val checkAlreadyUsedNickname: CheckAlreadyUsedNickname,
    private val checkUserLimit: CheckUserLimit,
    private val preferenceDataStoreModule: PreferenceDataStoreModule,
) : CommonViewModel() {

    private val _profileInfo = MutableLiveData<UIMoreMyProfileInfo>()
    val profileInfo: LiveData<UIMoreMyProfileInfo> get() = _profileInfo

    private val _profileLoadFail = SingleLiveEvent<Boolean>()
    val profileLoadFail: LiveData<Boolean> get() = _profileLoadFail

    private val _profileUpdateFail = SingleLiveEvent<Boolean>()
    val profileUpdateFail: LiveData<Boolean> get() = _profileUpdateFail

    private val _profileUpdateSucceed = SingleLiveEvent<Boolean>()
    val profileUpdateSucceed: LiveData<Boolean> get() = _profileUpdateSucceed

    // 검사한 닉네임을 결과에 함께 담는다. 자세한 이유는 NicknameCheckState 참고.
    private val _nicknameCheckState = MutableLiveData<NicknameCheckState>(NicknameCheckState.Idle)
    val nicknameCheckState: LiveData<NicknameCheckState> get() = _nicknameCheckState

    private val _hasWaverPlus = MutableLiveData<Boolean>()
    val hasWaverPlus: LiveData<Boolean> get() = _hasWaverPlus

    private val _loginEmail = MutableLiveData<String>()
    val loginEmail: LiveData<String> get() = _loginEmail

    fun loadMyProfile() {
        viewModelScope.launch(ceh(_profileLoadFail, true)) {
            loadProfileInfo.invoke().apply {
                Log.e(TAG, "loadMyProfile: $this")
                if (success) {
                    _profileInfo.value = data.toUi()
                } else {
                    _profileLoadFail.value = true
                }
            }
        }
    }

    /**
     * 프로필을 수정한다. [needNicknameCheck] 면 닉네임 중복 검사를 먼저 통과해야 한다.
     *
     * 검사와 수정을 한 흐름으로 묶는다. 검사 결과를 LiveData 로 내보내 화면이 그걸 보고 수정을
     * 호출하는 구조였을 때는, 화면에 재진입하면 남아있던 지난 결과가 저장 버튼을 누르지도 않았는데
     * 수정 요청을 발화시켰다.
     */
    fun updateMyProfile(
        name: String,
        bio: String,
        profileImage: File? = null,
        needNicknameCheck: Boolean = false
    ) {
        viewModelScope.launch(ceh(_profileUpdateFail, true)) {
            // 먼저 내려두지 않으면 두 번 연속 실패했을 때 값이 true 로 같아 화면이 변화를 못 본다.
            _profileUpdateFail.value = false

            if (needNicknameCheck) {
                when (checkNickname(name)) {
                    // 중복은 닉네임 입력란에 인라인으로 표시된다. 다이얼로그까지 띄우지 않는다.
                    is NicknameCheckState.Duplicated -> return@launch
                    is NicknameCheckState.Available -> Unit
                    // 검사 자체가 실패하면 저장도 못 한 것이므로 실패로 알린다.
                    else -> {
                        _profileUpdateFail.value = true
                        return@launch
                    }
                }
            }

            val request = UpdateProfileRequest(
                name = name,
                bio = bio,
                image = profileImage
            )
            updateProfileInfo.invoke(request).apply {
                if (success) {
                    _profileUpdateSucceed.value = true
                } else {
                    _profileUpdateFail.value = true
                }
            }
        }
    }

    /** 닉네임 중복을 검사하고 결과를 [nicknameCheckState] 로 내보낸 뒤 그대로 돌려준다. */
    private suspend fun checkNickname(name: String): NicknameCheckState {
        _nicknameCheckState.value = NicknameCheckState.Checking
        val res = checkAlreadyUsedNickname.invoke(name)
        return when {
            res.success -> NicknameCheckState.Available(name)
            res.code == DUPLICATED_NICKNAME_CODE -> NicknameCheckState.Duplicated(name)
            else -> NicknameCheckState.Failed
        }.also { _nicknameCheckState.value = it }
    }

    fun checkHasWaverPlus() {
        viewModelScope.launch(ceh(_hasWaverPlus, false)) {
            val response = checkUserLimit.invoke()
            _hasWaverPlus.value =
                response.success && response.data.premiumStatus == ProfileInfo.PremiumStatus.ACTIVE
        }
    }

    fun loadLoginEmail() {
        viewModelScope.launch {
            preferenceDataStoreModule.loadLoginedEmail.collectLatest { value ->
                _loginEmail.value = value
            }
        }
    }

    companion object {
        private const val DUPLICATED_NICKNAME_CODE = "6001"
    }
}
