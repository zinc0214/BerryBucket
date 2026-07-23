package com.zinc.common.models

import kotlinx.serialization.Serializable

@Serializable
data class MyPushResponse(
    val data: MyPushList,
    val success: Boolean,
    val code: String,
    val message: String
)

@Serializable
data class MyPushList(
    val alarms: List<PushAlarm>
)
@Serializable
data class PushAlarm(
    val type: PushAlarmType,
    val message: String,
    val imgUrl: String? = null, // FOLLOW 타입인 경우 팔로워의 프로필 이미지 URL
    val bucketId: String? = null // LIKE, COMMENT, D_DAY, TOGETHER 타입인 경우 이동할 버킷리스트 ID
)

enum class PushAlarmType {
    LIKE, // 좋아요
    COMMENT, // 댓글
    FOLLOW, // 팔로우
    BADGE, // 뱃지 달성
    TOGETHER, // 함께하는 사람이 버킷 완성
    NOTICE, // 공지
    EVENT, // 이벤트
    D_DAY // 디데이 7일
}
