package com.zinc.common.models

import kotlinx.serialization.Serializable

@Serializable
data class BucketDetailResponse(
    val data: DetailInfo,
    val success: Boolean,
    val code: String,
    val message: String
)

@Serializable
data class DetailInfo(
    val id: String,
    val userId: String,
    val title: String,
    val memo: String?,
    val exposureStatus: ExposureStatus,
    val status: CompleteStatus,
    val pin: YesOrNo,
    val complete: YesOrNo,
    val isMine: YesOrNo,
    val isLike: YesOrNo,
    val category: Category,
    val goalCount: Int,
    val userCount: Int,
    val keywords: List<Keyword>,
    val targetDate: String?,
    val friendUsers: List<FriendUser>?,
    val images: List<String>?,
    val comment: List<Comment>?
) {
    enum class CompleteStatus {
        PROGRESS, COMPLETE
    }


    enum class ExposureStatus {
        PUBLIC, FOLLOWER, PRIVATE
    }

    @Serializable
    data class FriendUser(
        val id: String,
        val name: String,
        val imgUrl: String? = null,
        val goalCount: Int = 0,
        val userCount: Int = 0
    )

    @Serializable
    data class Comment(
        val id: String,
        val isMyComment: YesOrNo,
        val userId: String,
        val imgUrl: String?,
        val name: String,
        val content: String
    )

    @Serializable
    data class Category(
        val id: Int,
        val name: String
    )

    @Serializable
    data class Keyword(
        val code: String,
        val name: String
    )
}

data class AddBucketCommentRequest(
    val bucketId: Int,
    val content: String,
    val mentionIds: List<String>
)