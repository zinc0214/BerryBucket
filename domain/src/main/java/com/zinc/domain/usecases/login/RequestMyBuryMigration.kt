package com.zinc.domain.usecases.login

import com.zinc.common.models.CommonResponse2
import com.zinc.domain.repository.LoginRepository
import javax.inject.Inject

// 마이버리 데이터 이관 요청
class RequestMyBuryMigration @Inject constructor(
    private val loginRepository: LoginRepository
) {
    suspend operator fun invoke(): CommonResponse2 =
        loginRepository.requestMyBuryMigration()
}
