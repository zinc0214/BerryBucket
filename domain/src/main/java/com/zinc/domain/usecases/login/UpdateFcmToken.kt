package com.zinc.domain.usecases.login

import com.zinc.domain.repository.LoginRepository
import javax.inject.Inject

// FCM 토큰 등록
class UpdateFcmToken @Inject constructor(
    private val loginRepository: LoginRepository
) {
    suspend operator fun invoke(token: String) =
        loginRepository.updateFcmToken(token)
}
