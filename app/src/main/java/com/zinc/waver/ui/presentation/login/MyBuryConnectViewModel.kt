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

    // 중복 요청 방지. 결과가 나오면 화면이 곧 닫히므로 되돌릴 필요가 없다.
    private var isRequesting = false

    fun requestMigration() {
        if (isRequesting) return
        isRequesting = true

        // 네트워크 오류는 NONE 으로 흘려서 안내 없이 다음 단계로 넘어가게 한다.
        viewModelScope.launch(ceh(_migrationResult, MyBuryMigrationResult.NONE)) {
            _migrationResult.value = MyBuryMigrationResult.from(requestMyBuryMigration())
        }
    }
}
