package com.zinc.mybury_2.compose.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zinc.mybury_2.R
import com.zinc.mybury_2.compose.theme.Gray3
import com.zinc.mybury_2.compose.theme.Main3
import com.zinc.mybury_2.model.BucketProgressState
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@Composable
fun BucketCircularProgressBar(
    progressState: (BucketProgressState) -> Unit,
    radius: Dp = 16.dp,
    color: Color = Main3,
    animDuration: Int = 1000,
    animDelay: Int = 0
) {
    var animationPlayed by remember {
        mutableStateOf(false)
    }
    val curPercentage = animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(
            durationMillis = animDuration,
            delayMillis = animDelay
        )
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
            drawArc(
                color = color,
                -90f,
                360 * curPercentage.value,
                useCenter = false,
                style = Stroke(1.5.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        Image(
            painter = painterResource(R.drawable.check),
            contentDescription = null,
            modifier = Modifier
                .height(32.dp)
                .width(32.dp)
        )

        if ((curPercentage.value * 100).toInt() == 100) {
            Image(
                painter = painterResource(R.drawable.check_succeed),
                contentDescription = null,
                modifier = Modifier
                    .height(32.dp)
                    .width(32.dp)
            )

            runBlocking {
                coroutineScope {
                    launch {
                        animationPlayed = false
                        progressState.invoke(BucketProgressState.BACK)
                        delay(500L)
                    }
                }
            }
        }
    }
}