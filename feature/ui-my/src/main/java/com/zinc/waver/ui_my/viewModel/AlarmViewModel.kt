package com.zinc.waver.ui_my.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zinc.common.models.PushAlarm
import com.zinc.domain.usecases.alarm.LoadAlarmList
import com.zinc.waver.ui.viewmodel.CommonViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val loadAlarmList: LoadAlarmList,
) : CommonViewModel() {

    private val _alarmList = MutableLiveData<List<PushAlarm>>()
    val alarmList: LiveData<List<PushAlarm>> get() = _alarmList

    private val _alarmLoadFail = MutableLiveData<Boolean>()
    val alarmLoadFail: LiveData<Boolean> get() = _alarmLoadFail

    fun loadAlarmList() {
        viewModelScope.launch(ceh(_alarmLoadFail, true)) {
            val response = loadAlarmList.invoke()
            // data 가 논널로 선언돼 있어 success 를 보기 전에 건드리면 NPE 다.
            if (response.success) {
                _alarmList.value = response.data.alarms
            } else {
                Log.e("ayhan", "loadAlarmList 실패: code=${response.code}")
                _alarmLoadFail.value = true
            }
        }
    }
}
