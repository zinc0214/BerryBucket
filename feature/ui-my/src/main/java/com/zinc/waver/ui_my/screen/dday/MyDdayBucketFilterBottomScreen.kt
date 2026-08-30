package com.zinc.waver.ui_my.screen.dday

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.zinc.waver.ui.design.theme.Gray7
import com.zinc.waver.ui.presentation.component.BottomButtonView
import com.zinc.waver.ui.presentation.component.LabelWithSwitchView
import com.zinc.waver.ui.presentation.component.MyText
import com.zinc.waver.ui.util.dpToSp
import com.zinc.waver.ui_my.R
import com.zinc.waver.ui_my.viewModel.MyViewModel

@Composable
fun MyDdayBucketFilterBottomScreen(
    viewModel: MyViewModel,
    negativeEvent: () -> Unit,
    positiveEvent: () -> Unit
) {
    val context = LocalContext.current

    val showMinusPref by viewModel.isShownMinusDday.observeAsState()
    val showPlusPref by viewModel.isShowPlusDday.observeAsState()

    val minusBucketListSelectedState = remember {
        mutableStateOf(showMinusPref)
    }

    val plusBucketListSelectedState = remember {
        mutableStateOf(showPlusPref)
    }


    LaunchedEffect(key1 = showMinusPref, block = {
        showMinusPref?.let {
            minusBucketListSelectedState.value = it
        }
    })

    LaunchedEffect(key1 = showPlusPref, block = {
        showPlusPref?.let {
            plusBucketListSelectedState.value = it
        }
    })

    // 시트가 계속 컴포즈되어 있으므로 최신 콜백을 참조하도록 한다.
    val onSaved by rememberUpdatedState(positiveEvent)
    LaunchedEffect(Unit) {
        viewModel.ddayFilterSaved.collect { onSaved() }
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
        minusBucketListSelectedState.value?.let { minusSelected ->
            LabelWithSwitchView(modifier = Modifier.padding(bottom = 10.dp),
                textLabel = R.string.dDayPreBucketList,
                isChecked = minusSelected,
                checkedChanged = { checked ->
                    minusBucketListSelectedState.value = checked
                })
        }
        plusBucketListSelectedState.value?.let { plusSelected ->
            LabelWithSwitchView(textLabel = R.string.dDayGoneBucketList,
                isChecked = plusSelected,
                checkedChanged = { checked ->
                    plusBucketListSelectedState.value = checked
                })
        }
        Spacer(modifier = Modifier.height(40.dp))
        BottomButtonView(
            negativeEvent = {
                minusBucketListSelectedState.value = showMinusPref
                plusBucketListSelectedState.value = showPlusPref
                negativeEvent()
            },
            positiveEvent = {
                if (minusBucketListSelectedState.value == false && plusBucketListSelectedState.value == false) {
                    Toast.makeText(
                        context,
                        R.string.needToShowOne,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    viewModel.updateDdayBucketFilter(
                        isMinusShow = minusBucketListSelectedState.value,
                        isPlusShow = plusBucketListSelectedState.value
                    )
                }
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
        fontSize = dpToSp(13.dp)
    )
}