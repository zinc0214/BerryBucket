package com.zinc.berrybucket.ui_my

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.zinc.berrybucket.model.HomeItemSelected
import com.zinc.berrybucket.model.MyPagerClickEvent
import com.zinc.berrybucket.model.MyTabType
import com.zinc.berrybucket.model.MyTabType.ALL
import com.zinc.berrybucket.model.MyTabType.CATEGORY
import com.zinc.berrybucket.model.MyTabType.DDAY
import com.zinc.berrybucket.ui.design.theme.BaseTheme
import com.zinc.berrybucket.ui.design.theme.Gray1
import com.zinc.berrybucket.ui.design.theme.Gray10
import com.zinc.berrybucket.ui.design.theme.Gray6
import com.zinc.berrybucket.ui.presentation.component.MyText
import com.zinc.berrybucket.ui.util.dpToSp
import com.zinc.berrybucket.ui_my.model.MyTopEvent
import com.zinc.berrybucket.ui_my.screen.all.AllBucketLayer
import com.zinc.berrybucket.ui_my.screen.category.screen.CategoryLayer
import com.zinc.berrybucket.ui_my.screen.dday.DdayBucketLayer
import com.zinc.berrybucket.ui_my.viewModel.MyViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun MyScreen(
    itemSelected: (HomeItemSelected) -> Unit,
    bottomSheetClicked: (BottomSheetScreenType) -> Unit,
    myTopEvent: (MyTopEvent) -> Unit,
    goToCategoryEdit: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val viewModel: MyViewModel = hiltViewModel()

    viewModel.loadProfile()
    val profileInfo by viewModel.profileInfo.observeAsState()

    val tabItems = MyTabType.values()
    val pagerState = rememberPagerState()

    val nestedScrollInterop = rememberNestedScrollInteropConnection()

    profileInfo?.let { profile ->
        rememberSystemUiController().setSystemBarsColor(Gray1)
        BaseTheme {
            AndroidView(modifier = Modifier.fillMaxSize(), factory = { context ->
                MyView(context).apply {
                    setProfileInfo(profile, myTopEvent)
                    setTabView(
                        tabItems = tabItems,
                        pagerState = pagerState,
                        viewModel = viewModel,
                        coroutineScope = coroutineScope,
                        itemSelected = itemSelected,
                        nestedScrollInterop = nestedScrollInterop,
                        goToCategoryEdit = {
                            goToCategoryEdit()
                        },
                        bottomSheetClicked = {
                            bottomSheetClicked.invoke(it)
                        })
                }
            })
        }
    }
}


@Composable
fun MyTabLayer(
    tabItems: List<MyTabType>, pagerState: PagerState, coroutineScope: CoroutineScope
) {

    val tabWidths = remember {
        val tabWidthStateList = mutableStateListOf<Dp>()
        repeat(tabItems.size) {
            tabWidthStateList.add(0.dp)
        }
        tabWidthStateList
    }

    LazyRow(
        modifier = Modifier
            .background(color = Gray1)
            .padding(start = 16.dp, top = 24.dp)
    ) {

        itemsIndexed(items = tabItems, itemContent = { index, tab ->
            MyTab(mySection = tab,
                isSelected = pagerState.currentPage == index,
                tabWidths = tabWidths,
                currentIndex = index,
                isClicked = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                })
        })
    }
}

