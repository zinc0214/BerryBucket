package com.zinc.domain.repository

import com.zinc.common.models.CommonResponse
import com.zinc.common.models.ReportItems

interface ReportRepository {
    suspend fun loadReportItems(): ReportItems
    suspend fun reportComment(token: String, id: Int, reason: String): CommonResponse
}