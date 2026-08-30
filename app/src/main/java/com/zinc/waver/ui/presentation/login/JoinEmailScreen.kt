package com.zinc.waver.ui.presentation.login

import BuildConfig.GoogleWebClientId
import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.zinc.domain.models.GoogleEmailInfo
import com.zinc.waver.R
import com.zinc.waver.model.DialogButtonInfo
import com.zinc.waver.ui.design.theme.Gray1
import com.zinc.waver.ui.design.theme.Gray10
import com.zinc.waver.ui.design.theme.Gray3
import com.zinc.waver.ui.design.theme.Gray7
import com.zinc.waver.ui.design.theme.Main4
import com.zinc.waver.ui.presentation.component.MyText
import com.zinc.waver.ui.presentation.component.dialog.ApiFailDialog
import com.zinc.waver.ui.presentation.component.dialog.CommonDialogView
import org.json.JSONObject
import com.zinc.waver.ui_common.R as CommonR


// 회원가입1 > 이메일 입력
@Composable
fun JoinEmailScreen(
    goToNexPage: (GoogleEmailInfo) -> Unit,
    goToBack: () -> Unit,
    goToLogin: (GoogleEmailInfo) -> Unit,
) {
    val viewModel: JoinEmailViewModel = hiltViewModel()

    // 이미 이메일이 있는지?
    val checkAlreadyUsedEmailAsState by viewModel.isAlreadyUsedEmail.observeAsState()
    val isAlreadyUsedEmail = remember { mutableStateOf(false) }

    // 이미 삭제된 유저인 경우
    val isDeletedUserAsState by viewModel.isDeletedUser.observeAsState()
    val isDeletedUser = remember { mutableStateOf(false) }

    // 존재하는 이메일이 없는 경우
    val goToMakeNickNameAsState by viewModel.goToMakeNickName.observeAsState()

    // 이메일이 있는 경우, 로그인 하러 가기
    val goToLoginAsState by viewModel.goToLogin.observeAsState()

    // api 실패
    val failApiAsState by viewModel.failEmailCheck.observeAsState()
    val isFailApi = remember { mutableStateOf(false) }

    val prevLoginEmail = remember { mutableStateOf<GoogleEmailInfo?>(null) }


    LaunchedEffect(key1 = checkAlreadyUsedEmailAsState) {
        isAlreadyUsedEmail.value = checkAlreadyUsedEmailAsState ?: false
    }

    LaunchedEffect(key1 = goToMakeNickNameAsState) {
        goToMakeNickNameAsState?.let { data ->
            goToNexPage(data)
        }
    }

    LaunchedEffect(key1 = failApiAsState) {
        isFailApi.value = failApiAsState == true
    }

    LaunchedEffect(key1 = goToLoginAsState) {
        if (goToLoginAsState == true) {
            prevLoginEmail.value?.let { goToLogin(it) }
        }
    }

    LaunchedEffect(isDeletedUserAsState) {
        isDeletedUser.value = isDeletedUserAsState == true
    }

    GoogleSignInButton(
        goToEmailCheck = {
            viewModel.checkUserStatus(it)
            prevLoginEmail.value = it
        })

    if (isAlreadyUsedEmail.value) {
        CommonDialogView(
            title = stringResource(id = R.string.alreadyUsedEmailTitle),
            message = stringResource(id = R.string.alreadyUsedEmailDesc),
            dismissAvailable = false,
            leftButtonInfo = DialogButtonInfo(text = CommonR.string.closeDesc, color = Gray7),
            rightButtonInfo = DialogButtonInfo(text = CommonR.string.goToLogin, color = Main4),
            leftButtonEvent = {
                isAlreadyUsedEmail.value = false
                goToBack()
            },
            rightButtonEvent = {
                isAlreadyUsedEmail.value = false
                prevLoginEmail.value?.let { viewModel.savedLoginEmail(it) }
            }
        )
    }
    if (isFailApi.value) {
        ApiFailDialog(
            title = stringResource(id = R.string.joinFailTitle),
            message = stringResource(id = R.string.loginRetry)
        ) {
            isFailApi.value = false
        }
    }
    if (isDeletedUser.value) {
        CommonDialogView(
            title = stringResource(id = R.string.deletedUserTitle),
            message = stringResource(id = R.string.deletedUserDesc),
            dismissAvailable = false,
            rightButtonInfo = DialogButtonInfo(text = CommonR.string.confirm, color = Main4),
            rightButtonEvent = {
                isDeletedUser.value = false
                goToBack()
            }
        )
    }
}

@Composable
private fun EmailView(modifier: Modifier, emailClicked: () -> Unit) {
    ConstraintLayout(modifier = modifier) {
        val (logo, guide, button) = createRefs()

        Image(
            painter = painterResource(R.drawable.img_login_text),
            contentDescription = stringResource(id = R.string.waverJoinGuide),
            modifier = Modifier
                .constrainAs(guide) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                }
        )

        Image(
            painter = painterResource(id = R.drawable.img_wave),
            contentDescription = null,
            modifier = Modifier
                .constrainAs(logo) {
                    start.linkTo(parent.start)
                    bottom.linkTo(guide.top)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                }
                .padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .constrainAs(button) {
                    start.linkTo(parent.start)
                    top.linkTo(guide.bottom)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                }
                .padding(start = 40.dp, end = 40.dp, top = 72.dp)
                .clickable {
                    emailClicked()
                }
                .background(color = Gray1, shape = RoundedCornerShape(2.dp))
                .border(width = 1.dp, color = Gray3, shape = RoundedCornerShape(2.dp))
                .padding(top = 13.dp, bottom = 14.dp, start = 24.dp, end = 106.dp)
        ) {

            Row(
                modifier = Modifier.width(280.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.google),
                    contentDescription = null,
                    modifier = Modifier.sizeIn(18.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                MyText(
                    text = stringResource(id = R.string.goToJoinWithGoogle),
                    fontSize = 15.sp,
                    color = Gray10,
                    fontWeight = FontWeight.Normal
                )
            }

        }
    }
}

