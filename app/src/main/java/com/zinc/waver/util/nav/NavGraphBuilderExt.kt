package com.zinc.waver.util.nav

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.zinc.common.models.BucketStatus
import com.zinc.waver.model.HomeItemSelected
import com.zinc.waver.model.MySearchClickEvent
import com.zinc.waver.model.MyTabType
import com.zinc.waver.model.UICategoryInfo
import com.zinc.waver.ui.presentation.home.HomeSections
import com.zinc.waver.ui.presentation.model.ActionWithActivity
import com.zinc.waver.ui.presentation.model.WaverPlusType
import com.zinc.waver.ui.presentation.screen.category.CategoryEditScreen
import com.zinc.waver.ui.presentation.screen.waverplus.WaverPlusGuideScreen
import com.zinc.waver.ui_detail.model.CloseBucketDetailEvent
import com.zinc.waver.ui_detail.model.OpenBucketDetailEvent2
import com.zinc.waver.ui_detail.screen.OpenDetailBucketListScreen
import com.zinc.waver.ui_feed.FeedScreen
import com.zinc.waver.ui_more.models.AppInfoItemType
import com.zinc.waver.ui_more.models.MoreItemType
import com.zinc.waver.ui_more.screen.AlarmSettingScreen
import com.zinc.waver.ui_more.screen.AppInfoScreen
import com.zinc.waver.ui_more.screen.MoreScreen
import com.zinc.waver.ui_more.screen.MyWaveManageScreen
import com.zinc.waver.ui_more.screen.ProfileSettingScreen
import com.zinc.waver.ui_my.BottomSheetScreenType
import com.zinc.waver.ui_my.MyScreen
import com.zinc.waver.ui_my.SearchBottomView
import com.zinc.waver.ui_my.model.AlarmClickEvent
import com.zinc.waver.ui_my.model.MyTopEvent
import com.zinc.waver.ui_my.screen.alarm.AlarmScreen
import com.zinc.waver.ui_my.screen.all.StatusBucketListScreen
import com.zinc.waver.ui_my.screen.category.screen.CategoryBucketListScreen
import com.zinc.waver.ui_my.screen.profile.FollowerListScreen
import com.zinc.waver.ui_my.screen.profile.FollowerListSettingScreen
import com.zinc.waver.ui_my.screen.profile.FollowingListScreen
import com.zinc.waver.ui_my.screen.profile.FollowingListSettingScreen
import com.zinc.waver.ui_other.model.OtherHomeEvent
import com.zinc.waver.ui_other.screen.OtherHomeScreen
import com.zinc.waver.ui_search.model.SearchGoToEvent
import com.zinc.waver.ui_search.screen.SearchRecommendScreen
import com.zinc.waver.ui_search.screen.SearchScreen
import com.zinc.waver.ui_write.model.WriteEvent
import com.zinc.waver.ui_write.presentation.WriteScreen
import com.zinc.waver.util.nav.MainDestinations.FOLLOWER.MY_FOLLOWER
import com.zinc.waver.util.nav.MainDestinations.FOLLOWER.MY_FOLLOWER_SETTING
import com.zinc.waver.util.nav.MainDestinations.FOLLOWING.MY_FOLLOWING
import com.zinc.waver.util.nav.MainDestinations.FOLLOWING.MY_FOLLOWING_SETTING

internal fun NavGraphBuilder.homeMy(
    onBucketSelected: (HomeItemSelected, NavBackStackEntry) -> Unit,
    bottomSheetClicked: (BottomSheetScreenType, NavBackStackEntry) -> Unit,
    myTopEvent: (MyTopEvent, NavBackStackEntry) -> Unit,
    goToCategoryEdit: (NavBackStackEntry) -> Unit,
) {
    composable(HomeSections.MY.route) { from ->
        MyScreen(
            itemSelected = {
                onBucketSelected.invoke(it, from)
            },
            bottomSheetClicked = {
                bottomSheetClicked.invoke(it, from)
            },
            myTopEvent = {
                myTopEvent.invoke(it, from)
            },
            goToCategoryEdit = {
                goToCategoryEdit.invoke(from)
            }
        )
    }
}

