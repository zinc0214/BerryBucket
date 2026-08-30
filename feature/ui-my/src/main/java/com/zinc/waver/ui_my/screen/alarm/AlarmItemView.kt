package com.zinc.waver.ui_my.screen.alarm

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.zinc.common.models.PushAlarm
import com.zinc.common.models.PushAlarmType
import com.zinc.common.models.PushAlarmType.BADGE
import com.zinc.common.models.PushAlarmType.COMMENT
import com.zinc.common.models.PushAlarmType.D_DAY
import com.zinc.common.models.PushAlarmType.EVENT
import com.zinc.common.models.PushAlarmType.FOLLOW
import com.zinc.common.models.PushAlarmType.LIKE
import com.zinc.common.models.PushAlarmType.NOTICE
import com.zinc.common.models.PushAlarmType.TOGETHER
import com.zinc.waver.ui.design.theme.Gray2
import com.zinc.waver.ui.design.theme.Gray9
import com.zinc.waver.ui.util.HtmlText
import com.zinc.waver.ui_my.R
import com.zinc.waver.ui_my.model.AlarmClickEvent
import com.zinc.waver.ui_common.R as CommonR

@Composable
fun AlarmItemView(
    alarmItem: PushAlarm,
    onClicked: (AlarmClickEvent) -> Unit
) {
    val clickEvent = getClickEvent(alarmItem)
    val hasImageUrl = alarmItem.imgUrl != null
    val imageShape = RoundedCornerShape(14.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (clickEvent != null) {
                    Modifier.clickable { onClicked(clickEvent) }
                } else {
                    Modifier
                }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            // sizeIn(36.dp) 은 minWidth 만 지정돼 최대 크기가 열려 있었다.
            // 폴백 아이콘(intrinsic 80dp)이 그대로 커지지 않도록 36dp 로 고정한다.
            modifier = Modifier
                .size(36.dp)
                // 이미지 URL 인 경우에만 라운딩 + 테두리를 준다. 타입별 아이콘은 원본 그대로 노출한다.
                .then(
                    if (hasImageUrl) {
                        Modifier
                            .clip(imageShape)
                            .border(width = 1.dp, color = Gray2, shape = imageShape)
                    } else {
                        Modifier
                    }
                ),
            // 이미지 URL이 내려오면 우선 사용하고, 없으면 타입별 아이콘을 사용한다.
            // URL 은 있는데 로드에 실패하면 빈 프로필 아이콘으로 대체한다.
            painter = if (hasImageUrl) {
                rememberAsyncImagePainter(
                    model = alarmItem.imgUrl,
                    error = painterResource(CommonR.drawable.profile_icon_blank),
                    fallback = painterResource(CommonR.drawable.profile_icon_blank)
                )
            } else {
                painterResource(getAlarmIcon(alarmItem.type))
            },
            // 비율이 다른 이미지가 와도 라운딩된 영역을 꽉 채우도록 한다.
            contentScale = if (hasImageUrl) ContentScale.Crop else ContentScale.Fit,
            contentDescription = stringResource(R.string.alarmIconDesc)
        )

        Spacer(modifier = Modifier.width(14.dp))

        HtmlText(
            html = alarmItem.message,
            fontSize = 14.dp,
            textColor = Gray9.hashCode()
        )
    }
}

// 타입별 클릭 이벤트, null 이면 클릭 불가
private fun getClickEvent(alarmItem: PushAlarm): AlarmClickEvent? = when (alarmItem.type) {
    LIKE, COMMENT, D_DAY, TOGETHER -> alarmItem.bucketId?.let { AlarmClickEvent.GoToBucketDetail(it) }
    FOLLOW -> AlarmClickEvent.GoToFollowerList
    BADGE -> AlarmClickEvent.GoToBadgeList
    NOTICE, EVENT -> null
}

private fun getAlarmIcon(type: PushAlarmType) = when (type) {
    LIKE -> R.drawable.btn_32_like_on
    COMMENT -> R.drawable.btn_32_coment_alarm
    FOLLOW -> R.drawable.btn_32_app_noti
    BADGE -> R.drawable.btn_32_badge
    TOGETHER -> CommonR.drawable.ico_36_together
    NOTICE -> R.drawable.btn_32_app_noti
    EVENT -> R.drawable.btn_32_event
    D_DAY -> R.drawable.btn_32_alarm_d_day
}

@Preview
@Composable
private fun AlarmItemPreview() {
    AlarmItemView(
        alarmItem = PushAlarm(
            type = COMMENT,
            message = "맹꽁이 좋아요 님이 둥가둥가",
            imgUrl = null
        ),
        onClicked = {}
    )
}