@Composable
fun GoogleSignInButton(goToEmailCheck: (GoogleEmailInfo) -> Unit) {
    val context = LocalContext.current
    var showGoogleEmailSelect by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf("") }

    LaunchedEffect(showGoogleEmailSelect) {
        if (showGoogleEmailSelect) {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(GoogleWebClientId)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()
            try {
                val result = CredentialManager.create(context).getCredential(
                    context = context,
                    request = request
                )
                handleSignIn(
                    result = result,
                    goToEmailCheck = goToEmailCheck,
                    onFailed = { showError = it })
            } catch (e: GetCredentialCancellationException) {
                // 사용자가 직접 닫은 것이므로 오류로 알리지 않는다.
                Log.i("ayhan", "sign-in cancelled", e)
            } catch (e: NoCredentialException) {
                Log.i("ayhan", "NoCredentialException: ", e)
                showError = handleNoCredentialException(context, goToEmailCheck)
            } catch (e: GetCredentialException) {
                Log.i("ayhan", "GetCredentialException: ", e)
                // 실패를 삼키면 버튼을 눌러도 화면이 그대로 멈춰 있어 사용자가 원인을 알 수 없다.
                showError = e.message.orEmpty()
            }

            showGoogleEmailSelect = false
        }
    }

    if (showError.isNotEmpty()) {
        CommonDialogView(
            title = stringResource(id = R.string.joinFailTitle),
            message = stringResource(id = R.string.loginRetry) + "\n${showError}",
            dismissAvailable = true,
            rightButtonInfo = DialogButtonInfo(text = CommonR.string.closeDesc, color = Gray7),
            rightButtonEvent = { showError = "" },
        )
    }

    // Google Sign-In Button
    EmailView(modifier = Modifier.fillMaxSize(), emailClicked = {
        showGoogleEmailSelect = true
    })
}


/** @return 사용자에게 보여줄 실패 사유. 성공했거나 사용자가 취소했으면 빈 문자열. */
private suspend fun handleNoCredentialException(
    context: Context,
    goToEmailCheck: (GoogleEmailInfo) -> Unit
): String {
    var failMessage = ""
    try {
        val signInWithGoogleOption = GetSignInWithGoogleOption
            .Builder(serverClientId = GoogleWebClientId)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()
        val result = CredentialManager.create(context).getCredential(
            context = context,
            request = request
        )
        handleSignIn(
            result = result,
            goToEmailCheck = goToEmailCheck,
            onFailed = { failMessage = it })
    } catch (e: GetCredentialCancellationException) {
        Log.i("ayhan", "sign-in cancelled", e)
    } catch (e: Exception) {
        Log.e("ayhan", "handleNoCredentialException: ", e)
        failMessage = e.message.orEmpty()
    }
    return failMessage
}

/**
 * 자격증명을 [GoogleEmailInfo] 로 바꿔 [goToEmailCheck] 에 넘긴다.
 *
 * 자격증명(idToken, 비밀번호)은 절대 로그로 남기지 않는다. minify 가 꺼져 있어 릴리스 빌드에도
 * 그대로 출력되고, idToken 은 유효기간 안에서 재사용 가능한 인증 수단이다.
 */
fun handleSignIn(
    result: GetCredentialResponse,
    goToEmailCheck: (GoogleEmailInfo) -> Unit,
    onFailed: (String) -> Unit = {}
) {
    // Handle the successfully returned credential.
    when (val credential = result.credential) {

        // GoogleIdToken credential
        is CustomCredential -> {
            if (credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                Log.e("ayhan", "Unexpected type of credential")
                onFailed("Unexpected type of credential")
                return
            }
            try {
                val googleIdTokenCredential =
                    GoogleIdTokenCredential.createFrom(credential.data)
                val email = googleIdTokenCredential.id
                val uid = decodeGoogleIdToken(googleIdTokenCredential.idToken)

                // uid 는 계정 식별자다. 빈 값으로 진행하면 uid 없는 계정이 서버에 생성된다.
                if (uid.isEmpty()) {
                    Log.e("ayhan", "Failed to extract uid from google id token")
                    onFailed("Invalid google id token")
                    return
                }
                goToEmailCheck(GoogleEmailInfo(email = email, uid = uid))
            } catch (e: GoogleIdTokenParsingException) {
                Log.e("ayhan", "Received an invalid google id token response", e)
                onFailed(e.message.orEmpty())
            }
        }

        else -> {
            // 이 화면은 구글 계정 가입만 지원한다. 그 외 자격증명은 처리하지 않는다.
            Log.e("ayhan", "Unexpected type of credential")
            onFailed("Unexpected type of credential")
        }
    }
}

private fun decodeGoogleIdToken(idToken: String): String {
    return try {
        val parts = idToken.split(".")
        if (parts.size != 3) {
            Log.e("ayhan", "Invalid JWT format")
            return ""
        }

        val payload = parts[1]
        val decodedBytes = Base64.decode(payload, Base64.URL_SAFE)
        val jsonString = String(decodedBytes, Charsets.UTF_8)

        // JSON 파싱해서 sub 값만 추출
        val jsonObject = JSONObject(jsonString)
        jsonObject.getString("sub")
    } catch (e: Exception) {
        Log.e("ayhan", "Failed to decode idToken", e)
        ""
    }
}

@Preview(showBackground = true)
@Composable
private fun EmailViewPreview() {
    EmailView(modifier = Modifier.fillMaxSize()) {}
}