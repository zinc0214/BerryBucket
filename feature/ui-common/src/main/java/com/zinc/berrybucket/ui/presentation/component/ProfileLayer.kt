package com.zinc.berrybucket.ui.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zinc.berrybucket.ui.design.theme.Gray10
import com.zinc.berrybucket.ui.design.theme.Gray7
import com.zinc.berrybucket.ui.design.theme.Main3
import com.zinc.berrybucket.ui.util.dpToSp
import com.zinc.common.models.TopProfile

@Composable
fun ProfileLayer(profileInfo: TopProfile) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            ProfileCircularProgressBar(
                percentage = profileInfo.percent,
                profileImageUrl = profileInfo.imgUrl ?: ""
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        MyText(
            modifier = Modifier
                .padding(start = 26.dp, end = 26.dp)
                .align(Alignment.CenterHorizontally),
            text = profileInfo.badgeTitle ?: "",
            fontSize = dpToSp(15.dp),
            color = Main3,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        MyText(
            modifier = Modifier
                .padding(start = 26.dp, end = 26.dp)
                .align(Alignment.CenterHorizontally),
            text = profileInfo.name,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Normal,
            fontSize = dpToSp(20.dp),
            color = Gray10,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        MyText(
            modifier = Modifier
                .padding(start = 26.dp, end = 26.dp)
                .align(Alignment.CenterHorizontally),
            text = profileInfo.bio ?: "",
            fontSize = dpToSp(14.dp),
            color = Gray7,
            textAlign = TextAlign.Center
        )
    }
}