internal fun NavGraphBuilder.homeFeed(
    onFeedClicked: (String, String, Boolean, NavBackStackEntry) -> Unit
) {
    composable(HomeSections.FEED.route) { from ->
        FeedScreen(goToBucket = { bucketId, userId, isMine ->
            onFeedClicked(bucketId, userId, isMine, from)
        })
    }
}

internal fun NavGraphBuilder.homeSearch(
    onSearchEvent: (SearchGoToEvent, NavBackStackEntry) -> Unit,
) {
    composable(HomeSections.SEARCH.route) { nav ->
        SearchRecommendScreen(
            onSearchEvent = {
                onSearchEvent.invoke(it, nav)
            }
        )
    }
}

internal fun NavGraphBuilder.homeMore(
    moreItemClicked: (MoreItemType, NavBackStackEntry) -> Unit,
    backPress: () -> Unit
) {
    composable(HomeSections.MORE.route) { from ->
        MoreScreen(moreItemClicked = {
            moreItemClicked.invoke(it, from)
        }, goToBack = {
            backPress()
        })
    }
}

internal fun NavGraphBuilder.homeMyBucketSearch(
    mySearchClickEvent: (MySearchClickEvent, NavBackStackEntry) -> Unit
) {

    composable(
        route = MainDestinations.MY_SEARCH + "/{${MainDestinations.SELECT_TAB}}",
        arguments = listOf(
            navArgument(MainDestinations.SELECT_TAB) {
                type = SerializableType(
                    type = MyTabType::class.java,
                    parser = MyTabType::parseNavigationValue
                )
            }
        ),
        content = { entry ->
            val arguments = requireNotNull(entry.arguments)
            val selectedTab: MyTabType =
                arguments.extraNotNullSerializable(MainDestinations.SELECT_TAB)

            SearchBottomView(
                tab = selectedTab,
                mySearchClickEvent = {
                    mySearchClickEvent(it, entry)
                }
            )
        }
    )
}

internal fun NavGraphBuilder.homeCategoryBucketListNavGraph(
    backPress: () -> Unit,
    bucketClicked: (String, Boolean, NavBackStackEntry) -> Unit,
    goToAddBucket: (NavBackStackEntry) -> Unit
) {
    composable(
        route = "${MainDestinations.MY_CATEGORY_BUCKET_LIST}/{${MainDestinations.CATEGORY_INFO}}",
        arguments = listOf(
            navArgument(MainDestinations.CATEGORY_INFO) {
                type = SerializableType(
                    type = UICategoryInfo::class.java,
                    parser = UICategoryInfo::parseNavigationValue
                )
            }
        ),
        content = { entry ->
            val arguments = requireNotNull(entry.arguments)
            val categoryInfo: UICategoryInfo =
                arguments.extraNotNullSerializable(MainDestinations.CATEGORY_INFO)

            CategoryBucketListScreen(
                _categoryInfo = categoryInfo,
                onBackPressed = backPress,
                bucketItemClicked = { id, isPrivate ->
                    bucketClicked(id, isPrivate, entry)
                },
                goToAddBucket = {
                    goToAddBucket(entry)
                }
            )
        }
    )
}

internal fun NavGraphBuilder.homeStatusBucketListNavGraph(
    backPress: () -> Unit,
    bucketClicked: (String, Boolean, NavBackStackEntry) -> Unit
) {
    composable(
        route = "${MainDestinations.MY_STATUS_BUCKET_LIST}/{${MainDestinations.STATUS_INFO}}",
        arguments = listOf(
            navArgument(MainDestinations.STATUS_INFO) {
                type = NavType.BoolType
            }
        )) { entry ->
        val arguments = entry.arguments
        val statusInfo = arguments?.getBoolean(MainDestinations.STATUS_INFO) ?: true
        val statusType = if (statusInfo) BucketStatus.PROGRESS else BucketStatus.COMPLETE

        StatusBucketListScreen(
            status = statusType,
            onBackPressed = backPress,
            bucketItemClicked = { id, isPrivate ->
                bucketClicked(id, isPrivate, entry)
            }
        )
    }
}


