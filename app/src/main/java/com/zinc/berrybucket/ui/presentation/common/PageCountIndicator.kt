package com.zinc.berrybucket.ui.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import com.zinc.berrybucket.ui.compose.theme.Gray1
import com.zinc.berrybucket.ui.compose.theme.Gray11

@ExperimentalPagerApi
@Composable
fun PageCountIndicator(
    pagerState: PagerState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = Gray11.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(vertical = 3.dp, horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = "${pagerState.currentPage + 1}/${pagerState.pageCount}",
            color = Gray1,
            fontSize = 12.sp
        )
    }
}