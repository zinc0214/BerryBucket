package com.zinc.waver.ui.presentation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import com.zinc.common.models.BucketStatus
import com.zinc.common.models.ExposureStatus
import com.zinc.waver.model.HomeItemSelected
import com.zinc.waver.model.MySearchClickEvent
import com.zinc.waver.ui.design.theme.BaseTheme
import com.zinc.waver.ui.design.theme.Gray1
import com.zinc.waver.ui.presentation.home.HomeBottomBar
import com.zinc.waver.ui.presentation.home.HomeSections
import com.zinc.waver.ui.presentation.model.ActionWithActivity
import com.zinc.waver.ui_detail.model.CloseBucketDetailEvent
import com.zinc.waver.ui_detail.model.OpenBucketDetailEvent2
import com.zinc.waver.ui_more.models.MoreItemType
import com.zinc.waver.ui_more.models.MoreItemType.ALARM
import com.zinc.waver.ui_more.models.MoreItemType.APP_INFO
import com.zinc.waver.ui_more.models.MoreItemType.LOGOUT
import com.zinc.waver.ui_more.models.MoreItemType.PROFILE
import com.zinc.waver.ui_more.models.MoreItemType.WAVE_PLUS
import com.zinc.waver.ui_my.BottomSheetScreenType
import com.zinc.waver.ui_my.model.AlarmClickEvent
import com.zinc.waver.ui_my.model.MyTopEvent
import com.zinc.waver.ui_other.model.OtherHomeEvent
import com.zinc.waver.ui_search.model.SearchGoToEvent
import com.zinc.waver.util.nav.alarmNavGraph
import com.zinc.waver.util.nav.closeBucketDetailNavGraph
import com.zinc.waver.util.nav.goToOtherHomeNavGraph
import com.zinc.waver.util.nav.homeCategoryBucketListNavGraph
import com.zinc.waver.util.nav.homeCategoryEditNavGraph
import com.zinc.waver.util.nav.homeFeed
import com.zinc.waver.util.nav.homeMore
import com.zinc.waver.util.nav.homeMy
import com.zinc.waver.util.nav.homeMyBucketSearch
import com.zinc.waver.util.nav.homeSearch
import com.zinc.waver.util.nav.homeStatusBucketListNavGraph
import com.zinc.waver.util.nav.moreAlarmNavGraph
import com.zinc.waver.util.nav.moreAppInfoNavGraph
import com.zinc.waver.util.nav.moreProfileNavGraph
import com.zinc.waver.util.nav.myFollowerListNavGraph
import com.zinc.waver.util.nav.myFollowerSettingNavGraph
import com.zinc.waver.util.nav.myFollowingListNavGraph
import com.zinc.waver.util.nav.myFollowingSettingNavGraph
import com.zinc.waver.util.nav.myWaveManageNavGraph
import com.zinc.waver.util.nav.openBucketDetailNavGraph
import com.zinc.waver.util.nav.searchDirectNavGraph
import com.zinc.waver.util.nav.wavePlusGuideNavGraph
import com.zinc.waver.util.nav.writeNavGraph

@Composable
fun HomeScreen(
    action: (ActionWithActivity) -> Unit,
    startDestination: String = HomeSections.MY.route
) {
    BaseTheme {
        val appState = rememberWaverAppState()
        val shownBottomSheet = remember {
            mutableStateOf(false)
        }

        Column(
            modifier = Modifier
                .background(color = Gray1)
                .navigationBarsPadding()
        ) {
            HomeScaffold(
                appState = appState,
                shownBottomSheet = shownBottomSheet,
                action = action,
                startDestination = startDestination
            )
        }
    }
}

@Composable
private fun HomeScaffold(
    appState: WaverAppState,
    shownBottomSheet: MutableState<Boolean>,
    action: (ActionWithActivity) -> Unit,
    startDestination: String = HomeSections.MY.route
) {
    Scaffold(
        bottomBar = {
            HomeBottomBarContent(
                appState = appState,
                shownBottomSheet = shownBottomSheet
            )
        },
        scaffoldState = appState.scaffoldState
    ) { padding ->
        HomeNavHost(
            appState = appState,
            shownBottomSheet = shownBottomSheet,
            action = action,
            padding = padding,
            startDestination = startDestination
        )
    }
}

