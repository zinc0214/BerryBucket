package com.zinc.waver.ui_my.screen.all

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.zinc.common.models.BucketStatus
import com.zinc.common.models.BucketType
import com.zinc.common.models.ExposureStatus
import com.zinc.waver.model.MyTabType
import com.zinc.waver.model.UIBucketInfoSimple
import com.zinc.waver.ui.design.theme.Gray2
import com.zinc.waver.ui.presentation.component.TitleIconType
import com.zinc.waver.ui.presentation.component.TitleView
import com.zinc.waver.ui_my.SimpleBucketListView
import com.zinc.waver.ui_my.viewModel.MyViewModel
import com.zinc.waver.ui_common.R as CommonR

@Composable
fun StatusBucketListScreen(
    status: BucketStatus,
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit,
    bucketItemClicked: (String, Boolean) -> Unit,
    viewModel: MyViewModel = hiltViewModel()
) {
    val bucketListAsState by viewModel.allBucketItem.observeAsState()
    val apiFailed by viewModel.dataLoadFailed.observeAsState()

    var bucketList by remember { mutableStateOf(bucketListAsState?.bucketList) }
    var showApiLoadError by remember { mutableStateOf(false) }

    val title =
        if (status == BucketStatus.PROGRESS) CommonR.string.proceedingText else CommonR.string.succeedText

    LaunchedEffect(Unit) {
        viewModel.loadStatusBucketList(status = status)
    }

    LaunchedEffect(bucketListAsState) {
        bucketList = bucketListAsState?.bucketList
    }

    LaunchedEffect(apiFailed) {
        showApiLoadError = apiFailed ?: false
    }

    bucketList?.let {
        StatusBucketListScreen(
            modifier = modifier,
            title = stringResource(title),
            bucketList = it,
            onBackPressed = onBackPressed,
            bucketItemClicked = bucketItemClicked,
            achieveBucketClicked = { bucketId ->
                viewModel.achieveBucket(bucketId, MyTabType.ALL)
            }
        )
    }
}


@Composable
private fun StatusBucketListScreen(
    modifier: Modifier = Modifier,
    title: String,
    bucketList: List<UIBucketInfoSimple>,
    onBackPressed: () -> Unit,
    bucketItemClicked: (String, Boolean) -> Unit,
    achieveBucketClicked: (String) -> Unit
) {
    Column(
        modifier = modifier
            .statusBarsPadding()
            .background(Gray2)
            .fillMaxSize(),
    ) {

        TitleView(
            title = title,
            leftIconType = TitleIconType.BACK,
            isDividerVisible = true,
            onLeftIconClicked = {
                onBackPressed()
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        SimpleBucketListView(
            bucketList = bucketList,
            tabType = MyTabType.ALL,
            showDday = true,
            itemClicked = { info ->
                bucketItemClicked(info.id, true)
            },
            achieveClicked = achieveBucketClicked
        )

        Spacer(modifier = Modifier.height(64.dp))
    }
}

@Preview(showSystemUi = true)
@Composable
private fun StatusBucketListScreenPreview() {
    StatusBucketListScreen(
        title = "진행 중",
        bucketList = listOf(
            UIBucketInfoSimple(
                id = "1",
                title = "버킷리스트으으으",
                currentCount = 1,
                goalCount = 2,
                dDay = null,
                bucketType = BucketType.ORIGINAL,
                exposureStatues = ExposureStatus.PUBLIC,
                status = BucketStatus.PROGRESS,
                isChallenge = false
            ),
            UIBucketInfoSimple(
                id = "2",
                title = "버킷리스트으으으",
                currentCount = 1,
                goalCount = 10,
                dDay = null,
                bucketType = BucketType.ORIGINAL,
                exposureStatues = ExposureStatus.PUBLIC,
                status = BucketStatus.PROGRESS,
                isChallenge = false
            )
        ),
        onBackPressed = {},
        bucketItemClicked = { _, _ -> },
        achieveBucketClicked = {}
    )
}