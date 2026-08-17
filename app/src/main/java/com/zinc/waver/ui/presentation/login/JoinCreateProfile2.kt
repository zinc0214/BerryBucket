package com.zinc.waver.ui.presentation.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.zinc.domain.models.GoogleEmailInfo
import com.zinc.waver.R
import com.zinc.waver.model.DialogButtonInfo
import com.zinc.waver.ui.design.theme.Error2
import com.zinc.waver.ui.design.theme.Gray1
import com.zinc.waver.ui.design.theme.Gray10
import com.zinc.waver.ui.design.theme.Gray3
import com.zinc.waver.ui.design.theme.Gray4
import com.zinc.waver.ui.design.theme.Gray5
import com.zinc.waver.ui.design.theme.Gray6
import com.zinc.waver.ui.design.theme.Gray8
import com.zinc.waver.ui.design.theme.Main4
import com.zinc.waver.ui.presentation.component.MyText
import com.zinc.waver.ui.presentation.component.MyTextField
import com.zinc.waver.ui.presentation.component.TitleIconType
import com.zinc.waver.ui.presentation.component.TitleView
import com.zinc.waver.ui.presentation.component.dialog.CommonDialogView
import com.zinc.waver.ui.presentation.login.model.CreateProfileInfo
import com.zinc.waver.ui.util.dpToSp
import com.zinc.waver.ui_common.R as CommonR

@Composable
fun JoinCreateProfile2(
    emailInfo: GoogleEmailInfo,
    createProfileInfo: CreateProfileInfo,
    goToMain: (isMyBuryUser: Boolean) -> Unit,
    goToBack: () -> Unit
) {
    val createUserViewModel: JoinNickNameViewModel = hiltViewModel()

    val failJoinAsState by createUserViewModel.failJoin.observeAsState()
    val joinSucceedAsState by createUserViewModel.joinSucceed.observeAsState()
    val isAlreadyUsedNickNameAsState by createUserViewModel.isAlreadyUsedNickName.observeAsState()

    var showErrorPopup by remember { mutableStateOf(false) }
    LaunchedEffect(joinSucceedAsState) {
        joinSucceedAsState?.let { isMyBuryUser -> goToMain(isMyBuryUser) }
    }

    LaunchedEffect(isAlreadyUsedNickNameAsState) {
        if (isAlreadyUsedNickNameAsState == true) {
            showErrorPopup = true
        }
    }

    JoinCreateProfile2(
        createProfileInfo = createProfileInfo,
        isJoinFailed = failJoinAsState ?: false,
        goToBack = goToBack,
        goToJoin = { bio ->
            createUserViewModel.join(
                emailInfo = emailInfo,
                nickName = createProfileInfo.nickName,
                bio = bio,
                image = createProfileInfo.imgFile
            )
        }
    )

    if (showErrorPopup) {
        CommonDialogView(
            message = stringResource(R.string.joinFailDesc),
            dismissAvailable = false,
            rightButtonInfo = DialogButtonInfo(
                text = CommonR.string.confirm,
                color = Main4
            ),
            rightButtonEvent = {
                goToBack()
            })
    }
}

