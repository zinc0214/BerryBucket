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

    // 중복 요청 방지.
    private var isRequesting = false

    /**
     * 화면이 끝날 때 결과를 소비한다.
     *
     * 이 VM 은 Activity 스코프로 유지되므로 리셋하지 않으면 다음 진입 때
     * [migrationResult] 가 지난 결과를 그대로 들고 있어, 버튼을 누르지도 않았는데
     * 이전 안내 팝업이 다시 뜬다.
     */
    fun consumeResult() {
        _migrationResult.value = null
        isRequesting = false
    }

    fun requestMigration() {
        if (isRequesting) return
        isRequesting = true

        // 네트워크 오류는 NONE 으로 흘려서 안내 없이 다음 단계로 넘어가게 한다.
        viewModelScope.launch(ceh(_migrationResult, MyBuryMigrationResult.NONE)) {
            _migrationResult.value = MyBuryMigrationResult.from(requestMyBuryMigration())
        }
    }
}
