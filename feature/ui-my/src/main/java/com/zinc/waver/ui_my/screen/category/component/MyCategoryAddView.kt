package com.zinc.waver.ui_my.screen.category.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zinc.waver.ui.design.theme.Gray1
import com.zinc.waver.ui.design.theme.Main2
import com.zinc.waver.ui.design.theme.Main3
import com.zinc.waver.ui.presentation.component.MyText
import com.zinc.waver.ui.presentation.component.dashedBorder
import com.zinc.waver.ui.util.dpToSp
import com.zinc.waver.ui_common.R as CommonR

@Composable
internal fun MyCategoryAddView(
    addCategory: () -> Unit
) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .dashedBorder(
                width = 1.dp,
                color = Main2,
                shape = MaterialTheme.shapes.medium,
                on = 4.dp,
                off = 4.dp
            )
            .background(Gray1)
            .clip(MaterialTheme.shapes.medium)
            .clickable {
                addCategory()
            }, contentAlignment = Alignment.Center
    ) {
        MyText(
            textAlign = TextAlign.Center,
            text = stringResource(id = CommonR.string.categoryAdd),
            fontSize = dpToSp(14.dp),
            color = Main3,
            modifier = Modifier.padding(vertical = 22.dp, horizontal = 20.dp)
        )
    }
}