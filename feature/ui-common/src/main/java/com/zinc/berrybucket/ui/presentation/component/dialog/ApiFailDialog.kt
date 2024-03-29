package com.zinc.berrybucket.ui.presentation.component.dialog

import androidx.compose.runtime.Composable
import com.zinc.berrybucket.model.DialogButtonInfo
import com.zinc.berrybucket.ui.design.theme.Main4
import com.zinc.berrybucket.ui_common.R

@Composable
fun ApiFailDialog(title: String? = null, message: String? = null, dismissEvent: () -> Unit) {

    CommonDialogView(
        title = title, message = message, dismissAvailable = false,
        positive = DialogButtonInfo(
            text = R.string.confirm,
            color = Main4
        ),
        positiveEvent = {
            dismissEvent()
        }
    )
}