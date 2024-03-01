package com.zinc.berrybucket.ui.presentation.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zinc.berrybucket.R
import com.zinc.berrybucket.ui.design.theme.Gray10
import com.zinc.berrybucket.ui.design.theme.Gray6
import com.zinc.berrybucket.ui.presentation.component.MyText
import com.zinc.berrybucket.ui.presentation.component.MyTextField
import com.zinc.berrybucket.ui.presentation.component.dialog.ApiFailDialog
import com.zinc.berrybucket.ui.presentation.login.component.ProfileCreateTitle
import com.zinc.berrybucket.ui.presentation.login.model.LoginPrevData
import com.zinc.berrybucket.ui.util.dpToSp


// 회원가입1 > 이메일 입력
@Composable
fun JoinEmailScreen(
    goToNexPage: (LoginPrevData) -> Unit,
    goToBack: () -> Unit
) {
    val viewModel: JoinEmailViewModel = hiltViewModel()

    // 이미 이메일이 있는지?
    val checkAlreadyUsedEmailAsState by viewModel.isAlreadyUsedEmail.observeAsState()
    val isAlreadyUsedEmail = remember { mutableStateOf(false) }

    // 사용가능한 이메일인 경우
    val goToMakeNickNameAsState by viewModel.goToMakeNickName.observeAsState()

    // api 실패
    val failApiAsState by viewModel.failEmailCheck.observeAsState()
    val isFailApi = remember { mutableStateOf(false) }

    val prevLoginEmail = remember { mutableStateOf("") }

    LaunchedEffect(key1 = checkAlreadyUsedEmailAsState) {
        isAlreadyUsedEmail.value = checkAlreadyUsedEmailAsState ?: false
    }

    LaunchedEffect(key1 = goToMakeNickNameAsState) {
        goToMakeNickNameAsState?.let { data ->
            goToNexPage(
                LoginPrevData(
                    email = prevLoginEmail.value,
                    accessToken = data.first,
                    refreshToken = data.second
                )
            )
        }
    }

    LaunchedEffect(key1 = failApiAsState) {
        isFailApi.value = failApiAsState == true
    }

    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ProfileCreateTitle(
            saveButtonEnable = prevLoginEmail.value.isNotEmpty(),
            backClicked = {
                goToBack()
            },
            saveClicked = {
                viewModel.checkEmailValid(prevLoginEmail.value)
            }
        )

        Spacer(modifier = Modifier.padding(top = 44.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
                .padding(horizontal = 28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MyTextField(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(),
                    value = prevLoginEmail.value,
                    textStyle = TextStyle(
                        color = Gray10, fontSize = dpToSp(24.dp), fontWeight = FontWeight.Medium
                    ),
                    onValueChange = {
                        prevLoginEmail.value = it
                    },
                    decorationBox = { innerTextField ->
                        Row {
                            if (prevLoginEmail.value.isEmpty()) {
                                MyText(
                                    text = "이메일을 입력하세요",
                                    color = Gray6,
                                    fontSize = dpToSp(24.dp)
                                )
                            }
                            innerTextField()  //<-- Add this
                        }
                    },
                )
            }
        }
    }
    if (isAlreadyUsedEmail.value) {
        ApiFailDialog(
            title = stringResource(id = R.string.alreadyUsedEmailTitle),
            message = stringResource(id = R.string.alreadyUsedEmailDesc)
        ) {
            viewModel.goToLogin(prevLoginEmail.value)
        }
    }
    if (isFailApi.value) {
        ApiFailDialog(
            title = stringResource(id = R.string.joinFailTitle),
            message = stringResource(id = R.string.loginRetry)
        ) {
            viewModel.checkEmailValid(prevLoginEmail.value)
        }
    }
}