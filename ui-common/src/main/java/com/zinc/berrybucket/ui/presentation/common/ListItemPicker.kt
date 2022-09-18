package com.zinc.berrybucket.ui.presentation.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.zinc.berrybucket.ui.design.theme.Gray4
import com.zinc.berrybucket.ui.design.theme.Main4
import com.zinc.berrybucket.ui.util.dpToSp
import com.zinc.berrybucket.ui.util.parseDateWithZero
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

private fun <T> getItemIndexForOffset(
    range: List<T>,
    value: T,
    offset: Float,
    halfNumbersColumnHeightPx: Float
): Int {
    val indexOf = range.indexOf(value) - (offset / halfNumbersColumnHeightPx).toInt()
    return maxOf(0, minOf(indexOf, range.count() - 1))
}

@Composable
fun NumberPicker(
    modifier: Modifier = Modifier,
    label: (String) -> String = { it.toString() },
    value: Int,
    onValueChange: (Int) -> Unit,
    rangeList: List<Int>
) {
    val verticalMargin = 16.dp
    val numbersColumnHeight = 120.dp
    val halfNumbersColumnHeight = numbersColumnHeight / 2
    val halfNumbersColumnHeightPx = with(LocalDensity.current) { halfNumbersColumnHeight.toPx() }

    val coroutineScope = rememberCoroutineScope()

    val animatedOffset = remember { Animatable(0f) }
        .apply {
            val index = rangeList.indexOf(value)
            val offsetRange = remember(value, rangeList) {
                -((rangeList.count() - 1) - index) * halfNumbersColumnHeightPx to
                        index * halfNumbersColumnHeightPx
            }
            updateBounds(offsetRange.first, offsetRange.second)
        }

    val coercedAnimatedOffset = animatedOffset.value % halfNumbersColumnHeightPx

    val indexOfElement =
        getItemIndexForOffset(rangeList, value, animatedOffset.value, halfNumbersColumnHeightPx)

    var dividersWidth by remember { mutableStateOf(16.dp) }

    Layout(
        modifier = modifier
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState { deltaY ->
                    coroutineScope.launch {
                        animatedOffset.snapTo(animatedOffset.value + deltaY)
                    }
                },
                onDragStopped = { velocity ->
                    coroutineScope.launch {
                        val endValue = animatedOffset.fling(
                            initialVelocity = velocity,
                            animationSpec = exponentialDecay(frictionMultiplier = 20f),
                            adjustTarget = { target ->
                                val coercedTarget = target % halfNumbersColumnHeightPx
                                val coercedAnchors =
                                    listOf(
                                        -halfNumbersColumnHeightPx,
                                        0f,
                                        halfNumbersColumnHeightPx
                                    )
                                val coercedPoint =
                                    coercedAnchors.minByOrNull { abs(it - coercedTarget) }!!
                                val base =
                                    halfNumbersColumnHeightPx * (target / halfNumbersColumnHeightPx).toInt()
                                coercedPoint + base
                            }
                        ).endState.value

                        val result = rangeList.elementAt(
                            getItemIndexForOffset(
                                rangeList,
                                value,
                                endValue,
                                halfNumbersColumnHeightPx
                            )
                        )
                        onValueChange(result)
                        animatedOffset.snapTo(0f)
                    }
                }
            )
            .padding(vertical = numbersColumnHeight / 3 + verticalMargin * 2),
        content = {

            Box(
                modifier
                    .width(dividersWidth)
                    .height(16.dp)
                    .background(color = Color.Transparent)
            )
            Box(
                modifier = Modifier
                    .padding(vertical = verticalMargin)
                    .offset { IntOffset(x = 0, y = coercedAnimatedOffset.roundToInt()) }
            ) {
                val baseLabelModifier = Modifier.align(Alignment.Center)
                if (indexOfElement > 0)
                    Label(
                        text = label(parseDateWithZero(rangeList.elementAt(indexOfElement - 1))),
                        textStyle = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = dpToSp(dp = 26.dp),
                            color = Gray4
                        ),
                        modifier = baseLabelModifier
                            .offset(y = -halfNumbersColumnHeight)
                            .heightIn(min = 48.dp)
                    )
                Label(
                    text = label(parseDateWithZero(rangeList.elementAt(indexOfElement))),
                    textStyle = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = dpToSp(dp = 30.dp),
                        color = Main4
                    ),
                    modifier = baseLabelModifier
                        .heightIn(min = 48.dp)
                )
                if (indexOfElement < rangeList.count() - 1)
                    Label(
                        text = label(parseDateWithZero(rangeList.elementAt(indexOfElement + 1))),
                        textStyle = TextStyle(
                            fontWeight = FontWeight.Bold,
                            fontSize = dpToSp(dp = 26.dp),
                            color = Gray4
                        ),
                        modifier = baseLabelModifier
                            .offset(y = halfNumbersColumnHeight)
                            .heightIn(min = 48.dp)
                    )
            }
            Box(
                modifier
                    .width(dividersWidth)
                    .height(16.dp)
                    .background(color = Color.Transparent)
            )
        }
    ) { measurables, constraints ->
        // Don't constrain child views further, measure them with given constraints
        // List of measured children
        val placeables = measurables.map { measurable ->
            // Measure each children
            measurable.measure(constraints)
        }

        dividersWidth = placeables
            .drop(1)
            .first()
            .width
            .toDp()

        // Set the size of the layout as big as it can
        layout(dividersWidth.toPx().toInt(), placeables
            .sumOf {
                it.height
            }
        ) {
            // Track the y co-ord we have placed children up to
            var yPosition = 0

            // Place children in the parent layout
            placeables.forEach { placeable ->

                // Position item on the screen
                placeable.placeRelative(x = 0, y = yPosition)

                // Record the y co-ord placed up to
                yPosition += placeable.height
            }
        }
    }
}

@Composable
private fun Label(text: String, textStyle: TextStyle, modifier: Modifier) {
    Text(
        modifier = modifier
            .widthIn(min = 80.dp)
            .heightIn(min = 60.dp)
            .pointerInput(Unit) {
                detectTapGestures(onLongPress = {
                    // FIXME: Empty to disable text selection
                })
            },
        text = text,
        textAlign = TextAlign.Center,
        style = textStyle
    )
}

private suspend fun Animatable<Float, AnimationVector1D>.fling(
    initialVelocity: Float,
    animationSpec: DecayAnimationSpec<Float>,
    adjustTarget: ((Float) -> Float)?,
    block: (Animatable<Float, AnimationVector1D>.() -> Unit)? = null,
): AnimationResult<Float, AnimationVector1D> {
    val targetValue = animationSpec.calculateTargetValue(value, initialVelocity)
    val adjustedTarget = adjustTarget?.invoke(targetValue)
    return if (adjustedTarget != null) {
        animateTo(
            targetValue = adjustedTarget,
            initialVelocity = initialVelocity,
            block = block
        )
    } else {
        animateDecay(
            initialVelocity = initialVelocity,
            animationSpec = animationSpec,
            block = block,
        )
    }
}