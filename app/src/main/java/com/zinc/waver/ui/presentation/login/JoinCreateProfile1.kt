package com.zinc.waver.ui.presentation.login

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.zinc.waver.R
import com.zinc.waver.ui.design.theme.Error2
import com.zinc.waver.ui.design.theme.Gray1
import com.zinc.waver.ui.design.theme.Gray4
import com.zinc.waver.ui.design.theme.Gray5
import com.zinc.waver.ui.design.theme.Gray6
import com.zinc.waver.ui.design.theme.Main4
import com.zinc.waver.ui.presentation.component.ImageSelectBottomScreen
import com.zinc.waver.ui.presentation.component.MyText
import com.zinc.waver.ui.presentation.component.MyTextField
import com.zinc.waver.ui.presentation.component.TitleView
import com.zinc.waver.ui.presentation.component.profile.ProfileUpdateView
import com.zinc.waver.ui.presentation.login.model.CreateProfileInfo
import com.zinc.waver.ui.presentation.login.model.NicknameCheckState
import com.zinc.waver.ui.presentation.model.ActionWithActivity
import com.zinc.waver.ui.util.dpToSp
import com.zinc.waver.ui.util.isValidNicknameCheck
import com.zinc.waver.util.FileUtil.getFileFromUri
import java.io.File
import java.io.FileOutputStream
import com.zinc.waver.ui_common.R as CommonR

