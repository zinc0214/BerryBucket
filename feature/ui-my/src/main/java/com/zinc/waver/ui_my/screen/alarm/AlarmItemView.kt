package com.zinc.waver.ui_my.screen.alarm

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.zinc.waver.ui.design.theme.Gray9
import com.zinc.waver.ui.util.HtmlText
import com.zinc.waver.ui_my.R

@Composable
fun AlarmItemView(alarmItem: PushAlarm) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            modifier = Modifier
                .padding(0.dp)
                .sizeIn(36.dp),
            // 이미지 URL이 내려오면 우선 사용하고, 없으면 타입별 아이콘을 사용한다
            painter = if (alarmItem.imgUrl != null) {
                rememberAsyncImagePainter(model = alarmItem.imgUrl)
            } else {
                painterResource(getAlarmIcon(alarmItem.type))
            },
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

private fun getAlarmIcon(type: PushAlarmType) = when (type) {
    LIKE -> R.drawable.btn_32_like_on
    COMMENT -> R.drawable.btn_32_coment_alarm
    FOLLOW -> R.drawable.btn_32_app_noti // TODO : 팔로우 아이콘 추가 필요
    BADGE -> R.drawable.btn_32_app_noti // TODO : 뱃지 아이콘 추가 필요
    TOGETHER -> com.zinc.waver.ui_common.R.drawable.ico_36_together
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
        )
    )
}
