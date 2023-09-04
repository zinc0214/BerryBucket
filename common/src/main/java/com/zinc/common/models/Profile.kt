package com.zinc.common.models

import kotlinx.serialization.Serializable

@Serializable
data class OtherProfileInfo(
    val id: String,
    val imgUrl: String?,
    val name: String,
    val mutualFollow: Boolean
)

@Serializable
data class MyProfileResponse(
    val data: MyProfileInfo,
    val success: Boolean,
    val code: String,
    val message: String
)

@Serializable
data class MyProfileInfo(
    val email: String,
    val name: String,
    val imgUrl: String?,
    val bio: String,
    val badgeTitle: String,
    val badgeImgUrl: String
)