package com.zinc.berrybucket.compose.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.zinc.berrybucket.R


@OptIn(ExperimentalPagerApi::class)
@Composable
private fun ImageViewPager(imageList: List<String>) {
    Scaffold(modifier = Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            val pagerState = rememberPagerState()

            Card(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .aspectRatio(1f),
                shape = RoundedCornerShape(4.dp),
                elevation = 0.dp,
                content = {
                    // Display 10 items
                    HorizontalPager(
                        count = 10,
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) { page ->
                        Image(
                            painter = painterResource(id = R.drawable.kakao),
                            contentDescription = "Test",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            )

            HorizontalPagerIndicator(
                pagerState = pagerState,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(top = 4.dp),
            )
        }
    }
}
