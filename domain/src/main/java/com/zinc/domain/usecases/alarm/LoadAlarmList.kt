package com.zinc.domain.usecases.alarm

import com.zinc.domain.repository.AlarmRepository
import javax.inject.Inject

class LoadAlarmList @Inject constructor(
    private val alarmRepository: AlarmRepository
) {
    suspend operator fun invoke() = alarmRepository.loadMyPushList()
}
