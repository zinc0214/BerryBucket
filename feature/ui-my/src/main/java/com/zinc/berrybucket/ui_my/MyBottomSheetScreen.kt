package com.zinc.berrybucket.ui_my

import androidx.compose.runtime.Composable
import com.zinc.berrybucket.model.MySearchClickEvent
import com.zinc.berrybucket.model.MyTabType
import com.zinc.berrybucket.ui_my.screen.all.MyAllBucketFilterBottomScreen
import com.zinc.berrybucket.ui_my.screen.dday.MyDdayBucketFilterBottomScreen
import com.zinc.berrybucket.ui_my.viewModel.MyViewModel

@Composable
fun SearchBottomView(
    tab: MyTabType,
    mySearchClickEvent: (MySearchClickEvent) -> Unit,
) {
    MySearchBottomScreen(currentTabType = tab,
        clickEvent = {
            mySearchClickEvent(it)
        })
}

@Composable
fun FilterBottomView(
    tab: MyTabType,
    viewModel: MyViewModel,
    isNeedToUpdated: (Boolean) -> Unit
) {
    when (tab) {
        is MyTabType.ALL -> {
            MyAllBucketFilterBottomScreen(
                viewModel = viewModel,
                negativeEvent = {
                    isNeedToUpdated.invoke(false)
                },
                positiveEvent = {
                    isNeedToUpdated.invoke(true)
                })
        }

        is MyTabType.DDAY -> {
            MyDdayBucketFilterBottomScreen(
                viewModel = viewModel,
                negativeEvent = {
                    isNeedToUpdated.invoke(false)
                },
                positiveEvent = {
                    isNeedToUpdated.invoke(true)
                })
        }

        is MyTabType.CHALLENGE -> TODO()
        else -> {
            // Do Nothing
        }
    }


}

sealed class BottomSheetScreenType {
    data class SearchScreen(
        val selectTab: MyTabType, val viewModel: MyViewModel
    ) : BottomSheetScreenType()

    data class FilterScreen(
        val needToShown: Boolean
    ) : BottomSheetScreenType()
}