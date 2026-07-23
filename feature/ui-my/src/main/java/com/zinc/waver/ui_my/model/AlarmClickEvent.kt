package com.zinc.waver.ui_my.model

sealed class AlarmClickEvent {
    data class GoToBucketDetail(val bucketId: String) : AlarmClickEvent() // LIKE, COMMENT, D_DAY, TOGETHER
    object GoToFollowerList : AlarmClickEvent() // FOLLOW
    object GoToBadgeList : AlarmClickEvent() // BADGE
}
