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
    val memo: String? = null,
    val exposureStatus: ExposureStatus,
    val status: CompleteStatus,
    val pin: YesOrNo,
    val complete: YesOrNo,
    val scrapYn: YesOrNo = YesOrNo.N, // 스크랩 여부
    val isMine: YesOrNo,
    val isLike: YesOrNo,
    val category: Category,
    val goalCount: Int,
    val userCount: Int,
    val completedDt: String? = null, // 완료 일시(ISO-8601)
    val targetDate: String? = null,
    val keywords: List<Keyword> = emptyList(),
    val friendStatusList: List<FriendStatus>? = null, // 함께하는 친구들의 달성 현황
    val images: List<String>? = null,
    val comment: List<Comment>? = null
) {
    enum class CompleteStatus {
        PROGRESS, COMPLETE
    }


    enum class ExposureStatus {
        PUBLIC, FOLLOWER, PRIVATE
    }

    @Serializable
    data class FriendStatus(
        val id: String,
        val name: String,
        val imgUrl: String? = null,
        val userCount: Int = 0,
        val status: CompleteStatus = CompleteStatus.PROGRESS,
        val completedDt: String? = null
    ) {
        fun isSucceed() = status == CompleteStatus.COMPLETE
    }

    @Serializable
    data class Comment(
        val id: String,
        val isMyComment: YesOrNo,
        val userId: String,
        val imgUrl: String? = null,
        val name: String,
        val content: String,
        val isBlocked: YesOrNo = YesOrNo.N // 차단한 유저의 댓글 여부
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
