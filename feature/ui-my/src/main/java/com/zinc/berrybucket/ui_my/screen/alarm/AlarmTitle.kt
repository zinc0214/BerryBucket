package com.zinc.berrybucket.ui_my.screen.alarm

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.zinc.berrybucket.ui.presentation.component.TitleIconType
import com.zinc.berrybucket.ui.presentation.component.TitleView
import com.zinc.berrybucket.ui_my.R


@Composable
internal fun AlarmTitle(backClicked: () -> Unit) {
    TitleView(
        title = stringResource(id = R.string.alarmTitle),
        leftIconType = TitleIconType.BACK,
        isDividerVisible = true,
        onLeftIconClicked = {
            backClicked()
        }
    )
}