@Composable
private fun HomeBottomBarContent(
    appState: WaverAppState,
    shownBottomSheet: MutableState<Boolean>
) {
    if (appState.shouldShowBottomBar && shownBottomSheet.value.not()) {
        HomeBottomBar(
            tabs = appState.bottomBarTabs,
            currentRoute = appState.currentRoute!!,
            navigateToRoute = {
                appState.navigateToBottomBarRoute(it)
                appState.currentHomeRoute.value = it
            }
        ) {
            appState.navigateToWriteBucket(
                "NoId",
                appState.navController.currentBackStackEntry!!
            )
        }
    }
}

@Composable
private fun HomeNavHost(
    appState: WaverAppState,
    shownBottomSheet: MutableState<Boolean>,
    action: (ActionWithActivity) -> Unit,
    padding: PaddingValues,
    startDestination: String = HomeSections.MY.route
) {
    NavHost(
        navController = appState.navController,
        startDestination = startDestination,
        enterTransition = {
            EnterTransition.None
        },
        exitTransition = {
            ExitTransition.None
        },
        modifier = Modifier.padding(padding)
    ) {
        setupMainScreens(appState, shownBottomSheet, action)
        setupDetailScreens(appState, action)
        setupMoreScreens(appState, action)
    }
}

private fun NavGraphBuilder.setupMainScreens(
    appState: WaverAppState,
    shownBottomSheet: MutableState<Boolean>,
    action: (ActionWithActivity) -> Unit
) {
    // Feed Screen
    homeFeed(onFeedClicked = { bucketId, userId, isMine, nav ->
        appState.navigateToOpenBucketDetail(bucketId, userId, isMine, nav)
    })

    // Search Screen
    homeSearch(
        onSearchEvent = { event, nav ->
            handleSearchEvent(event, nav, appState)
        }
    )

    // More Screen
    homeMore(
        moreItemClicked = { type, nav ->
            handleMoreItemClick(type, nav, appState, action)
        },
        backPress = appState::backPress
    )

    // My Screen
    homeMy(
        onBucketSelected = { selected, nav ->
            handleBucketSelected(selected, nav, appState)
        },
        bottomSheetClicked = { event, nav ->
            handleBottomSheetClick(event, nav, appState, shownBottomSheet)
        },
        myTopEvent = { event, nav ->
            handleMyTopEvent(event, nav, appState)
        },
        goToCategoryEdit = { nav ->
            appState.navigateToCategoryEdit(nav)
        }
    )

    // My Bucket Search
    homeMyBucketSearch { event, nav ->
        handleMySearchEvent(event, nav, appState, shownBottomSheet)
    }
}

private fun NavGraphBuilder.setupDetailScreens(
    appState: WaverAppState,
    action: (ActionWithActivity) -> Unit
) {
    // Status Bucket List
    homeStatusBucketListNavGraph(
        backPress = appState::backPress,
        bucketClicked = { id, isPrivate, nav ->
            handleBucketClick(id, isPrivate, nav, appState)
        }
    )

    // Category Bucket List
    homeCategoryBucketListNavGraph(
        backPress = appState::backPress,
        bucketClicked = { id, isPrivate, nav ->
            handleBucketClick(id, isPrivate, nav, appState)
        },
        goToAddBucket = {
            appState.navigateToWriteBucket("NoId", it)
        }
    )

    // Category Edit
    homeCategoryEditNavGraph(backPress = appState::backPress)

    // Following List
    myFollowingListNavGraph(
        backPress = appState::backPress,
        goToSetting = { nav ->
            appState.navigateToFollowingSettingList(from = nav)
        },
        goToOtherHome = { nav, id ->
            appState.navigateToOtherHome(nav, id)
        }
    )

    // Following Setting
    myFollowingSettingNavGraph(
        backPress = appState::backPress,
        goToOtherHome = { nav, id ->
            appState.navigateToOtherHome(nav, id)
        }
    )

    // Follower List
    myFollowerListNavGraph(
        backPress = appState::backPress,
        goToSetting = {
            appState.navigateToFollowerSettingList(it)
        },
        goToOtherHome = { nav, id ->
            appState.navigateToOtherHome(nav, id)
        }
    )

    // Follower Setting
    myFollowerSettingNavGraph(
        backPress = appState::backPress,
        goToOtherHome = { nav, id ->
            appState.navigateToOtherHome(nav, id)
        }
    )

    // Open Bucket Detail
    openBucketDetailNavGraph(
        goToBucketDetailEvent = { eventInfo, nav ->
            handleOpenBucketDetailEvent(eventInfo, nav, appState)
        },
        backPress = appState::backPress
    )

    // Close Bucket Detail
    closeBucketDetailNavGraph(
        goToBucketDetailEvent = { eventInfo, _ ->
            handleCloseBucketDetailEvent(eventInfo, appState)
        },
        backPress = appState::backPress
    )

    // Search Direct
    searchDirectNavGraph(
        backPress = appState::backPress,
        searchEvent = { event, nav ->
            handleSearchEvent(event, nav, appState)
        }
    )

    // Write
    writeNavGraph(
        action = { actionType ->
            action(actionType)
        },
        backPress = {
            appState.navigateToBottomBarRoute(appState.currentHomeRoute.value)
        },
        goToAddCategory = {
            appState.navigateToCategoryEdit(it)
        },
        goToHome = {
            appState.navigateToBottomBarRoute(appState.currentHomeRoute.value)
        }
    )

    // Other Home
    goToOtherHomeNavGraph { event, from ->
        handleOtherHomeEvent(event, from, appState)
    }
}

