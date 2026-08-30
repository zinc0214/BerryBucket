package com.zinc.waver.ui.presentation.login

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.zinc.waver.R
import com.zinc.waver.ui.presentation.component.dialog.ApiFailDialog

@Composable
fun LoginScreen(
    goToMainHome: () -> Unit,
    goToJoin: () -> Unit,
    goToFinish: () -> Unit
) {
    val viewModel: LoginViewModel = hiltViewModel()

    val canGoToMainAsState by viewModel.goToMain.observeAsState()
    val isLoginFailAsState by viewModel.loginFail.observeAsState()
    val goToJoinAsState by viewModel.needToStartJoin.observeAsState()
    val needToStartLoadTokenAsState by viewModel.needToStartLoadToken.observeAsState()

    val needToShowLoginFailDialog = remember { mutableStateOf(isLoginFailAsState) }
    val needToShowJoinDialog = remember { mutableStateOf(goToJoinAsState) }
    val isAnimFinished = remember { mutableStateOf(false) }

    // 컴포지션 도중에 호출하면 리컴포지션마다 부수효과가 새어나간다.
    LaunchedEffect(Unit) {
        if (!viewModel.isLoginChecked) {
            viewModel.checkHasLoginEmail()
        }
    }

    LaunchedEffect(key1 = canGoToMainAsState, key2 = isAnimFinished.value) {
        if (isAnimFinished.value) {
            canGoToMainAsState?.let {
                goToMainHome()
            }
        }
    }

    LaunchedEffect(key1 = isLoginFailAsState, key2 = isAnimFinished.value) {
        if (isAnimFinished.value) {
            needToShowLoginFailDialog.value = isLoginFailAsState
        }
    }

    LaunchedEffect(key1 = goToJoinAsState, key2 = isAnimFinished.value) {
        if (isAnimFinished.value) {
            needToShowJoinDialog.value = goToJoinAsState
        }
    }

    LaunchedEffect(key1 = needToStartLoadTokenAsState) {
        needToStartLoadTokenAsState?.let {
            viewModel.loadLoginToken(it)
        }
    }

    Scaffold { padding ->

        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            factory = { context ->
                SplashView(context = context, animFinished = {
                    isAnimFinished.value = true
                })
            }
        )
        if (needToShowLoginFailDialog.value == true) {
            ApiFailDialog(
                title = stringResource(id = R.string.loginFail),
                message = stringResource(id = R.string.loginRetry)
            ) {
                goToFinish()
                needToShowLoginFailDialog.value = false
            }
        }

        // 컴포지션 도중 화면을 전환하면 상태 변경이 컴포지션과 뒤엉킨다.
        LaunchedEffect(needToShowJoinDialog.value) {
            if (needToShowJoinDialog.value == true) {
                goToJoin()
            }
        }
    }
}
