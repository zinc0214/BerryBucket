package com.zinc.waver.ui_search.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.zinc.waver.ui.presentation.component.SearchEditView
import com.zinc.waver.ui.presentation.component.dialog.ApiFailDialog
import com.zinc.waver.ui_search.R
import com.zinc.waver.ui_search.component.SearchRecentView
import com.zinc.waver.ui_search.component.SearchResultBlankView
import com.zinc.waver.ui_search.component.SearchResultView
import com.zinc.waver.ui_search.component.SearchTopAppBar
import com.zinc.waver.ui_search.model.SearchActionEvent
import com.zinc.waver.ui_search.model.SearchGoToEvent
import com.zinc.waver.ui_search.viewmodel.SearchViewModel
import com.zinc.waver.ui_common.R as CommonR

@Composable
fun SearchScreen(
    closeEvent: () -> Unit,
    searchEvent: (SearchGoToEvent) -> Unit
) {
    val viewModel: SearchViewModel = hiltViewModel()
    val context = LocalContext.current

    val searchRecommendItems by viewModel.searchRecentlyItems.observeAsState()
    val searchResultItemsAsState by viewModel.searchResultItems.observeAsState()
    val loadFail by viewModel.loadFail.observeAsState()

    val listScrollState = rememberLazyListState()
    var searchResultItems by remember { mutableStateOf(searchResultItemsAsState) }
    var searchWord by remember { mutableStateOf(viewModel.prevSearchWord) }
    var showFailDialog by remember { mutableStateOf(null as String?) }

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

    LaunchedEffect(Unit) {
        viewModel.loadSearchRecentItems()
    }

    LaunchedEffect(searchResultItemsAsState) {
        searchResultItems = searchResultItemsAsState
    }

    LaunchedEffect(searchWord) {
        if (searchWord.isEmpty()) {
            searchResultItems = null
        }
    }

    LaunchedEffect(key1 = loadFail) {
        if (loadFail != null) {
            showFailDialog = loadFail
        }
    }

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        val (topAppBar, searchResultView) = createRefs()

        SearchTopAppBar(
            listState = listScrollState,
            title = searchWord,
            closeClicked = {
                closeEvent.invoke()
            },
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(topAppBar) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                }
        )

        LazyColumn(
            modifier = Modifier
                .constrainAs(searchResultView) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(topAppBar.bottom)
                    bottom.linkTo(parent.bottom)
                    height = Dimension.fillToConstraints
                },
            contentPadding = PaddingValues(
                top = 16.dp, bottom = 70.dp
            ),
            state = listScrollState
        ) {
            item {
                SearchEditView(
                    goToSearch = { word ->
                        viewModel.loadSearchResult(word)
                        searchWord = word
                    },
                    currentSearchWord = searchWord
                )
            }

            // 검색 결과가 없는 경우
            if (searchWord.isNotEmpty() && searchResultItems?.hasItems() == false) {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(), // 부모(LazyColumn)의 남은 영역을 채움
                        contentAlignment = Alignment.Center // 내부 컨텐츠를 정중앙으로 배치
                    ) {
                        SearchResultBlankView(
                            modifier = Modifier
                                .wrapContentSize()
                                .padding(bottom = 100.dp) // 필요하면 크기 조정
                        )
                    }
                }
            }

            // 최근 검색어 + 추천 키워드 화면
            if (searchWord.isEmpty()) {
                item {
                    searchRecommendItems?.let {
                        SearchRecentView(
                            modifier = Modifier.fillMaxSize(),
                            recentWords = it,
                            itemClicked = { selectWord ->
                                searchWord = selectWord
                                viewModel.loadSearchResult(selectWord)
                            },
                            recentItemDelete = { deleteItem ->
                                viewModel.deleteRecentWord(deleteItem)
                            }
                        )
                    }
                }
            }

            // 검색 결과
            if (searchResultItems?.hasItems() == true) {
                item {
                    searchResultItems?.let {
                        SearchResultView(
                            resultItems = it,
                            modifier = Modifier.animateItem(),
                            goToEvent = searchEvent,
                            actionEvent = { event ->
                                when (event) {
                                    is SearchActionEvent.RequestFollow -> {
                                        viewModel.requestFollow(event.userId, event.alreadyFollowed)
                                    }

                                    is SearchActionEvent.RequestCopy -> {
                                        viewModel.copyOtherBucket(event.bucketId)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showFailDialog != null) {
        val message =
            if (showFailDialog.isNullOrBlank()) stringResource(CommonR.string.retryDesc) else showFailDialog
        ApiFailDialog(
            title = stringResource(id = R.string.searchLoadFail),
            message = message,
            dismissEvent = {
                showFailDialog = null
                closeEvent()
            })
    }

}