package com.zinc.waver.ui.presentation.login

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.zinc.domain.models.GoogleEmailInfo
import com.zinc.waver.ui.presentation.login.model.CreateProfileInfo
import com.zinc.waver.ui.presentation.model.ActionWithActivity

/**
 * 가입 플로우. 가입이 끝나면 [goToMain] 으로 홈에 넘긴다.
 *
 * 웰컴 팝업은 이 화면이 아니라 홈에서 띄운다. [goToMain] 도달 = 가입 성공이므로
 * 홈이 그 신호만으로 팝업 노출 여부를 판단할 수 있다.
 */
@Composable
fun JoinScreen(
    goToMain: () -> Unit,
    goToBack: () -> Unit,
    goToLogin: (GoogleEmailInfo) -> Unit,
    addImageAction: (ActionWithActivity.AddImage) -> Unit,
) {

    var emailLoginSucceed by remember {
        mutableStateOf(false)
    }

    var isFirstCreate by remember { mutableStateOf(false) }
    var createProfileInfo by remember { mutableStateOf(CreateProfileInfo()) }

    Log.e("ayhan", "createProfileInfo : $createProfileInfo")

    val joinTryEmail: MutableState<GoogleEmailInfo?> = remember { mutableStateOf(null) }

    // 가입 완료 이후에는 JoinCreateProfile2 로 절대 되돌아가지 않는다.
    // showMyBuryConnect 로만 가렸더니 연결 화면이 닫힐 때 JoinCreateProfile2 가 다시 컴포지션에
    // 들어왔고, joinSucceed(SingleLiveEvent) 의 .value 가 true 로 남아 있어 observeAsState 초기값이
    // true → LaunchedEffect 재발화 → 연결 화면 재노출이 무한 반복됐다.
    var joinSucceed by remember { mutableStateOf(false) }
    var showMyBuryConnect by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (emailLoginSucceed.not()) {
            JoinEmailScreen(goToNexPage = {
                joinTryEmail.value = it
                emailLoginSucceed = true
            }, goToBack = {
                goToBack()
            }, goToLogin = goToLogin)
        } else if (isFirstCreate.not()) {
            JoinCreateProfile1(
                createProfileInfo = createProfileInfo,
                goToNext = { info ->
                    isFirstCreate = true
                    createProfileInfo = info
                },
                addImageAction = addImageAction
            )
        } else if (isFirstCreate && !joinSucceed) {
            joinTryEmail.value?.let {
                JoinCreateProfile2(
                    emailInfo = it,
                    createProfileInfo = createProfileInfo,
                    goToMain = { isMyBuryUser ->
                        joinSucceed = true
                        // 마이버리 기존 회원이면 데이터 연결 화면을 먼저 보여준다.
                        if (isMyBuryUser) {
                            showMyBuryConnect = true
                        } else {
                            goToMain()
                        }
                    },
                    goToBack = {
                        isFirstCreate = false
                    }
                )
            }
        }

        if (showMyBuryConnect) {
            MyBuryConnectScreen(
                onFinished = {
                    // 이관 안내 팝업과 연결 화면을 먼저 걷어낸 뒤 홈으로 넘긴다.
                    showMyBuryConnect = false
                    goToMain()
                }
            )
        }
    }
}

