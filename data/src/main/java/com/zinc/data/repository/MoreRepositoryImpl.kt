package com.zinc.data.repository

import com.zinc.common.models.CheckUserLimitResponse
import com.zinc.common.models.CommonResponse
import com.zinc.common.models.CommonResponse2
import com.zinc.common.models.LoadMyWaveBadgeResponse
import com.zinc.common.models.LoadMyWaveInfoResponse
import com.zinc.common.models.ProfileResponse
import com.zinc.data.api.WaverApi
import com.zinc.data.util.fileToMultipartFile
import com.zinc.data.util.toMultipartFile
import com.zinc.domain.models.SubscriptionStartRequest
import com.zinc.domain.models.UpdateProfileRequest
import com.zinc.domain.repository.MoreRepository
import javax.inject.Inject

internal class MoreRepositoryImpl @Inject constructor(
    private val waverApi: WaverApi
) : MoreRepository {
    override suspend fun loadProfileInfo(): ProfileResponse {
        return waverApi.loadMyProfile()
    }

    override suspend fun updateProfileInfo(
        request: UpdateProfileRequest
    ): CommonResponse {

        val name = request.name.toMultipartFile("name")
        val bio = request.bio.toMultipartFile("bio")
        val profileImage = request.image?.fileToMultipartFile("profileImage")

        return waverApi.updateMyProfile(
            name = name,
            bio = bio,
            profileImage = profileImage
        )
    }

    override suspend fun checkAlreadyUsedNickName(name: String): CommonResponse2 {
        return waverApi.checkAlreadyUsedNickname(name)
    }

    override suspend fun loadMyBadgeInfo(): LoadMyWaveBadgeResponse {
        return waverApi.loadMyWaveBadge()
    }

    override suspend fun loadMyWaveInfo(): LoadMyWaveInfoResponse {
        return waverApi.loadMyWaveInfo()
    }

    override suspend fun requestWithdrawal(): CommonResponse {
        return waverApi.requestWithdraw()
    }

    override suspend fun updateMyBadge(badgeId: Int): CommonResponse {
        return waverApi.updateMyBadge(badgeId)
    }

    override suspend fun startSubscription(request: SubscriptionStartRequest): CommonResponse {
        return waverApi.startSubscription(request)
    }

    override suspend fun checkUserLimit(): CheckUserLimitResponse {
        return waverApi.checkUserLimit()
    }
}