private fun NavGraphBuilder.setupMoreScreens(
    appState: WaverAppState,
    action: (ActionWithActivity) -> Unit
) {
    alarmNavGraph(
        backPress = appState::backPress,
        alarmClicked = { event, nav ->
            when (event) {
                is AlarmClickEvent.GoToBucketDetail -> {
                    appState.navigateToOpenBucketDetail(event.bucketId, "NoId", true, nav)
                }

                AlarmClickEvent.GoToFollowerList -> {
                    appState.navigateToFollowerList(nav)
                }

                AlarmClickEvent.GoToBadgeList -> {
                    appState.navigateToMyWaveManage(nav)
                }
            }
        }
    )
    moreAlarmNavGraph(backPress = appState::backPress)
    moreProfileNavGraph(
        backPress = appState::backPress,
        action = { action(it) }
    )
    moreAppInfoNavGraph(
        backPress = appState::backPress,
        onAppInfoItemClicked = { /* Do nothing */ },
        withdrawEvent = {
            action(ActionWithActivity.Logout)
        }
    )
    myWaveManageNavGraph(backPress = appState::backPress)
    wavePlusGuideNavGraph(
        backPress = appState::backPress,
        inAppBillingShow = { action.invoke(ActionWithActivity.InAppBilling(it)) }
    )
}

// Event Handler Functions
private fun handleSearchEvent(
    event: SearchGoToEvent,
    nav: NavBackStackEntry,
    appState: WaverAppState
) {
    when (event) {
        SearchGoToEvent.GoToSearch -> {
            appState.navigateToSearch(nav)
        }

        is SearchGoToEvent.GoToOpenBucket -> {
            appState.navigateToOpenBucketDetail(
                event.bucketId,
                event.userId,
                event.isMine,
                nav
            )
        }

        is SearchGoToEvent.GoToOtherUser -> {
            appState.navigateToOtherHome(nav, event.id)
        }
    }
}

private fun handleMoreItemClick(
    type: MoreItemType,
    nav: NavBackStackEntry,
    appState: WaverAppState,
    action: (ActionWithActivity) -> Unit
) {
    when (type) {
        ALARM -> appState.navigateToMoreAlarmSetting(nav)
        APP_INFO -> appState.navigateToMoreAppInfo(nav)
        PROFILE -> appState.navigateToMoreProfileSetting(nav)
        MoreItemType.MY_WAVE -> appState.navigateToMyWaveManage(nav)
        WAVE_PLUS -> appState.navigateToWavePlusGuide(nav)
        MoreItemType.CS_QNA -> action(ActionWithActivity.GoToQNAEmail)
        LOGOUT -> action(ActionWithActivity.Logout)
        else -> { /* Do Nothing */
        }
    }
}

