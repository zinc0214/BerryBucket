package com.zinc.waver.ui_detail.component.mention

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.zinc.waver.model.CommentMentionInfo
import com.zinc.waver.ui.design.theme.Gray10
import com.zinc.waver.ui.design.theme.Gray6
import com.zinc.waver.ui.design.theme.Gray9
import com.zinc.waver.ui.design.theme.Main4
import com.zinc.waver.ui.presentation.component.IconButton
import com.zinc.waver.ui.presentation.component.MyText
import com.zinc.waver.ui.util.dpToSp
import com.zinc.waver.ui_detail.R
import com.zinc.waver.ui_common.R as CommonR

@Composable
internal fun MentionTitleView(
    closeClicked: () -> Unit,
    finishClicked: () -> Unit,
    isFinishAvailable: Boolean,
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        IconButton(
            image = com.zinc.waver.ui_common.R.drawable.btn_40_close,
            contentDescription = stringResource(id = com.zinc.waver.ui_common.R.string.closeDesc),
            modifier = Modifier
                .padding(start = 14.dp, top = 6.dp, bottom = 6.dp)
                .size(40.dp),
            onClick = { closeClicked() }
        )

        Spacer(Modifier.weight(1F, fill = true))

        MyText(
            text = stringResource(id = com.zinc.waver.ui_common.R.string.succeedText),
            color = if (isFinishAvailable) Main4 else Gray6,
            fontSize = dpToSp(dp = 18.dp),
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 5.dp)
                .clickable {
                    finishClicked()
                }
                .padding(end = 18.dp)
        )
    }
}

@Composable
internal fun MentionListView(
    modifier: Modifier = Modifier,
    isFriendsList: Boolean,
    mentionList: List<CommentMentionInfo>,
    mentionSelected: (CommentMentionInfo) -> Unit
) {

    Column(modifier = Modifier.padding(28.dp)) {

        if (isFriendsList) {
            MyText(
                modifier = modifier
                    .padding(vertical = 28.dp),
                text = stringResource(id = R.string.mentionFriendText),
                color = Gray10,
                fontSize = dpToSp(
                    dp = 15.dp
                ),
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            mentionList.forEach { commentMentionInfo ->
                MentionItem(
                    modifier = Modifier.fillMaxWidth(),
                    item = commentMentionInfo,
                    itemClicked = {
                        mentionSelected(commentMentionInfo)
                    })
            }
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

@Composable
@Preview
private fun MentionTitlePreView() {
    MentionTitleView(
        closeClicked = {},
        finishClicked = {},
        isFinishAvailable = true
    )
}