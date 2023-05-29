package com.zinc.berrybucket.ui.presentation.detail.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import com.zinc.berrybucket.R
import com.zinc.berrybucket.ui.dialog.SingleTextFieldDialogEvent
import com.zinc.berrybucket.ui.dialog.SingleTextFieldDialogView
import com.zinc.berrybucket.ui.dialog.TextFieldArrangment
import com.zinc.berrybucket.ui.presentation.detail.model.GoalCountUpdateEvent

@Composable
fun GoalCountUpdateDialog(
    currentCount: String,
    event: (GoalCountUpdateEvent) -> Unit
) {

    var updateGoalCount by remember { mutableStateOf(currentCount) }

    SingleTextFieldDialogView(
        titleText = R.string.optionGoalCount,
        prevText = updateGoalCount,
        filedHintText = R.string.zeroGoalCount,
        saveNotAvailableToastText = R.string.countIsNotValidToast,
        textFieldArrangement = TextFieldArrangment.CENTER,
        keyboardType = KeyboardType.Number,
        enableCondition = { currentCount != updateGoalCount && updateGoalCount != "0" && updateGoalCount.isNotEmpty() },
        saveAvailableCondition = { updateGoalCount != "0" },
        event = {
            when (it) {
                SingleTextFieldDialogEvent.Close -> event.invoke(GoalCountUpdateEvent.Close)
                is SingleTextFieldDialogEvent.TextChangedField -> {
                    updateGoalCount = it.text
                }

                is SingleTextFieldDialogEvent.Update -> event.invoke(
                    GoalCountUpdateEvent.CountUpdate(
                        updateGoalCount
                    )
                )
            }
        }
    )
}