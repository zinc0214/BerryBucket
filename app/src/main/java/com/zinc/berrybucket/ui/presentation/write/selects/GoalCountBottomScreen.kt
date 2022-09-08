package com.zinc.berrybucket.ui.presentation.write.selects

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zinc.berrybucket.R
import com.zinc.berrybucket.model.BottomButtonClickEvent
import com.zinc.berrybucket.ui.design.theme.*
import com.zinc.berrybucket.ui.presentation.common.BottomButtonView
import com.zinc.berrybucket.ui.util.dpToSp

@Composable
fun GoalCountBottomScreen(
    originCount: String = "0",
    canceled: () -> Unit,
    confirmed: (String) -> Unit
) {
    var editedGoalCount by remember { mutableStateOf(TextFieldValue(originCount)) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp, vertical = 28.dp),
            text = stringResource(id = R.string.optionGoalCount),
            fontSize = dpToSp(dp = 15.dp),
            color = Gray10,
            textAlign = TextAlign.Center
        )

        BasicTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 36.dp, start = 68.dp, end = 68.dp)
                .heightIn(min = 56.dp)
                .background(
                    color = Gray1,
                    shape = RoundedCornerShape(4.dp)
                )
                .border(
                    width = 1.dp,
                    shape = RoundedCornerShape(4.dp),
                    color = if (originCount == editedGoalCount.text || editedGoalCount.text == "0") Gray4 else Main3
                ),
            value = editedGoalCount,
            textStyle = TextStyle(textAlign = TextAlign.Center, fontWeight = FontWeight.Bold),
            onValueChange = {
                editedGoalCount = it
            },
            maxLines = 1,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            decorationBox = { innerTextField ->
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    if (editedGoalCount.text.isEmpty()) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = "0",
                            color = if (originCount == editedGoalCount.text || editedGoalCount.text == "0") Gray7 else Gray10,
                            fontSize = dpToSp(22.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                    innerTextField()  //<-- Add this
                }
            }
        )

        BottomButtonView(
            rightText = R.string.confirm,
            clickEvent = {
                when (it) {
                    BottomButtonClickEvent.LeftButtonClicked -> canceled()
                    BottomButtonClickEvent.RightButtonClicked -> confirmed(editedGoalCount.text)
                }
            })
    }
}

@Preview
@Composable
private fun GoalCountBottomScreenPreview() {
    GoalCountBottomScreen(
        originCount = "100040", canceled = {}, confirmed = {}
    )
}