internal fun NavGraphBuilder.homeCategoryEditNavGraph(backPress: () -> Unit) {
    composable(MainDestinations.MY_CATEGORY_EDIT) { entry ->
        CategoryEditScreen(backClicked = backPress)
    }
}

internal fun NavGraphBuilder.myFollowingListNavGraph(
    backPress: () -> Unit,
    goToSetting: (NavBackStackEntry) -> Unit,
    goToOtherHome: (NavBackStackEntry, String) -> Unit
) {
    composable(MY_FOLLOWING) { nav ->
        FollowingListScreen(
            goToBack = {
                backPress()
            },
            goToSetting = {
                goToSetting(nav)
            },
            goToOtherHome = { id ->
                goToOtherHome(nav, id)
            }
        )
    }
}

internal fun NavGraphBuilder.myFollowingSettingNavGraph(
    backPress: () -> Unit,
    goToOtherHome: (NavBackStackEntry, String) -> Unit
) {
    composable(MY_FOLLOWING_SETTING) { nav ->
        FollowingListSettingScreen(
            goToBack = {
                backPress()
            },
            goToOtherHome = { id -> goToOtherHome(nav, id) }
        )
    }
}

internal fun NavGraphBuilder.myFollowerListNavGraph(
    backPress: () -> Unit,
    goToSetting: (NavBackStackEntry) -> Unit,
    goToOtherHome: (NavBackStackEntry, String) -> Unit
) {
    composable(MY_FOLLOWER) { nav ->
        FollowerListScreen(
            goToBack = { backPress() },
            goToSetting = { goToSetting(nav) },
            goToOtherHome = { id -> goToOtherHome(nav, id) }
        )
    }
}

internal fun NavGraphBuilder.myFollowerSettingNavGraph(
    backPress: () -> Unit,
    goToOtherHome: (NavBackStackEntry, String) -> Unit
) {
    composable(MY_FOLLOWER_SETTING) { nav ->
        FollowerListSettingScreen(
            goToBack = { backPress() },
            goToOtherHome = { id -> goToOtherHome(nav, id) })
    }
}

// 홈 > 검색 > 검색창 이동 
internal fun NavGraphBuilder.searchDirectNavGraph(
    backPress: () -> Unit,
    searchEvent: (SearchGoToEvent, NavBackStackEntry) -> Unit,
) {
    composable(SearchDestinations.GO_TO_SEARCH) { nav ->
        SearchScreen(
            closeEvent = backPress,
            searchEvent = {
                searchEvent.invoke(it, nav)
            }
        )
    }
}

internal fun NavGraphBuilder.writeNavGraph(
    action: (ActionWithActivity) -> Unit,
    backPress: () -> Unit,
    goToAddCategory: (NavBackStackEntry) -> Unit,
    goToHome: () -> Unit
) {
    composable(
        route = "${WriteDestinations.GO_TO_WRITE}/{${WriteDestinations.UPDATE_ID}}",
        arguments = listOf(
            navArgument(WriteDestinations.UPDATE_ID) {
                type = NavType.StringType
            }
        )) { entry ->
        val arguments = entry.arguments
        val bucketId = arguments?.getString(WriteDestinations.UPDATE_ID) ?: ""
        WriteScreen(
            id = bucketId,
            event = { event ->
                when (event) {
                    is WriteEvent.ActivityAction -> {
                        action(event.acton)
                    }

                    is WriteEvent.GoToBack -> {
                        backPress()
                    }

                    is WriteEvent.GoToAddCategory -> {
                        goToAddCategory(entry)
                    }
                }
            },
            addBucketSucceed = {
                goToHome()
            }
        )
    }
}

