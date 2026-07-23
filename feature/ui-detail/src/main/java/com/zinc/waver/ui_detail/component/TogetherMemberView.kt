package com.zinc.waver.ui_detail.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.zinc.waver.model.TogetherInfo
import com.zinc.waver.model.TogetherMember
import com.zinc.waver.ui.design.theme.Gray1
import com.zinc.waver.ui.design.theme.Gray10
import com.zinc.waver.ui.design.theme.Gray2
import com.zinc.waver.ui.design.theme.Gray3
import com.zinc.waver.ui.design.theme.Gray4
import com.zinc.waver.ui.design.theme.Gray9
import com.zinc.waver.ui.design.theme.Main1
import com.zinc.waver.ui.design.theme.Main2
import com.zinc.waver.ui.design.theme.Main3
import com.zinc.waver.ui.design.theme.Main4
import com.zinc.waver.ui.presentation.component.HorizontalProgressBar
import com.zinc.waver.ui.presentation.component.MyText
import com.zinc.waver.ui.util.dpToSp
import com.zinc.waver.ui_common.R

@Composable
fun TogetherMemberView(
    modifier: Modifier = Modifier,
    togetherInfo: TogetherInfo
) {

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 40.dp, end = 10.dp)
    ) {

        Divider(
            color = Gray3,
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
        )

        TogetherCount(count = togetherInfo.count)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            togetherInfo.togetherMembers.forEach {
                TogetherMemberItemView(togetherMember = it)
            }
        }
    }
}


@Composable
private fun TogetherCount(
    count: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 27.dp, start = 22.dp, end = 22.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ico_36_together),
            contentDescription = null,
            modifier = Modifier.size(32.dp)
        )

        MyText(
            modifier = Modifier.padding(start = 4.dp),
            text = count,
            fontSize = dpToSp(dp = 15.dp),
            color = Gray10
        )
    }
}

@Composable
private fun TogetherMemberItemView(
    togetherMember: TogetherMember
) {

    Card(
        modifier = Modifier.background(
            color = Main1,
            shape = RoundedCornerShape(4.dp)
        ),
        border = BorderStroke(1.dp, if (togetherMember.isSucceed()) Main3 else Gray2),
        elevation = 0.5.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 22.dp, bottom = 17.dp, start = 12.dp, end = 12.dp)
        ) {
            Box(
                modifier = Modifier.size(50.dp),
                content = {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = togetherMember.profileImage,
                            error = painterResource(id = R.drawable.profile_placeholder)
                        ),
                        contentDescription = stringResource(
                            id = R.string.profileImgDesc
                        ),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(44.dp)
                            .aspectRatio(1f)
                            .clip(shape = RoundedCornerShape(16.dp))
                            .align(Alignment.TopStart)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.badge_placeholder),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(22.dp)
                            .align(Alignment.BottomEnd),
                        alignment = Alignment.BottomEnd
                    )
                })

            Column(modifier = Modifier.padding(start = 10.dp)) {
                MyText(text = togetherMember.nickName, color = Gray9, fontSize = dpToSp(dp = 14.dp))

                Row(modifier = Modifier.padding(top = 5.dp, bottom = 7.dp)) {
                    HorizontalProgressBar(
                        Modifier
                            .align(Alignment.CenterVertically)
                            .height(8.dp),
                        togetherMember.userCount,
                        togetherMember.goalCount,
                        Main2
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    MyText(
                        text = togetherMember.userCount.toString(),
                        color = Main2,
                        fontSize = dpToSp(13.dp),
                    )
                    MyText(
                        text = "/${togetherMember.goalCount}",
                        color = Gray4,
                        fontSize = dpToSp(13.dp),
                    )
                }
            }
        }

        if (togetherMember.isSucceed()) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopEnd
            ) {
                MyText(
                    text = stringResource(id = R.string.succeedText),
                    color = Gray1,
                    fontSize = dpToSp(dp = 12.dp),
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(4.dp))
                        .background(color = Main4)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                        .align(Alignment.TopEnd)
                )
            }
        }
    }
}


@Preview
@Composable
private fun TogetherMemberPreview() {
    TogetherMemberView(
        modifier = Modifier, togetherInfo = TogetherInfo(
            count = "4",
            togetherMembers = listOf(
                TogetherMember(
                    memberId = "1",
                    profileImage = "1",
                    nickName = "아연이 내꺼지 너무 이쁘지",
                    isMine = false,
                    goalCount = 10,
                    userCount = 0
                ),
                TogetherMember(
                    memberId = "1",
                    profileImage = "1",
                    nickName = "누가 죄인인가!",
                    isMine = false,
                    goalCount = 11,
                    userCount = 11
                )
            )
        )
    )
}