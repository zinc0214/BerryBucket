package com.zinc.waver.ui.presentation.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.zinc.common.models.MyBuryMigrationResult
import com.zinc.waver.R
import com.zinc.waver.model.DialogButtonInfo
import com.zinc.waver.ui.design.theme.Gray1
import com.zinc.waver.ui.design.theme.Gray10
import com.zinc.waver.ui.design.theme.Gray3
import com.zinc.waver.ui.design.theme.Gray6
import com.zinc.waver.ui.design.theme.Main4
import com.zinc.waver.ui.presentation.component.MyText
import com.zinc.waver.ui.presentation.component.dialog.CommonDialogView
import com.zinc.waver.ui.util.dpToSp
import com.zinc.waver.ui_common.R as CommonR

/**
 * 마이버리 기존 회원(join 응답의 myburyYn == Y)에게 데이터 이관을 안내하는 화면.
 *
 * 이관 요청과 결과 팝업은 이 화면 안에서 끝난다. 호출자는 화면이 끝났다는 것만 알면 되므로
 * 콜백은 [onFinished] 하나뿐이다.
 */
@Composable
fun MyBuryConnectScreen(onFinished: () -> Unit) {
    val viewModel: MyBuryConnectViewModel = hiltViewModel()
    val migrationResult by viewModel.migrationResult.observeAsState()

    var isRequesting by remember { mutableStateOf(false) }

    // 안내할 내용이 없는 결과(8200 / 기타 코드 / 네트워크 오류)는 팝업 없이 바로 다음 단계로.
    LaunchedEffect(migrationResult) {
        if (migrationResult == MyBuryMigrationResult.NONE) {
            onFinished()
        }
    }

    MyBuryConnectContent(
        isRequesting = isRequesting,
        onMigrateClicked = {
            isRequesting = true
            viewModel.requestMigration()
        },
        onSkipClicked = onFinished
    )

    migrationResult?.messageRes()?.let { messageRes ->
        CommonDialogView(
            message = stringResource(id = messageRes),
            dismissAvailable = false,
            rightButtonInfo = DialogButtonInfo(
                text = CommonR.string.confirm,
                color = Main4
            ),
            rightButtonEvent = onFinished
        )
    }
}

private fun MyBuryMigrationResult.messageRes(): Int? = when (this) {
    MyBuryMigrationResult.REQUESTED -> R.string.myBuryMigrationRequested
    MyBuryMigrationResult.ALREADY_DONE -> R.string.myBuryMigrationAlreadyDone
    MyBuryMigrationResult.ALREADY_REQUESTED -> R.string.myBuryMigrationAlreadyRequested
    MyBuryMigrationResult.NONE -> null
}

@Composable
private fun MyBuryConnectContent(
    isRequesting: Boolean,
    onMigrateClicked: () -> Unit,
    onSkipClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Gray1)
    ) {
        MyBuryConnectHeroView(
            onCloseClicked = onSkipClicked,
            modifier = Modifier.weight(1f)
        )
        MyBuryConnectGuideView(
            isRequesting = isRequesting,
            onMigrateClicked = onMigrateClicked,
            onSkipClicked = onSkipClicked
        )
    }
}

@Composable
private fun MyBuryConnectHeroView(
    onCloseClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Image(
            painter = painterResource(id = CommonR.drawable.bg_membership_login),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = CommonR.drawable.btn_40_close),
                contentDescription = stringResource(id = R.string.myBuryConnectCloseDesc),
                tint = Gray1,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 8.dp, top = 8.dp)
                    .clickable { onCloseClicked() }
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = CommonR.drawable.mybury_logo),
                    contentDescription = null,
                    modifier = Modifier.size(76.dp)
                )
                Image(
                    painter = painterResource(id = CommonR.drawable.mybury_to_waver),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(horizontal = 2.dp)
                        .width(56.dp)
                        .height(52.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.playstore),
                    contentDescription = null,
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(20.dp))
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            MyText(
                text = stringResource(id = R.string.myBuryConnectHeroTitle),
                color = Gray1,
                fontSize = dpToSp(24.dp),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = dpToSp(34.dp)
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun MyBuryConnectGuideView(
    isRequesting: Boolean,
    onMigrateClicked: () -> Unit,
    onSkipClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Gray1)
            .navigationBarsPadding()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(36.dp))

        MyText(
            text = stringResource(id = R.string.myBuryConnectTitle),
            color = Gray10,
            fontSize = dpToSp(18.dp),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        MyText(
            text = stringResource(id = R.string.myBuryConnectHighlight),
            color = Main4,
            fontSize = dpToSp(15.dp),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = dpToSp(22.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        MyText(
            text = stringResource(id = R.string.myBuryConnectDescription),
            color = Gray6,
            fontSize = dpToSp(14.dp),
            textAlign = TextAlign.Center,
            lineHeight = dpToSp(21.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        MyBuryConnectButton(
            text = stringResource(id = R.string.myBuryConnectMigrateButton),
            textColor = Gray1,
            backgroundColor = Main4,
            borderColor = null,
            enabled = !isRequesting,
            onClicked = onMigrateClicked
        )

        Spacer(modifier = Modifier.height(12.dp))

        MyBuryConnectButton(
            text = stringResource(id = R.string.myBuryConnectFreshStartButton),
            textColor = Gray10,
            backgroundColor = Gray1,
            borderColor = Gray3,
            enabled = !isRequesting,
            onClicked = onSkipClicked
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun MyBuryConnectButton(
    text: String,
    textColor: Color,
    backgroundColor: Color,
    borderColor: Color?,
    enabled: Boolean,
    onClicked: () -> Unit
) {
    val shape = RoundedCornerShape(8.dp)
    MyText(
        text = text,
        color = textColor,
        fontSize = dpToSp(16.dp),
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(color = backgroundColor, shape = shape)
            .then(
                if (borderColor == null) Modifier
                else Modifier.border(width = 1.dp, color = borderColor, shape = shape)
            )
            .clickable(enabled = enabled) { onClicked() }
            .padding(vertical = 18.dp)
    )
}

@Preview(showBackground = true, heightDp = 780)
@Composable
private fun MyBuryConnectContentPreview() {
    MyBuryConnectContent(
        isRequesting = false,
        onMigrateClicked = {},
        onSkipClicked = {}
    )
}