internal fun NavGraphBuilder.alarmNavGraph(
    backPress: () -> Unit,
    alarmClicked: (AlarmClickEvent, NavBackStackEntry) -> Unit
) {
    composable(AlarmDestinations.GO_TO_ALARM) { entry ->
        AlarmScreen(
            onBackPressed = backPress,
            alarmClicked = { event ->
                alarmClicked(event, entry)
            }
        )
    }
}

internal fun NavGraphBuilder.moreAlarmNavGraph(
    backPress: () -> Unit
) {
    composable(MoreDestinations.ALARM_SETTING) {
        AlarmSettingScreen {
            backPress()
        }
    }
}

internal fun NavGraphBuilder.moreProfileNavGraph(
    backPress: () -> Unit,
    action: (ActionWithActivity) -> Unit,
) {
    composable(MoreDestinations.PROFILE_SETTING) {
        ProfileSettingScreen(
            onBackPressed = { backPress() },
            addImageAction = {
                action(it)
            })
    }
}

internal fun NavGraphBuilder.moreAppInfoNavGraph(
    backPress: () -> Unit,
    onAppInfoItemClicked: (AppInfoItemType) -> Unit,
    withdrawEvent: () -> Unit
) {
    composable(MoreDestinations.MORE_APP_INFO) {
        AppInfoScreen(
            onBackClicked = { backPress() },
            onAppInfoItemClicked = onAppInfoItemClicked,
            withdrawEvent = withdrawEvent
        )
    }
}

internal fun NavGraphBuilder.openBucketDetailNavGraph(
    goToBucketDetailEvent: (OpenBucketDetailEvent2, NavBackStackEntry) -> Unit,
    backPress: () -> Unit
) {
    composable(
        "${MainDestinations.OPEN_BUCKET_DETAIL}/{${MainDestinations.BUCKET_ID_KEY}}/{${MainDestinations.WRITER_ID_KEY}}/{${MainDestinations.BUCKET_IS_MINE}}",
        arguments = listOf(navArgument(MainDestinations.BUCKET_ID_KEY) {
            type = NavType.StringType
        }, navArgument(MainDestinations.BUCKET_IS_MINE) {
            type = NavType.BoolType
        }, navArgument(MainDestinations.WRITER_ID_KEY) {
            type = NavType.StringType
        })
    ) { backStackEntry ->
        val arguments = requireNotNull(backStackEntry.arguments)
        val detailId = arguments.getString(MainDestinations.BUCKET_ID_KEY) ?: ""
        val writerId = arguments.getString(MainDestinations.WRITER_ID_KEY) ?: ""
        val isMine = arguments.getBoolean(MainDestinations.BUCKET_IS_MINE)
        OpenDetailBucketListScreen(
            detailId = detailId,
            writerId = writerId,
            isMine = isMine,
            goToEvent = {
                when (it) {
                    is OpenBucketDetailEvent2.Update -> {
                        val info = it.info.copy(isForUpdate = true)
                        goToBucketDetailEvent.invoke(
                            OpenBucketDetailEvent2.Update(info), backStackEntry
                        )
                    }

                    is OpenBucketDetailEvent2.GoToOtherProfile -> {
                        goToBucketDetailEvent.invoke(it, backStackEntry)
                    }
                }
            }, backPress = backPress
        )
    }
}

