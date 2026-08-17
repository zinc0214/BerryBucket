package com.zinc.waver.ui_detail.component.mention

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.zinc.waver.model.CommentMentionInfo
import com.zinc.waver.ui.design.theme.Gray1
import com.zinc.waver.ui.design.theme.Gray9
import com.zinc.waver.ui.presentation.component.MyText
import com.zinc.waver.ui.util.dpToSp
import com.zinc.waver.ui_detail.R
import com.zinc.waver.ui_common.R as CommonR

@Composable
fun MentionSearchListPopup(
    searchedList: List<CommentMentionInfo>,
    mentionSelected: (CommentMentionInfo) -> Unit,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.BottomCenter)
            .fillMaxWidth()
            .padding(top = 3.dp, bottom = 60.dp, start = 16.dp, end = 16.dp),
        shape = RoundedCornerShape(10.dp),
        backgroundColor = Gray1,
        elevation = 3.dp
    ) {
        LazyColumn(
            modifier = modifier
                .fillMaxWidth()
                .background(color = Gray1)
                .heightIn(max = 200.dp)
                .padding(top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(items = searchedList, itemContent = {
                MentionItem(
                    modifier = Modifier.fillMaxWidth(),
                    item = it,
                    itemClicked = { info ->
                        mentionSelected(info)
                    })
            })
        }
    }

}

@Composable
private fun MentionItem(
    modifier: Modifier = Modifier,
    item: CommentMentionInfo,
    itemClicked: (CommentMentionInfo) -> Unit
) {
    Row(
        modifier = modifier
            .background(color = Gray1)
            .padding(horizontal = 20.dp)
            .clickable {
                itemClicked(item)
            }, verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(
                model = item.profileImage,
                placeholder = painterResource(CommonR.drawable.profile_icon_blank),
                error = painterResource(CommonR.drawable.profile_icon_blank),
                // 프로필 이미지를 등록하지 않은 유저(model == null)
                fallback = painterResource(CommonR.drawable.profile_icon_blank)
            ),
            contentDescription = stringResource(
                id = R.string.profileImage
            ),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(32.dp, 32.dp)
                .aspectRatio(1f)
                .clip(shape = RoundedCornerShape(12.dp))
        )

        MyText(
            modifier = Modifier.padding(start = 12.dp),
            text = item.nickName,
            color = Gray9,
            fontSize = dpToSp(13.dp),
            textAlign = TextAlign.Center
        )
    }
}