// helper: drawable을 bitmap으로 변환
private fun drawableToBitmap(drawable: Drawable, reqWidth: Int = -1, reqHeight: Int = -1): Bitmap {
    val width = when {
        reqWidth > 0 -> reqWidth
        drawable.intrinsicWidth > 0 -> drawable.intrinsicWidth
        else -> 512
    }
    val height = when {
        reqHeight > 0 -> reqHeight
        drawable.intrinsicHeight > 0 -> drawable.intrinsicHeight
        else -> 512
    }
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

@Composable
fun JoinCreateProfile1(
    createProfileInfo: CreateProfileInfo,
    goToNext: (CreateProfileInfo) -> Unit,
    addImageAction: (ActionWithActivity.AddImage) -> Unit,
) {
    val createUserViewModel: JoinNickNameViewModel = hiltViewModel()

    val checkState by createUserViewModel.nicknameCheckState
        .observeAsState(NicknameCheckState.Idle)

    // 검사가 비동기라, 검사를 요청한 시점의 입력값을 들고 있다가 결과가 오면 그대로 넘긴다.
    var pendingCreateInfo: CreateProfileInfo? by remember { mutableStateOf(null) }
    var nickNameData by remember { mutableStateOf(createProfileInfo.nickName) }

    LaunchedEffect(checkState, pendingCreateInfo) {
        val pending = pendingCreateInfo ?: return@LaunchedEffect
        if (checkState.isAvailableFor(pending.nickName)) {
            pendingCreateInfo = null
            goToNext(pending)
        } else if (checkState !is NicknameCheckState.Checking) {
            // 중복이거나 검사 실패. 대기 중이던 입력값을 버리고 화면에 오류만 남긴다.
            pendingCreateInfo = null
        }
    }

    JoinCreateProfile1(
        isAlreadyUsedNickName = checkState.isDuplicatedFor(nickNameData),
        isCheckFail = checkState is NicknameCheckState.Failed,
        isChecking = checkState is NicknameCheckState.Checking,
        goToNext = {
            // 지금 입력된 닉네임이 통과한 경우에만 검사를 건너뛴다.
            // 결과만 보고 판단하면 2단계에서 뒤로 온 뒤 닉네임을 바꿔도 검사 없이 통과한다.
            if (checkState.isAvailableFor(it.nickName)) {
                goToNext(it)
            } else {
                pendingCreateInfo = it
                createUserViewModel.checkIsAlreadyUsedName(it.nickName)
            }
        },
        createProfileInfo = createProfileInfo,
        nickNameData = nickNameData,
        updateNickName = {
            nickNameData = it
        },
        addImageAction = addImageAction
    )
}

@Composable
private fun JoinCreateProfile1(
    isAlreadyUsedNickName: Boolean,
    isCheckFail: Boolean,
    isChecking: Boolean,
    createProfileInfo: CreateProfileInfo,
    nickNameData: String,
    updateNickName: (String) -> Unit,
    addImageAction: (ActionWithActivity.AddImage) -> Unit,
    goToNext: (CreateProfileInfo) -> Unit,
) {
    val context = LocalContext.current

    var showSelectCameraType by remember { mutableStateOf(false) }

    val updateImageFile: MutableState<File?> =
        remember { mutableStateOf(createProfileInfo.imgFile) }
    val updateImagePath: MutableState<String?> =
        remember { mutableStateOf(createProfileInfo.imgPath) }

    // 검사 결과로 버튼을 잠그지 않는다. 잠그면 검사 후 진행에 실패했을 때 빠져나갈 길이 없다.
    val isButtonEnabled =
        !isChecking && nickNameData.length > 2 && isValidNicknameCheck(nickNameData)
    val bottomSheetScaffoldState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden, skipHalfExpanded = true
    )

    LaunchedEffect(showSelectCameraType) {
        if (showSelectCameraType) {
            bottomSheetScaffoldState.show()
        } else {
            bottomSheetScaffoldState.hide()
        }
    }

    LaunchedEffect(bottomSheetScaffoldState.currentValue) {
        if (bottomSheetScaffoldState.currentValue == ModalBottomSheetValue.Hidden) {
            showSelectCameraType = false
        }
    }

    LaunchedEffect(Unit) {
        // 이미 이미지가 있으면(사용자 선택 또는 이전 진입에서 지정한 기본값) 건드리지 않는다.
        // 가드가 없으면 2단계에서 뒤로 왔을 때 고른 사진이 랜덤 기본값으로 덮인다.
        if (updateImageFile.value != null) return@LaunchedEffect

        val randomProfile = listOf(
            CommonR.drawable.profile_icon_1,
            CommonR.drawable.profile_icon_2,
            CommonR.drawable.profile_icon_3
        ).random()

        try {
            val uri = ("android.resource://" + context.packageName + "/" + randomProfile).toUri()

            // 먼저 FileUtil을 사용해 파일을 만들어본다 (요청에 따라 FileUtil 수정은 하지 않음)
            val file = try {
                getFileFromUri(context, uri)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }

            if (file != null) {
                // 파일에서 비트맵 디코드가 가능한지 확인
                val decoded = try {
                    BitmapFactory.decodeFile(file.path)
                } catch (_: Throwable) {
                    null
                }

                if (decoded != null) {
                    try {
                        if (!decoded.isRecycled) decoded.recycle()
                    } catch (_: Exception) {
                    }
                    updateImageFile.value = file
                    updateImagePath.value = "file://${file.path}"
                } else {
                    // decode 실패: 리소스가 벡터(XML)일 가능성이 있음. Drawable로 직접 렌더링해서 파일로 저장
                    try {
                        val drawable: Drawable? = ResourcesCompat.getDrawable(
                            context.resources,
                            randomProfile,
                            context.theme
                        )
                        if (drawable != null) {
                            val bmp = drawableToBitmap(drawable)
                            val outFile =
                                File(context.cacheDir, "random_profile_${randomProfile}.png")
                            FileOutputStream(outFile).use { fos ->
                                bmp.compress(Bitmap.CompressFormat.PNG, 100, fos)
                                fos.flush()
                            }
                            // 메모리 해제
                            try {
                                if (!bmp.isRecycled) bmp.recycle()
                            } catch (_: Exception) {
                            }

                            updateImageFile.value = outFile
                            updateImagePath.value = "file://${outFile.path}"
                        } else {
                            // drawable도 못 얻으면 기존 파일을 사용하거나 null
                            updateImageFile.value = file
                            updateImagePath.value = "file://${file.path}"
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        updateImageFile.value = file
                        updateImagePath.value = "file://${file.path}"
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "이미지 처리 중 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
        }

    }

    ModalBottomSheetLayout(
        modifier = Modifier
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
        sheetState = bottomSheetScaffoldState,
        sheetContent = {
            if (showSelectCameraType) {
                ImageSelectBottomScreen(selectedType = {
                    addImageAction.invoke(
                        ActionWithActivity.AddImage(
                            type = it,
                            failed = {
                                Toast.makeText(
                                    context,
                                    "이미지 로드에 실패했습니다.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                showSelectCameraType = false
                            },
                            succeed = { imageInfo ->
                                showSelectCameraType = false
                                updateImagePath.value = imageInfo.path
                                updateImageFile.value = imageInfo.file
                            })
                    )
                })
            }
        },
        sheetShape = RoundedCornerShape(topEnd = 16.dp, topStart = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TitleView(
                title = stringResource(R.string.goToJoin1),
                isDividerVisible = true
            )

            Spacer(modifier = Modifier.padding(top = 36.dp))

            ProfileUpdateView(
                currentProfileUri = null,
                updatePath = updateImagePath,
                imageUpdateButtonClicked = {
                    showSelectCameraType = true
                })

            Spacer(modifier = Modifier.padding(top = 47.dp))

            ProfileNickNameEditView(
                prevNickName = nickNameData,
                isAlreadyUsedName = isAlreadyUsedNickName,
                isCheckFail = isCheckFail,
                dataChanged = {
                    updateNickName(it)
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
                    .background(color = if (isButtonEnabled) Main4 else Gray5)
                    .clickable(isButtonEnabled) {
                        goToNext(
                            CreateProfileInfo(
                                nickName = nickNameData,
                                imgPath = updateImagePath.value,
                                imgFile = updateImageFile.value
                            )
                        )
                    }
                    .padding(vertical = 18.dp)
            )
        }
    }
}

@Composable
private fun ProfileNickNameEditView(
    prevNickName: String,
    dataChanged: (String) -> Unit,
    isAlreadyUsedName: Boolean,
    isCheckFail: Boolean
) {
    val showError = isAlreadyUsedName || isCheckFail
    var isFocused by remember { mutableStateOf(false) }
    var isValidNickname by remember { mutableStateOf(false) }

    LaunchedEffect(prevNickName) {
        isValidNickname = isValidNicknameCheck(prevNickName)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
            .padding(horizontal = 28.dp)
    ) {

        MyText(
            text = stringResource(id = CommonR.string.profileSettingNickNameTitle),
            color = Gray6,
            fontSize = dpToSp(dp = 14.dp)
        )

        MyTextField(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused },
            value = prevNickName,
            singleLine = true,
            textStyle = TextStyle(fontSize = dpToSp(dp = 20.dp)),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            onValueChange = { changeText ->
                dataChanged(changeText)
            },
            decorationBox = { innerTextField ->
                Row {
                    if (prevNickName.isEmpty() && !isFocused) {
                        MyText(
                            text = stringResource(CommonR.string.addProfileNickname),
                            color = Gray4,
                            fontSize = dpToSp(20.dp)
                        )
                    }
                    innerTextField()  //<-- Add this
                }
            }
        )

        Divider(
            modifier = Modifier.padding(top = 15.5.dp),
            color = if (showError) Error2 else Gray5,
            thickness = 1.dp
        )

        if (showError) {
            val text =
                if (isCheckFail) CommonR.string.nicknameCheckFail else CommonR.string.alreadyUsedNickNameGuide
            MyText(
                modifier = Modifier.padding(top = 7.5.dp),
                text = stringResource(id = text),
                color = Error2,
                fontSize = dpToSp(dp = 12.dp)
            )
        }

        if (prevNickName.length < 3 || !isValidNickname) {
            val text = CommonR.string.enterCharacter
            MyText(
                modifier = Modifier.padding(top = 7.5.dp),
                text = stringResource(id = text),
                color = Gray6,
                fontSize = dpToSp(dp = 12.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun JoinNickNameScreenPreview() {
    JoinCreateProfile1(
        nickNameData = "",
        createProfileInfo = CreateProfileInfo(),
        goToNext = { },
        isAlreadyUsedNickName = false,
        isCheckFail = false,
        isChecking = false,
        updateNickName = {},
        addImageAction = {})
}

@Preview(showBackground = true)
@Composable
private fun ProfileNickNameEditViewPreview() {
    ProfileNickNameEditView(
        prevNickName = "",
        dataChanged = {},
        isAlreadyUsedName = false,
        isCheckFail = false
    )
}