internal fun NavGraphBuilder.closeBucketDetailNavGraph(
    goToBucketDetailEvent: (CloseBucketDetailEvent, NavBackStackEntry) -> Unit,
    backPress: () -> Unit
) {
    composable(
        "${MainDestinations.CLOSE_BUCKET_DETAIL}/{${MainDestinations.BUCKET_ID_KEY}}",
        arguments = listOf(navArgument(MainDestinations.BUCKET_ID_KEY) {
            type = NavType.StringType
        })
    ) { backStackEntry ->
        val arguments = requireNotNull(backStackEntry.arguments)
        val detailId = arguments.getString(MainDestinations.BUCKET_ID_KEY) ?: ""
        com.zinc.waver.ui_detail.screen.CloseDetailScreen(detailId, goToUpdate = {
            val info = it.copy(isForUpdate = true)
            goToBucketDetailEvent.invoke(
                CloseBucketDetailEvent.Update(info), backStackEntry
            )
        }, backPress = backPress)
    }
}

internal fun NavGraphBuilder.goToOtherHomeNavGraph(
    otherHomeEvent: (OtherHomeEvent, NavBackStackEntry) -> Unit
) {
    composable(
        "${OtherDestinations.GO_TO_OTHER_HOME}/{${OtherDestinations.OTHER_USER_ID}}",
        arguments = listOf(navArgument(OtherDestinations.OTHER_USER_ID) {
            type = NavType.StringType
        })
    ) { backStackEntry ->
        val arguments = requireNotNull(backStackEntry.arguments)
        val otherId = arguments.getString(OtherDestinations.OTHER_USER_ID) ?: ""
        OtherHomeScreen(userId = otherId) {
            otherHomeEvent(it, backStackEntry)
        }
    }
}

internal fun NavGraphBuilder.myWaveManageNavGraph(
    backPress: () -> Unit
) {
    composable(MoreDestinations.MY_WAVE_MANAGE) {
        MyWaveManageScreen(onBackPressed = backPress)
    }
}

internal fun NavGraphBuilder.wavePlusGuideNavGraph(
    backPress: () -> Unit,
    inAppBillingShow: (WaverPlusType) -> Unit,
) {
    composable(MoreDestinations.WAVE_PLUS) {

        WaverPlusGuideScreen(onBackPressed = { backPress() }, inAppBillingShow = inAppBillingShow)
    }
}

object MainDestinations {
    const val HOME_ROUTE = "home"
    const val CLOSE_BUCKET_DETAIL = "close_bucket_detail"
    const val OPEN_BUCKET_DETAIL = "open_bucket_detail"
    const val BUCKET_ID_KEY = "bucketId"
    const val WRITER_ID_KEY = "writerId"
    const val BUCKET_IS_MINE = "isMine"
    const val MY_SEARCH = "my_search"
    const val MY_CATEGORY_EDIT = "my_category_edit"
    const val SELECT_TAB = "select_tab"
    const val MY_CATEGORY_BUCKET_LIST = "my_category_bucket_list"
    const val CATEGORY_INFO = "category_info"
    const val MY_STATUS_BUCKET_LIST = "my_status_bucket_list"
    const val STATUS_INFO = "status_info"

    object FOLLOWING {
        const val MY_FOLLOWING = "my_following"
        const val MY_FOLLOWING_SETTING = "my_following_setting"
    }

    object FOLLOWER {
        const val MY_FOLLOWER = "my_follower"
        const val MY_FOLLOWER_SETTING = "my_follower_setting"
    }
}

object SearchDestinations {
    const val GO_TO_SEARCH = "go_to_search"
}

object WriteDestinations {
    const val GO_TO_WRITE = "go_to_write"
    const val UPDATE_ID = "update_id"
}

object MoreDestinations {
    const val ALARM_SETTING = "alarm_setting"
    const val PROFILE_SETTING = "profile_setting"
    const val MORE_APP_INFO = "app_info"
    const val MY_WAVE_MANAGE = "my_wave_manage"
    const val WAVE_PLUS = "wave_plus"
}

object AlarmDestinations {
    const val GO_TO_ALARM = "go_to_alarm"
}

object OtherDestinations {
    const val GO_TO_OTHER_HOME = "go_to_other_home"
    const val OTHER_USER_ID = "other_user_id"
}