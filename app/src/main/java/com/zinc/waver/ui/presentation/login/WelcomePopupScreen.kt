package com.zinc.waver.ui.presentation.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.zinc.waver.R
import com.zinc.waver.model.DialogButtonInfo
import com.zinc.waver.ui.design.theme.Gray1
import com.zinc.waver.ui.design.theme.Gray7
import com.zinc.waver.ui.design.theme.Main4
import com.zinc.waver.ui.presentation.component.BottomButtonView
import com.zinc.waver.ui.presentation.component.MyText
import com.zinc.waver.ui.util.dpToSp

@Composable
fun WelcomePopupScreen(
    goToBadgeInfo: () -> Unit,
    gotoStart: () -> Unit
) {
    Dialog(
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        ),
        onDismissRequest = { }) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .width(320.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color = Gray1),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // 웰컴 이미지
                Icon(
                    painter = painterResource(id = R.drawable.welcome_waver),
                    contentDescription = "Welcome Waver",
                    modifier = Modifier.fillMaxWidth(),
                    tint = Color.Unspecified
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 제목
                MyText(
                    text = stringResource(id = R.string.welcomePopupTitle),
                    fontSize = dpToSp(16.dp),
                    fontWeight = FontWeight.Bold,
                    color = Main4,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 설명 텍스트
                MyText(
                    text = stringResource(id = R.string.welcomePopupDescription),
                    fontSize = dpToSp(15.dp),
                    color = Gray7,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 34.dp)
                )

                Spacer(modifier = Modifier.height(39.dp))

                BottomButtonView(
                    negative = DialogButtonInfo(
                        text = R.string.welcomePopupNegativeButton,
                        color = Gray7
                    ),
                    positive = DialogButtonInfo(
                        text = R.string.welcomePopupPositiveButton,
                        color = Main4
                    ),
                    negativeEvent = goToBadgeInfo,
                    positiveEvent = gotoStart
                )
            }
        }
    }
}

@Preview
@Composable
fun WelcomePopupScreenPreview() {
    WelcomePopupScreen(
        goToBadgeInfo = {},
        gotoStart = {}
    )
}