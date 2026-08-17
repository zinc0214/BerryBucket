package com.zinc.data.repository

import com.zinc.common.models.CheckUserStatusRequest
import com.zinc.common.models.CheckUserStatusResponse
import com.zinc.common.models.CommonResponse2
import com.zinc.common.models.CreateProfileRequest
import com.zinc.common.models.JoinResponse
import com.zinc.common.models.LoadTokenByEmailRequest
import com.zinc.common.models.LoadTokenByEmailResponse
import com.zinc.common.models.RefreshTokenResponse
import com.zinc.data.api.WaverApi
import com.zinc.data.util.fileToMultipartFile
import com.zinc.data.util.toMultipartFile
import com.zinc.domain.repository.LoginRepository
import javax.inject.Inject

class LoginRepositoryImpl @Inject constructor(
    private val waverApi: WaverApi
) : LoginRepository {
    override suspend fun createProfile(
        createProfileRequest: CreateProfileRequest
    ): JoinResponse {
        val accountType = createProfileRequest.accountType.toMultipartFile("accountType")
        val email = createProfileRequest.email.toMultipartFile("email")
        val uid = createProfileRequest.uid.toMultipartFile("uid")
        val name = createProfileRequest.name.toMultipartFile("name")
        val bio = createProfileRequest.bio?.toMultipartFile("bio")
        val profileImage = createProfileRequest.profileImage?.fileToMultipartFile("profileImage")

        return waverApi.crateProfile(accountType, email, uid, name, bio, profileImage)
    }

    override suspend fun refreshToken(): RefreshTokenResponse {
        return waverApi.refreshToken()
    }

    override suspend fun requestLogin(loginRequest: LoadTokenByEmailRequest): LoadTokenByEmailResponse {
        return waverApi.requestLogin(loginRequest)
    }

    override suspend fun checkUserStatus(checkUserStatusRequest: CheckUserStatusRequest): CheckUserStatusResponse {
        return waverApi.checkUserStatus(checkUserStatusRequest)
    }

    override suspend fun requestMyBuryMigration(): CommonResponse2 {
        return waverApi.requestMyBuryMigration()
    }
}