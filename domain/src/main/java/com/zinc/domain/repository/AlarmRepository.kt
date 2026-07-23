package com.zinc.domain.repository

import com.zinc.common.models.MyPushResponse

interface AlarmRepository {
    suspend fun loadMyPushList(): MyPushResponse
}
