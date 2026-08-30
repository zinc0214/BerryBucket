package com.zinc.waver.ui_my

import androidx.compose.runtime.Composable
import com.zinc.waver.model.MySearchClickEvent
import com.zinc.waver.model.MyTabType
import com.zinc.waver.ui_my.screen.all.MyAllBucketFilterBottomScreen
import com.zinc.waver.ui_my.screen.dday.MyDdayBucketFilterBottomScreen
import com.zinc.waver.ui_my.viewModel.MyViewModel

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
    onFilterSaved: () -> Unit
) {
    when (tab) {
        is MyTabType.ALL -> {
            MyAllBucketFilterBottomScreen(
                viewModel = viewModel,
                negativeEvent = onFilterSaved,
                positiveEvent = onFilterSaved
            )
        }

        is MyTabType.DDAY -> {
            MyDdayBucketFilterBottomScreen(
                viewModel = viewModel,
                negativeEvent = onFilterSaved,
                positiveEvent = onFilterSaved
            )
        }

        is MyTabType.CHALLENGE -> TODO()
        else -> {
            // Do Nothing
        }
    }


}

sealed class BottomSheetScreenType {
    data class MyBucketSearchScreen(
        val selectTab: MyTabType, val viewModel: MyViewModel
    ) : BottomSheetScreenType()

    data class MyBucketFilterScreen(
        val needToShown: Boolean
    ) : BottomSheetScreenType()
}