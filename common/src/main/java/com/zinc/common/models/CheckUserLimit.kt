package com.zinc.common.models

import kotlinx.serialization.Serializable

@Serializable
data class CheckUserLimitResponse(
    val data: UserLimitInfo,
    val success: Boolean,
    val code: String,
    val message: String
)

@Serializable
data class UserLimitInfo(
    val premiumStatus: ProfileInfo.PremiumStatus,
    val imageLimit: Int,
    val imageUsed: Int,
    val togetherLimit: Int,
    val togetherUsed: Int
) {
    val isPremium: Boolean get() = premiumStatus == ProfileInfo.PremiumStatus.ACTIVE
    val canUseImage: Boolean get() = isPremium || imageUsed < imageLimit
    val canUseTogether: Boolean get() = isPremium || togetherUsed < togetherLimit

    companion object {
        // API 실패 등 확인 불가 시 사용하는 폴백 (모두 사용 불가)
        val NONE = UserLimitInfo(
            premiumStatus = ProfileInfo.PremiumStatus.NONE,
            imageLimit = 0,
            imageUsed = 0,
            togetherLimit = 0,
            togetherUsed = 0
        )
    }
}
