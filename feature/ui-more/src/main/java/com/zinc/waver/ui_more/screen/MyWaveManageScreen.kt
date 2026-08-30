package com.zinc.waver.ui_more.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.zinc.common.models.MyBadge
import com.zinc.waver.ui.design.theme.Gray1
import com.zinc.waver.ui.design.theme.Gray10
import com.zinc.waver.ui.design.theme.Gray3
import com.zinc.waver.ui.design.theme.Gray8
import com.zinc.waver.ui.design.theme.Main3
import com.zinc.waver.ui.design.theme.Main4
import com.zinc.waver.ui.presentation.component.IconButton
import com.zinc.waver.ui.presentation.component.MyText
import com.zinc.waver.ui.util.dpToSp
import com.zinc.waver.ui_more.R
import com.zinc.waver.ui_more.models.MyWaverTotalInfo
import com.zinc.waver.ui_more.viewModel.MyWaveManageViewModel
import java.util.UUID
import com.zinc.waver.ui_common.R as CommonR

@Composable
fun MyWaveManageScreen(
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MyWaveManageViewModel = hiltViewModel()
) {
    val myWaveInfoAsState by viewModel.myWaverInfo.observeAsState()
    val succeedUpdate by viewModel.updateSucceed.observeAsState()

    val myWaveInfo = remember {
        mutableStateOf(myWaveInfoAsState)
    }
    var selectedBadge: MyBadge? by remember {
        mutableStateOf(null)
    }


    LaunchedEffect(key1 = myWaveInfoAsState) {
        myWaveInfo.value = myWaveInfoAsState
        selectedBadge = myWaveInfo.value?.badges?.firstOrNull()
    }

    LaunchedEffect(succeedUpdate) {
        if (succeedUpdate == true) {
            onBackPressed()
        }
    }

    BackHandler {
        viewModel.updateMySelectBadge(
            selectedBadge?.id ?: myWaveInfo.value?.badges?.first()?.id ?: -1
        )
        onBackPressed()
    }

    // 컴포지션 본문에서 호출하면 리컴포지션마다 다시 요청한다.
    LaunchedEffect(Unit) {
        if (myWaveInfo.value == null) {
            viewModel.loadMyWaveInfo()
        }
    }

    myWaveInfo.value?.let { info ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            HeaderView(info = info, selectedBadge = selectedBadge) {
                viewModel.updateMySelectBadge(
                    selectedBadge?.id ?: myWaveInfo.value?.badges?.first()?.id ?: -1
                )
            }

            selectedBadge?.let { badge ->
                ContentView(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally),
                    info = info,
                    selectedBadge = badge,
                    badgeSelected = {
                        selectedBadge = it
                    })
            }
        }
    }
}

@Composable
private fun HeaderView(info: MyWaverTotalInfo, selectedBadge: MyBadge?, onBackPressed: () -> Unit) {
    val currentBadgeInfo = info.badges.firstOrNull { it.imgUrl == selectedBadge?.imgUrl }
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 28.dp)
    ) {
        val (closeButton, title, currentBadge, logo) = createRefs()

        IconButton(
            image = CommonR.drawable.btn_40_close,
            contentDescription = stringResource(id = CommonR.string.closeDesc),
            modifier = Modifier
                .padding(start = 14.dp, top = 6.dp, bottom = 6.dp)
                .size(40.dp)
                .constrainAs(closeButton) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                },
            onClick = { onBackPressed() }
        )

        MyText(
            text = stringResource(id = R.string.myWave),
            modifier = Modifier
                .padding(start = 28.dp, top = 24.dp, end = 16.dp)
                .constrainAs(title) {
                    top.linkTo(closeButton.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(currentBadge.start)
                    width = Dimension.fillToConstraints
                },
            color = Gray10,
            textAlign = TextAlign.Start,
            fontWeight = FontWeight.Bold,
            fontSize = dpToSp(dp = 24.dp)
        )

        Image(
            painter = rememberAsyncImagePainter(
                model = currentBadgeInfo?.imgUrl, // TODO : 이미지로 변경 필요
                placeholder = painterResource(CommonR.drawable.badge_placeholder),
                error = painterResource(CommonR.drawable.badge_placeholder)
            ),
            contentDescription = currentBadgeInfo?.title,
            modifier = Modifier
                .padding(top = 12.dp, end = 32.dp)
                .size(60.dp)
                .constrainAs(currentBadge) {
                    end.linkTo(parent.end)
                    top.linkTo(closeButton.bottom)
                },
            contentScale = ContentScale.Crop
        )

        Image(
            painter = painterResource(id = CommonR.drawable.img_wave_02),
            contentDescription = null,
            modifier = Modifier
                .sizeIn(60.dp)
                .constrainAs(logo) {
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                }
                .padding(top = 30.dp)
        )
    }
}