@Composable
fun MyViewPager(
    tabItems: List<MyTabType>,
    pagerState: PagerState,
    viewModel: MyViewModel,
    coroutineScope: CoroutineScope,
    nestedScrollInterop: NestedScrollConnection,
    itemSelected: (HomeItemSelected) -> Unit,
    bottomSheetClicked: (BottomSheetScreenType) -> Unit,
    goToCategoryEdit: () -> Unit,
) {

    HorizontalPager(
        count = tabItems.size,
        state = pagerState,
        verticalAlignment = Alignment.Top
    ) { page ->
        when (page) {
            0 -> {
                AllBucketLayer(viewModel = viewModel,
                    nestedScrollInterop = nestedScrollInterop,
                    clickEvent = {
                        when (it) {
                            is MyPagerClickEvent.BucketItemClicked -> {
                                itemSelected.invoke(HomeItemSelected.GoToDetailHomeItem(it.info))
                            }

                            is MyPagerClickEvent.SearchClicked -> {
                                coroutineScope.launch {
                                    bottomSheetClicked.invoke(
                                        BottomSheetScreenType.SearchScreen(
                                            selectTab = ALL(), viewModel = viewModel
                                        )
                                    )
                                }
                            }

                            is MyPagerClickEvent.FilterClicked -> {
                                coroutineScope.launch {
                                    bottomSheetClicked.invoke(
                                        BottomSheetScreenType.FilterScreen(
                                            selectTab = ALL(),
                                            viewModel = viewModel
                                        )
                                    )
                                }
                            }

                            is MyPagerClickEvent.AchieveBucketClicked -> {
                                viewModel.achieveBucket(it.id, ALL())
                            }

                            else -> {
                                // Do Nothing
                            }
                        }
                    })
                return@HorizontalPager
            }

            1 -> {
                CategoryLayer(clickEvent = {
                    when (it) {
                        MyPagerClickEvent.CategoryEditClicked -> {
                            goToCategoryEdit()
                        }

                        is MyPagerClickEvent.SearchClicked -> {
                            coroutineScope.launch {
                                bottomSheetClicked.invoke(
                                    BottomSheetScreenType.SearchScreen(
                                        selectTab = CATEGORY(), viewModel = viewModel
                                    )
                                )
                            }
                        }

                        is MyPagerClickEvent.CategoryItemClicked -> {
                            itemSelected.invoke(HomeItemSelected.GoToCategoryBucketList(it.info))
                        }

                        else -> {
                            // Do Nothing
                        }

                    }
                })
                return@HorizontalPager
            }

            2 -> {
                DdayBucketLayer(viewModel = viewModel, clickEvent = {
                    when (it) {
                        is MyPagerClickEvent.BucketItemClicked -> {
                            itemSelected.invoke(HomeItemSelected.GoToDetailHomeItem(it.info))
                        }

                        is MyPagerClickEvent.SearchClicked -> {
                            coroutineScope.launch {
                                bottomSheetClicked.invoke(
                                    BottomSheetScreenType.SearchScreen(
                                        selectTab = DDAY(),
                                        viewModel = viewModel
                                    )
                                )
                            }
                        }

                        is MyPagerClickEvent.FilterClicked -> {
                            coroutineScope.launch {
                                bottomSheetClicked.invoke(
                                    BottomSheetScreenType.FilterScreen(
                                        selectTab = DDAY(),
                                        viewModel = viewModel
                                    )
                                )
                            }
                        }

                        is MyPagerClickEvent.AchieveBucketClicked -> {
                            viewModel.achieveBucket(it.id, DDAY())
                        }

                        else -> {
                            // Do Nothing
                        }
                    }
                })
                return@HorizontalPager
            }
        }
    }
}

@Composable
private fun MyTab(
    mySection: MyTabType,
    isSelected: Boolean,
    tabWidths: SnapshotStateList<Dp>,
    currentIndex: Int,
    isClicked: (Int) -> Unit

) {
    val textStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = dpToSp(15.dp))
    val density = LocalDensity.current

    Column(modifier = Modifier
        .padding(start = 12.dp, end = 12.dp)
        .clickable {
            isClicked(currentIndex)
        }) {
        MyText(text = stringResource(id = mySection.title),
            style = if (isSelected) textStyle.copy(color = Gray10)
            else textStyle.copy(color = Gray6),
            onTextLayout = { textLayoutResult ->
                tabWidths[currentIndex] = with(density) { textLayoutResult.size.width.toDp() }
            })

        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .height(3.dp)
                .width(tabWidths[currentIndex])
                .background(if (isSelected) Gray10 else Color.Transparent)
        )
    }
}