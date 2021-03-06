package com.zinc.berrybucket.model

//data class DetailInfo(
//    val detailProfileInfo: ProfileInfo,
//    val detailDescInfo: DetailDescInfo,
//    val commentInfo: CommentInfo
//)

data class ImageInfo(
    val imageList: List<String>
) : DetailDescType()

data class CommonDetailDescInfo(
    val dDay: String,
    val tagList: List<String>,
    val title: String,
    val goalCount: Int = 0,
    val userCount: Int = 0
) : DetailDescType()

data class CloseDetailDescInfo(
    val commonDetailDescInfo: CommonDetailDescInfo,
    val goalCount: Int,
    val userCount: Int
) : DetailDescType()

data class CommentInfo(
    val commentCount: Int,
    val commenterList: List<Commenter>
) : DetailDescType()

data class MemoInfo(
    val memo: String
) : DetailDescType()

data class Commenter(
    val commentId: String = "",
    val profileImage: String,
    val nickName: String,
    val comment: String,
    val isMine: Boolean = true
)

data class InnerSuccessButton(
    var isVisible: Boolean
) : DetailDescType()

data class CommentTagInfo(
    val profileImageView: String,
    val nickName: String
)

data class SuccessButtonInfo(
    val goalCount: Int = 0,
    val userCount: Int = 0
)

sealed class DetailDescType {

}

fun detailId(descType: DetailDescType): Int {
    return when (descType) {
        is ImageInfo -> 0
        is ProfileInfo -> 1
        is CommonDetailDescInfo, is CloseDetailDescInfo -> 2
        is MemoInfo -> 3
        is CommentInfo -> 4
        is InnerSuccessButton -> 5
    }
}

enum class DetailType(val text : String) {
    MY_CLOSE("MY_CLOSE"),  // 내 공개 상세 버킷
    MY_OPEN("MY_OPEN"),  // 내 비공개 상세 버킷
    OTHER_OPEN("OTHER_OPEN") // 다른사람의 공개 상세 버킷
}

data class DetailInfo(
    val imageInfo: ImageInfo? = null,
    val profileInfo: ProfileInfo,
    val descInfo: CommonDetailDescInfo,
    val memoInfo: MemoInfo? = null,
    val commentInfo: CommentInfo? = null
)

sealed class DetailAppBarClickEvent {
    object MoreOptionClicked : DetailAppBarClickEvent()
    object CloseClicked : DetailAppBarClickEvent()
}

sealed class DetailClickEvent {
    object SuccessClicked : DetailClickEvent()
}

data class CommentLongClicked(val commentIndex: Int) : DetailClickEvent()