package com.zinc.common.models

import java.io.File
import java.io.Serializable

data class JoinResponse(
    val success: Boolean,
    val code: String,
    val message: String,
    val data: JoinData?
) : Serializable

data class JoinData(
    val myburyYn: YesOrNo?
) : Serializable {
    // 마이버리 기존 회원 여부. 필드 누락이나 알 수 없는 값이면 false 로 떨어진다.
    fun isMyBuryUser() = myburyYn?.isYes() == true
}

data class JoinAccessToken(
    val accessToken: String
) : Serializable

data class CreateProfileRequest(
    val accountType: String = "ANDROID",
    val email: String,
    val uid: String,
    val name: String,
    val bio: String? = null,
    val profileImage: File? = null
) : Serializable

data class RefreshTokenResponse(
    val data: RefreshAccessToken?,
    val success: Boolean,
    val code: String,
    val message: String
)

data class RefreshAccessToken(
    val accessToken: String,
    val refreshToken: String
)

data class LoadTokenByEmailRequest(
    val uid: String
)

data class LoadTokenByEmailResponse(
    val data: AccessTokenDto,
    val success: Boolean,
    val code: String,
    val message: String
)

data class AccessTokenDto(
    val accessToken: String
)

data class CheckUserStatusRequest(
    val email: String,
    val uid: String
)

data class CheckUserStatusResponse(
    val data: StatusInfo,
    val success: Boolean,
    val code: String,
    val message: String
) {
    data class StatusInfo(
        val status: Status,
        val lastLoginAt: String?,
        val withdrawnAt: String?
    )

    enum class Status {
        ACTIVE,
        WITHDRAWN
    }
}

data class FcmTokenRequest(
    val fcmToken: String
) : Serializable