@Composable
private fun JoinCreateProfile2(
    createProfileInfo: CreateProfileInfo,
    isJoinFailed: Boolean,
    goToJoin: (String) -> Unit,
    goToBack: () -> Unit
) {

    var bioText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TitleView(
            title = stringResource(R.string.goToJoin2),
            isDividerVisible = true,
            leftIconType = TitleIconType.BACK,
            onLeftIconClicked = {
                goToBack()
            }
        )

        Spacer(modifier = Modifier.padding(top = 32.dp))

        Image(
            painter = rememberAsyncImagePainter(
                model = createProfileInfo.imgPath,
                placeholder = painterResource(id = CommonR.drawable.profile_placeholder),
                error = painterResource(id = CommonR.drawable.profile_placeholder)
            ),
            contentDescription = stringResource(
                id = CommonR.string.moreProfileImageDesc
            ),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp, 48.dp)
                .aspectRatio(1f)
                .clip(shape = RoundedCornerShape(18.dp))
        )

        Spacer(modifier = Modifier.padding(top = 32.dp))

        NicknameText(nickName = createProfileInfo.nickName)

        Spacer(modifier = Modifier.padding(top = 20.dp))

        ProfileBioEditView(
            nickName = createProfileInfo.nickName,
            prevBioText = bioText,
            isJoinFailed = isJoinFailed,
            bioTextChanged = {
                bioText = it
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        MyText(
            text = stringResource(CommonR.string.next),
            fontSize = dpToSp(16.dp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            color = Gray1,
            modifier = Modifier
                .fillMaxWidth()
                .background(color = if (bioText.isNotBlank()) Main4 else Gray5)
                .clickable(bioText.isNotBlank()) {
                    goToJoin(bioText)
                }
                .padding(vertical = 18.dp)
        )
    }
}

@Composable
private fun NicknameText(nickName: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp)
    ) {
        MyText(
            text = stringResource(CommonR.string.profileSettingNickNameTitle),
            fontSize = dpToSp(14.dp),
            color = Gray6
        )
        MyText(
            text = nickName,
            fontSize = dpToSp(15.dp),
            color = Gray8,
            modifier = Modifier.padding(top = 6.dp)
        )
        Divider(modifier = Modifier.padding(top = 6.dp), color = Gray3)
    }
}

@Composable
private fun ProfileBioEditView(
    nickName: String,
    prevBioText: String,
    isJoinFailed: Boolean,
    bioTextChanged: (String) -> Unit
) {

    var currentText by remember { mutableStateOf(prevBioText) }
    var currentTextSize by remember { mutableIntStateOf(currentText.length) }
    val hintText = nickName + stringResource(CommonR.string.enterBioDesc)
    var isFocused by remember { mutableStateOf(false) }

    val maxLength = 30

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            MyText(
                text = stringResource(id = CommonR.string.profileSettingBioTitle),
                color = Gray6,
                fontSize = dpToSp(dp = 14.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            MyText(
                text = "($currentTextSize/30)",
                color = Gray5,
                fontSize = dpToSp(dp = 12.dp)
            )
        }

        MyTextField(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused },
            value = currentText,
            singleLine = false,
            textStyle = TextStyle(
                fontSize = dpToSp(dp = 20.dp),
                color = Gray10
            ),
            onValueChange = { changeText ->
                currentText = if (changeText.length > 30) {
                    val lastIndex = changeText.lastIndex
                    changeText.removeRange(maxLength - 1, lastIndex)
                } else {
                    changeText
                }
                currentTextSize = currentText.length
                bioTextChanged(changeText)
            },
            decorationBox = { innerTextField ->
                Row {
                    if (currentText.isEmpty() && !isFocused) {
                        MyText(
                            text = hintText,
                            color = Gray4,
                            fontSize = dpToSp(20.dp)
                        )
                    }
                    innerTextField()  //<-- Add this
                }
            })

        Divider(
            modifier = Modifier.padding(top = 15.5.dp),
            color = Gray5,
            thickness = 1.dp
        )

        if (isJoinFailed) {
            MyText(
                modifier = Modifier.padding(top = 7.5.dp),
                text = stringResource(id = R.string.joinFailDesc),
                color = Error2,
                fontSize = dpToSp(dp = 12.dp)
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun JoinNickNameScreenPreview() {
    JoinCreateProfile2(
        createProfileInfo = CreateProfileInfo(nickName = "닉네임은 웨이버"),
        isJoinFailed = false,
        goToJoin = {},
        goToBack = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun NicknameTextPreview() {
    NicknameText("닉네임이지요")
}

@Preview(showBackground = true)
@Composable
private fun ProfileBioEditPreview() {
    ProfileBioEditView(
        nickName = "닉네임",
        prevBioText = "d",
        isJoinFailed = true,
        bioTextChanged = {},
    )
}