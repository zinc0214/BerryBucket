package com.zinc.waver.ui_my.screen.category.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.zinc.waver.model.CategoryLoadFailStatus
import com.zinc.waver.model.MyTabType
import com.zinc.waver.model.UICategoryInfo
import com.zinc.waver.ui.design.theme.Gray2
import com.zinc.waver.ui.presentation.component.TitleIconType
import com.zinc.waver.ui.presentation.component.TitleView
import com.zinc.waver.ui.presentation.component.dialog.ApiFailDialog
import com.zinc.waver.ui.viewmodel.CategoryViewModel
import com.zinc.waver.ui_common.R
import com.zinc.waver.ui_my.SimpleBucketCard
import com.zinc.waver.ui_my.screen.category.component.MyCategoryBucketListBlankView
import com.zinc.waver.ui_my.viewModel.MyViewModel

@Composable
fun CategoryBucketListScreen(
    _categoryInfo: UICategoryInfo,
    onBackPressed: () -> Unit,
    bucketItemClicked: (String, Boolean) -> Unit,
    goToAddBucket: () -> Unit,
    viewModel: CategoryViewModel = hiltViewModel(),
    myViewModel: MyViewModel = hiltViewModel()
) {

    val _bucketList by viewModel.categoryBucketList.observeAsState()
    val apiFailed by viewModel.loadFail.observeAsState()

    val categoryInfo = remember {
        mutableStateOf(_categoryInfo)
    }
    val bucketList = remember {
        mutableStateOf(_bucketList)
    }
    val showApiFail = remember {
        mutableStateOf(false)
    }

    LaunchedEffect(key1 = _categoryInfo) {
        categoryInfo.value = _categoryInfo
    }

    LaunchedEffect(key1 = _bucketList) {
        bucketList.value = _bucketList
    }

    LaunchedEffect(key1 = apiFailed) {
        if (apiFailed is CategoryLoadFailStatus.BucketLoadFail) {
            showApiFail.value = true
        } else {
            showApiFail.value = false
        }
    }

    // 컴포지션 본문에서 호출하면 리컴포지션마다 다시 요청한다.
    LaunchedEffect(categoryInfo.value.id) {
        if (bucketList.value.isNullOrEmpty()) {
            viewModel.loadCategoryBucketList(categoryInfo.value.id)
        }
    }

    bucketList.value?.let { data ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .statusBarsPadding()
                .background(Gray2)
        ) {
            TitleView(
                title = categoryInfo.value.name,
                leftIconType = TitleIconType.BACK,
                isDividerVisible = true,
                onLeftIconClicked = {
                    onBackPressed()
                }
            )

            if (data.isEmpty()) {
                MyCategoryBucketListBlankView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    categoryName = _categoryInfo.name
                ) {
                    goToAddBucket()
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        bottom = 60.dp,
                        top = 24.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(data, key = { _, item -> item.id }) { _, bucket ->
                        SimpleBucketCard(
                            itemInfo = bucket,
                            tabType = MyTabType.ALL,
                            isShowDday = false,
                            itemClicked = { item ->
                                bucketItemClicked.invoke(
                                    item.id,
                                    item.isPrivate()
                                )
                            },
                            achieveAnimDone = { myViewModel.achieveBucket(it, MyTabType.ALL) }
                        )
                    }
                }
            }
        }
    }

    if (showApiFail.value) {
        ApiFailDialog(
            title = stringResource(R.string.apiFailTitle),
            message = stringResource(id = R.string.apiFailMessage),
            dismissEvent = {
                showApiFail.value = false
                onBackPressed()
            })
    }
}