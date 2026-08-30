package com.zinc.waver.ui_my.screen.all

import android.util.Log
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zinc.waver.ui.design.theme.Gray2
import com.zinc.waver.ui.design.theme.Gray3
import com.zinc.waver.ui.design.theme.Gray7
import com.zinc.waver.ui.presentation.component.BottomButtonView
import com.zinc.waver.ui.presentation.component.LabelWithSwitchView
import com.zinc.waver.ui.presentation.component.MyText
import com.zinc.waver.ui.util.dpToSp
import com.zinc.waver.ui_my.R
import com.zinc.waver.ui_my.view.LabelWithRadioView
import com.zinc.waver.ui_my.viewModel.MyViewModel

@Composable
fun MyAllBucketFilterBottomScreen(
    viewModel: MyViewModel,
    negativeEvent: () -> Unit,
    positiveEvent: () -> Unit
) {
    val context = LocalContext.current

    val showProgressPref by viewModel.showProgress.observeAsState()
    val showSucceedPref by viewModel.showSucceed.observeAsState()
    val orderTypePref by viewModel.orderType.observeAsState()
    val showDdayPref by viewModel.showDdayView.observeAsState()

    val proceedingBucketListSelectedState = remember {
        mutableStateOf(showProgressPref)
    }

    val succeedBucketListSelectedState = remember {
        mutableStateOf(showSucceedPref)
    }

    val sortSelectedState = remember {
        mutableStateOf(orderTypePref)
    }

    val ddayShowSelectedState = remember {
        mutableStateOf(showDdayPref)
    }

    LaunchedEffect(key1 = showProgressPref, block = {
        showProgressPref?.let {
            proceedingBucketListSelectedState.value = it
        }
    })

    LaunchedEffect(key1 = showSucceedPref, block = {
        showSucceedPref?.let {
            Log.e("ayhan", "showSucceedPref: $showSucceedPref")
            succeedBucketListSelectedState.value = it
        }
    })

    LaunchedEffect(key1 = orderTypePref, block = {
        orderTypePref?.let {
            sortSelectedState.value = it
        }
    })

    LaunchedEffect(key1 = showDdayPref, block = {
        showDdayPref?.let {
            ddayShowSelectedState.value = it
        }
    })

    // 시트가 계속 컴포즈되어 있으므로 최신 콜백을 참조하도록 한다.
    val onSaved by rememberUpdatedState(positiveEvent)
    LaunchedEffect(Unit) {
        viewModel.allFilterSaved.collect { onSaved() }
    }

    Column(
        modifier = Modifier.background(
            shape = RoundedCornerShape(
                topStart = 13.dp, topEnd = 13.dp
            ), color = Gray2
        )
    ) {
        FilterTitleLabel(
            labelText = R.string.showBucketList,
            modifier = Modifier.padding(top = 32.dp, bottom = 15.dp)
        )
        proceedingBucketListSelectedState.value?.let { isProceed ->
            LabelWithSwitchView(
                modifier = Modifier.padding(bottom = 10.dp),
                textLabel = R.string.proceedingBucketList,
                isChecked = isProceed,
                checkedChanged = {
                    proceedingBucketListSelectedState.value = it
                })
        }

        succeedBucketListSelectedState.value?.let { isSucceed ->
            LabelWithSwitchView(
                textLabel = R.string.succeedBucketList,
                isChecked = isSucceed,
                checkedChanged = {
                    succeedBucketListSelectedState.value = it
                })
        }

        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
                .padding(top = 17.dp, bottom = 27.dp)
                .height(1.dp)
                .background(color = Gray3)

        )

        FilterTitleLabel(
            modifier = Modifier.padding(bottom = 20.dp),
            labelText = R.string.sortStandard
        )

        sortSelectedState.value?.let { sortType ->
            LabelWithRadioView(
                modifier = Modifier,
                itemLabels = listOf(0 to R.string.sortByUpdate, 1 to R.string.sortByCreate),
                selectedIndex = sortType,
                changedSelectedItem = {
                    sortSelectedState.value = it
                })
        }

        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
                .padding(top = 12.dp, bottom = 27.dp)
                .height(1.dp)
                .background(color = Gray3)

        )
        FilterTitleLabel(
            labelText = R.string.showContent, modifier = Modifier.padding(bottom = 15.dp)
        )

        ddayShowSelectedState.value?.let { showDday ->
            LabelWithSwitchView(
                modifier = Modifier.padding(bottom = 30.dp),
                textLabel = R.string.showDday,
                isChecked = showDday,
                checkedChanged = {
                    ddayShowSelectedState.value = it
                })
        }

        BottomButtonView(
            positiveEvent = {
                if (proceedingBucketListSelectedState.value == false && succeedBucketListSelectedState.value == false) {
                    Toast.makeText(
                        context,
                        R.string.needToShowOne,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    viewModel.updateAllBucketFilter(
                        isProgress = proceedingBucketListSelectedState.value,
                        isSucceed = succeedBucketListSelectedState.value,
                        orderType = sortSelectedState.value,
                        showDday = ddayShowSelectedState.value
                    )
                }
            },
            negativeEvent = {
                proceedingBucketListSelectedState.value = showProgressPref
                succeedBucketListSelectedState.value = showSucceedPref
                sortSelectedState.value = orderTypePref
                ddayShowSelectedState.value = showDdayPref
                negativeEvent()
            })
    }

}

@Composable
private fun FilterTitleLabel(
    @StringRes labelText: Int, modifier: Modifier = Modifier
) {
    MyText(
        modifier = modifier.padding(horizontal = 28.dp),
        text = stringResource(id = labelText),
        color = Gray7,
        fontSize = dpToSp(13.dp),
    )
}