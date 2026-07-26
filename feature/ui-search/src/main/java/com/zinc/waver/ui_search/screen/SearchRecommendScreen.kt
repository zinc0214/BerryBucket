package com.zinc.waver.ui_search.screen

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.zinc.waver.ui_search.component.RecommendListView
import com.zinc.waver.ui_search.component.RecommendTopBar
import com.zinc.waver.ui_search.component.SearchRecommendLoading
import com.zinc.waver.ui_search.model.SearchGoToEvent
import com.zinc.waver.ui_search.viewmodel.SearchViewModel
import com.zinc.waver.ui_common.R as CommonR


@Composable
fun SearchRecommendScreen(
    onSearchEvent: (SearchGoToEvent) -> Unit
) {
    val viewModel: SearchViewModel = hiltViewModel()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadRecommendList()
    }

    val recommendList by viewModel.recommendList.observeAsState()
    val copySucceedAsState by viewModel.copySucceed.observeAsState()

    LaunchedEffect(copySucceedAsState) {
        if (copySucceedAsState == true) {
            Toast.makeText(
                context,
                CommonR.string.bucketCopySucceedToast,
                Toast.LENGTH_SHORT
            ).show()
            viewModel.consumeCopySucceed()
        }
    }

    var isFirstItemShown by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        RecommendTopBar(
            modifier = Modifier
                .fillMaxWidth(),
            isFirstItemShown = isFirstItemShown,
            editViewClicked = {
                onSearchEvent.invoke(SearchGoToEvent.GoToSearch)
            }
        )

        if (recommendList == null) {
            SearchRecommendLoading()
        } else {
            recommendList?.let { list ->
                Log.e("ayhan", "recommendList : $list")
                RecommendListView(
                    recommendList = list,
                    bucketClicked = { bucketId, userId, isMine ->
                        onSearchEvent.invoke(
                            SearchGoToEvent.GoToOpenBucket(
                                bucketId,
                                userId,
                                isMine
                            )
                        )
                    },
                    isFirstItemShown = { isTop ->
                        isFirstItemShown = isTop
                    },
                    copyBucket = {
                        viewModel.copyOtherBucket(it)
                    },
                    onRefreshClicked = {
                        viewModel.loadRecommendList()
                    }
                )
            }
        }
    }

}