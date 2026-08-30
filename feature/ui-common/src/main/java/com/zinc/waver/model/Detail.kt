package com.zinc.waver.model

import com.zinc.common.models.BucketStatus

sealed class DetailDescType {
    data class WriterProfileInfoUi(
        val profileImage: String?,
        val badgeImage: String,
        val badgeTitle: String,
        val nickName: String,
        val userId: String
    ) : DetailDescType() {
        fun toUi() = UiProfileInfo(
            profileImage = this.profileImage,
            badgeImage = this.badgeImage,
            titlePosition = this.badgeTitle,
            nickName = this.nickName
        )
    }

    data class ImageInfo(
        val imageList: List<String>
    ) : DetailDescType()

    data class CommonDetailDescInfo(
        val isMine: Boolean,
        val dDay: String?,
        val status: BucketStatus,
        val keywordList: List<WriteKeyWord>?,
        val title: String,
        val goalCount: Int,
        val userCount: Int,
        val categoryInfo: WriteCategoryInfo,
        val isScrap: Boolean
    ) : DetailDescType()

    data class CloseDetailDescInfo(
        val commonDetailDescInfo: CommonDetailDescInfo,
        val goalCount: Int,
        val userCount: Int
    ) : DetailDescType()

    data class CommentInfo(
        val commentCount: Int,
        val commentList: List<Comment>
    ) : DetailDescType()

    data class MemoInfo(
        val memo: String
    ) : DetailDescType()

}

data class BucketDetailUiInfo(
    val bucketId: String,
    val writeOpenType: WriteOpenType,
    val imageInfo: DetailDescType.ImageInfo? = null,
    val writerProfileInfo: DetailDescType.WriterProfileInfoUi,
    val descInfo: DetailDescType.CommonDetailDescInfo,
    val memoInfo: DetailDescType.MemoInfo? = null,
    val commentInfo: DetailDescType.CommentInfo? = null,
    val togetherInfo: TogetherInfo? = null,
    val isMine: Boolean,
    val isDone: Boolean,
    val isLike: Boolean
) {
    val canShowCompleteButton get() = isMine && !isDone
    fun getButtonIndex(): Int {
        var index = 0
        if (imageInfo != null) index += 1
        if (memoInfo != null) index += 1
        return index
    }

}

data class Comment(
    val commentId: String,
    val userId: String,
    val profileImage: String?,
    val nickName: String,
    val comment: String,
    val isMine: Boolean
)

data class TogetherInfo(
    val count: String,
    val togetherMembers: List<TogetherMember>
)

data class TogetherMember(
    val memberId: String = "",
    val profileImage: String,
    val nickName: String,
    val isMine: Boolean,
    val goalCount: Int,
    val userCount: Int,
    val isSucceed: Boolean = false
)

data class CommentMentionInfo(
    val userId: String,
    val profileImage: String?,
    val nickName: String,
    val isFriend: Boolean,
    val isSelected: Boolean
)

data class SuccessButtonInfo(
    val goalCount: Int,
    val userCount: Int
)

sealed class DetailAppBarClickEvent {
    data object MoreOptionClicked : DetailAppBarClickEvent()
    data object CloseClicked : DetailAppBarClickEvent()
}

sealed class DetailClickEvent {
    data class SuccessClicked(val id: String) : DetailClickEvent()
    data class GoToOtherProfile(val id: String) : DetailClickEvent()
}

data class CommentLongClicked(val commentIndex: Int) : DetailClickEvent()

sealed interface DetailLoadFailStatus {
    data object LoadFail : DetailLoadFailStatus
    data object AchieveFail : DetailLoadFailStatus
    data object GoalCountUpdateFail : DetailLoadFailStatus
    data object AddCommentFail : DetailLoadFailStatus
    data object DeleteCommentFail : DetailLoadFailStatus
    data object LikeFail : DetailLoadFailStatus
}