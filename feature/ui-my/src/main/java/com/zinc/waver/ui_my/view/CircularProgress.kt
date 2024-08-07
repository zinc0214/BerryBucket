package com.zinc.waver.ui_my.view

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zinc.waver.model.BucketProgressState
import com.zinc.waver.model.MyTabType
import com.zinc.waver.model.MyTabType.ALL
import com.zinc.waver.ui.design.theme.Gray3
import com.zinc.waver.ui.design.theme.Main2
import com.zinc.waver.ui.design.theme.Sub_D2
import com.zinc.waver.ui_common.R

@Composable
fun BucketCircularProgressBar(
    progressState: (BucketProgressState) -> Unit,
    tabType: MyTabType,
    radius: Dp = 16.dp,
    animDuration: Int = 1000,
    animDelay: Int = 0
) {
    var animationPlayed by remember {
        mutableStateOf(false)
    }

    var checkBoxShow by remember {
        mutableStateOf(false)
    }

    var isAnimationFinished by remember {
        mutableStateOf(false)
    }
    val curPercentage = animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(
            durationMillis = animDuration,
            delayMillis = animDelay
        ),
        finishedListener = {
            if (it == 1.0f) {
                animationPlayed = false
                checkBoxShow = true
                progressState.invoke(BucketProgressState.PROGRESS_END)
                isAnimationFinished = true
            }
        },
        label = "curPercentage"
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(radius * 2)
            .clickable {
                animationPlayed = true
                progressState.invoke(BucketProgressState.STARTED)
            }
            .padding(3.dp)
    ) {
        Canvas(modifier = Modifier.size(radius * 2f)) {
            drawCircle(
                color = Gray3,
                style = Stroke(1.5.dp.toPx())
            )
        }

        Canvas(modifier = Modifier.size(radius * 2f)) {
            if (animationPlayed) {
                drawArc(
                    color = if (tabType is ALL) Main2 else Sub_D2,
                    startAngle = -90f,
                    sweepAngle = 360 * curPercentage.value,
                    useCenter = false,
                    style = Stroke(1.5.dp.toPx(), cap = StrokeCap.Round)
                )
            } else {
                drawArc(
                    color = if (tabType is ALL) Main2 else Sub_D2,
                    startAngle = -90f,
                    sweepAngle = if (checkBoxShow) 360f else 0f,
                    useCenter = false,
                    style = Stroke(1.5.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }

        Image(
            painter = painterResource(R.drawable.check),
            contentDescription = null,
            modifier = Modifier
                .height(32.dp)
                .width(32.dp)
        )

        if (checkBoxShow) {
            Image(
                painter =
                if (tabType is ALL)
                    painterResource(R.drawable.check_succeed)
                else
                    painterResource(R.drawable.check_succeed_dday),
                contentDescription = null,
                modifier = Modifier
                    .height(32.dp)
                    .width(32.dp)
            )
        }

        if ((curPercentage.value * 100).toInt() == 1) {
            if (isAnimationFinished) {
                progressState.invoke(BucketProgressState.FINISHED)
                isAnimationFinished = false
            }
            checkBoxShow = false
        }
    }
}

@Preview
@Composable
private fun BucketCircularProgressBarPreview() {
    BucketCircularProgressBar(
        progressState = {}, tabType = ALL,
    )
}