@Composable
private fun ContentView(
    info: MyWaverTotalInfo,
    selectedBadge: MyBadge,
    badgeSelected: (MyBadge) -> Unit,
    modifier: Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier
            .padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(bottom = 40.dp)
    ) {

        items(1, span = { _ ->
            GridItemSpan(3)
        }) {
            MyWaveStatusView(info)
        }

        items(items = info.badges, key = { it.title + it.step + UUID.randomUUID() }) { info ->
            GridItemSpan(1)
            BadgeView(
                info = info,
                selectedBadge = selectedBadge,
                badgeSelected = {
                    badgeSelected(info)
                }
            )
        }
    }
}

@Composable
private fun MyWaveStatusView(info: MyWaverTotalInfo, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatusView(
                count = info.totalBadgeCount,
                text = stringResource(id = R.string.badgeCount)
            )
            StatusView(count = info.totalLikeCount, text = stringResource(id = R.string.likedCount))
            StatusView(
                count = info.totalBucketCount,
                text = stringResource(id = R.string.bucketCount)
            )
        }

        Spacer(modifier = Modifier.height(36.dp))
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            color = Gray3
        )
    }
}

@Composable
private fun StatusView(count: Int, text: String) {
    Column(modifier = Modifier.width(80.dp)) {
        MyText(
            text = count.toString(),
            color = Gray10,
            fontSize = dpToSp(dp = 24.dp),
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        MyText(text = text, color = Gray10, fontSize = dpToSp(dp = 16.dp))
    }
}

@Composable
private fun BadgeView(
    info: MyBadge,
    selectedBadge: MyBadge,
    badgeSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (info == selectedBadge) Main3 else Gray3
    val textColor = if (info == selectedBadge) Main4 else Gray10

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .background(color = Gray1, shape = RoundedCornerShape(8.dp))
            .border(
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(width = 1.dp, color = borderColor)
            )
            .clickable(info.step != MyBadge.Step.STEP0) {
                badgeSelected()
            }
            .padding(top = 16.dp, bottom = 12.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(
                model = info.imgUrl, // TODO : 이미지 적용 필요
                placeholder = painterResource(CommonR.drawable.badge_placeholder),
                error = painterResource(CommonR.drawable.badge_placeholder)
            ),
            contentDescription = info.title,
            modifier = Modifier
                .padding(horizontal = 23.dp)
                .size(50.dp),
            contentScale = ContentScale.Crop
        )

        MyText(
            text = info.title,
            color = textColor,
            fontSize = dpToSp(dp = 16.dp),
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 8.dp)
        )

        MyText(
            text = info.step.text(),
            color = Gray8,
            fontSize = dpToSp(dp = 14.dp),
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun HeaderPreview() {

    val info = MyWaverTotalInfo(
        totalBadgeCount = 100,
        totalLikeCount = 2,
        totalBucketCount = 30,
        badgeImgUrl = "",
        badges = listOf(
            MyBadge(1, "요리", "1", MyBadge.Step.STEP0),
            MyBadge(2, "요리", "2", MyBadge.Step.STEP1),
            MyBadge(3, "요리", "3", MyBadge.Step.STEP2)
        )
    )

    Column {
        HeaderView(info, selectedBadge = MyBadge(1, "음식", "2", MyBadge.Step.STEP1)) {}
        ContentView(
            info = info,
            selectedBadge = info.badges.first(),
            badgeSelected = {},
            Modifier.fillMaxWidth()
        )
    }
}