private fun handleBucketSelected(
    selected: HomeItemSelected,
    nav: NavBackStackEntry,
    appState: WaverAppState
) {
    when (selected) {
        is HomeItemSelected.GoToDetailHomeItem -> {
            if (selected.bucketInfo.exposureStatues == ExposureStatus.PRIVATE) {
                appState.navigateToCloseBucketDetail(selected.bucketInfo.id, nav)
            } else {
                appState.navigateToOpenBucketDetail(
                    selected.bucketInfo.id, "NoId", true, nav
                )
            }
        }

        is HomeItemSelected.GoToCategoryBucketList -> {
            appState.navigateToCategoryBucketList(selected.categoryInfo, nav)
        }

        is HomeItemSelected.GoToStatusBucketList -> {
            appState.navigateToStatusBucketList(
                isProgress = selected.bucketStatus == BucketStatus.PROGRESS,
                from = nav
            )
        }
    }
}

private fun handleBottomSheetClick(
    event: BottomSheetScreenType,
    nav: NavBackStackEntry,
    appState: WaverAppState,
    shownBottomSheet: MutableState<Boolean>
) {
    when (event) {
        is BottomSheetScreenType.MyBucketSearchScreen -> {
            appState.navigateToMySearch(event.selectTab, nav)
        }

        is BottomSheetScreenType.MyBucketFilterScreen -> {
            shownBottomSheet.value = event.needToShown
        }
    }
}

private fun handleMyTopEvent(
    event: MyTopEvent,
    nav: NavBackStackEntry,
    appState: WaverAppState
) {
    when (event) {
        MyTopEvent.Alarm -> appState.navigateToAlarm(nav)
        MyTopEvent.Follower -> appState.navigateToFollowerList(nav)
        MyTopEvent.Following -> appState.navigateToFollowingList(nav)
    }
}

private fun handleMySearchEvent(
    event: MySearchClickEvent,
    nav: NavBackStackEntry,
    appState: WaverAppState,
    shownBottomSheet: MutableState<Boolean>
) {
    when (event) {
        is MySearchClickEvent.BucketItemClicked -> {
            if (event.isPrivate) {
                appState.navigateToCloseBucketDetail(event.id, nav)
            } else {
                appState.navigateToOpenBucketDetail(event.id, "NoId", true, nav)
            }
        }

        is MySearchClickEvent.CategoryItemClicked -> {
            // TODO: 다음은 여기하자!!!!
        }

        is MySearchClickEvent.CloseClicked -> {
            shownBottomSheet.value = false
            appState.backPress()
        }
    }
}

private fun handleBucketClick(
    id: String,
    isPrivate: Boolean,
    nav: NavBackStackEntry,
    appState: WaverAppState
) {
    if (isPrivate) {
        appState.navigateToCloseBucketDetail(id, nav)
    } else {
        appState.navigateToOpenBucketDetail(id, "NoId", true, nav)
    }
}

private fun handleOpenBucketDetailEvent(
    eventInfo: OpenBucketDetailEvent2,
    nav: NavBackStackEntry,
    appState: WaverAppState
) {
    when (eventInfo) {
        is OpenBucketDetailEvent2.Update -> {
            appState.navigateToWriteBucket(
                eventInfo.info.bucketId ?: "NoId",
                appState.navController.currentBackStackEntry!!
            )
        }

        is OpenBucketDetailEvent2.GoToOtherProfile -> {
            appState.navigateToOtherHome(nav, eventInfo.id)
        }
    }
}

private fun handleCloseBucketDetailEvent(
    eventInfo: CloseBucketDetailEvent,
    appState: WaverAppState
) {
    when (eventInfo) {
        is CloseBucketDetailEvent.Update -> {
            appState.navigateToWriteBucket(
                eventInfo.info.bucketId ?: "NoId",
                appState.navController.currentBackStackEntry!!
            )
        }
    }
}

private fun handleOtherHomeEvent(
    event: OtherHomeEvent,
    from: NavBackStackEntry,
    appState: WaverAppState
) {
    when (event) {
        is OtherHomeEvent.GoToBack -> appState.backPress()
        is OtherHomeEvent.GoToOtherBucket -> appState.navigateToOpenBucketDetail(
            bucketId = event.bucketId.toString(),
            writerId = event.writerId,
            isMine = false,
            from = from
        )
    }
}
