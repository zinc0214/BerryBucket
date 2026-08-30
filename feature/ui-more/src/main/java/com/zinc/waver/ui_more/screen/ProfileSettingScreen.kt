package com.zinc.waver.ui_more.screen

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.zinc.waver.model.AddImageType
import com.zinc.waver.model.DialogButtonInfo
import com.zinc.waver.ui.design.theme.Gray1
import com.zinc.waver.ui.design.theme.Main4
import com.zinc.waver.ui.presentation.component.ImageSelectBottomScreen
import com.zinc.waver.ui.presentation.component.dialog.ApiFailDialog
import com.zinc.waver.ui.presentation.component.dialog.CommonDialogView
import com.zinc.waver.ui.presentation.component.profile.ProfileEditView
import com.zinc.waver.ui.presentation.component.profile.ProfileUpdateView
import com.zinc.waver.ui.presentation.model.ActionWithActivity
import com.zinc.waver.ui.presentation.model.NicknameCheckState
import com.zinc.waver.ui.presentation.model.ProfileEditData
import com.zinc.waver.ui.util.isValidNicknameCheck
import com.zinc.waver.ui_more.R
import com.zinc.waver.ui_more.components.ProfileSettingTitle
import com.zinc.waver.ui_more.viewModel.MoreViewModel
import com.zinc.waver.util.RandomProfileImageUtil
import com.zinc.waver.util.downloadImageWithCoil
import kotlinx.coroutines.launch
import java.io.File
import com.zinc.waver.ui_common.R as CommonR

@Composable
fun ProfileSettingScreen(
    onBackPressed: () -> Unit,
    addImageAction: (ActionWithActivity.AddImage) -> Unit,
) {

    val context = LocalContext.current
    val viewModel: MoreViewModel = hiltViewModel()

    val profileInfoAsState by viewModel.profileInfo.observeAsState()
    val loadProfileFail by viewModel.profileLoadFail.observeAsState()
    val profileUpdateSucceedAsSate by viewModel.profileUpdateSucceed.observeAsState()
    val profileUpdateFailAsState by viewModel.profileUpdateFail.observeAsState()
    val nicknameCheckState by viewModel.nicknameCheckState
        .observeAsState(NicknameCheckState.Idle)

    val profileInfo = remember { mutableStateOf(profileInfoAsState) }
    val isDataChanged = remember { mutableStateOf(false) }
    val showApiFailDialog = remember { mutableStateOf(false) }
    val showUpdateSucceedDialog = remember { mutableStateOf(false) }
    val showUpdateFailDialog = remember { mutableStateOf(false) }

    val updateImagePath: MutableState<String?> = remember { mutableStateOf(null) }
    val updateImageFile: MutableState<File?> = remember { mutableStateOf(null) }
    var showSelectCameraType by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val bottomSheetScaffoldState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden, skipHalfExpanded = true
    )

    if (profileInfo.value == null) {
        viewModel.loadMyProfile()
    }

    val nickNameData = remember {
        mutableStateOf(
            ProfileEditData(
                dataType = ProfileEditData.ProfileDataType.NICKNAME,
                prevText = profileInfo.value?.name.orEmpty()
            )
        )

    }
    val bioData = remember {
        mutableStateOf(
            ProfileEditData(
                dataType = ProfileEditData.ProfileDataType.BIO,
                prevText = profileInfo.value?.bio.orEmpty()
            )
        )
    }
    LaunchedEffect(key1 = profileUpdateSucceedAsSate) {
        showUpdateSucceedDialog.value = profileUpdateSucceedAsSate ?: false
    }

    LaunchedEffect(key1 = loadProfileFail) {
        showApiFailDialog.value = loadProfileFail ?: false
    }

    LaunchedEffect(key1 = profileUpdateFailAsState) {
        showUpdateFailDialog.value = profileUpdateFailAsState ?: false
    }

    LaunchedEffect(key1 = profileInfoAsState) {
        profileInfo.value = profileInfoAsState
        nickNameData.value = nickNameData.value.copy(prevText = profileInfo.value?.name.orEmpty())
        bioData.value = bioData.value.copy(prevText = profileInfo.value?.bio.orEmpty())
        val downloadImageInfo = downloadImageWithCoil(
            context = context,
            imageUrl = profileInfoAsState?.imgUrl.orEmpty(),
            0
        )
        updateImageFile.value = downloadImageInfo?.file
        updateImagePath.value = downloadImageInfo?.path
    }

    LaunchedEffect(
        key1 = nickNameData.value,
        key2 = bioData.value,
        key3 = updateImagePath.value,
        block = {
            val isEmpty = nickNameData.value.prevText.isEmpty() || bioData.value.prevText.isEmpty()
            isDataChanged.value = isEmpty.not() &&
                    (profileInfo.value?.name != nickNameData.value.prevText || profileInfo.value?.bio != bioData.value.prevText || updateImagePath.value != null)
        })

    val isNeedToBottomSheetOpen: (Boolean) -> Unit = {
        coroutineScope.launch {
            if (it) {
                bottomSheetScaffoldState.show()
            } else {
                bottomSheetScaffoldState.hide()
            }
        }
    }

    LaunchedEffect(bottomSheetScaffoldState.currentValue) {
        when (bottomSheetScaffoldState.currentValue) {
            ModalBottomSheetValue.Hidden -> {
                showSelectCameraType = false
            }

            else -> {
                // Do Nothing
            }
        }
    }

    LaunchedEffect(showSelectCameraType) {
        if (showSelectCameraType) {
            bottomSheetScaffoldState.show()
        } else {
            bottomSheetScaffoldState.hide()
        }
    }

    // 문자열 리소스를 미리 로드
    val defaultImageFailedMsg = stringResource(id = R.string.defaultImageLoadFailed)
    val imageFailedMsg = stringResource(id = R.string.imageLoadFailed)
    val cameraPermissionMsg = stringResource(id = R.string.cameraPermissionRequired)
    val galleryPermissionMsg = stringResource(id = R.string.galleryPermissionRequired)

    val handleImageTypeSelection: (AddImageType) -> Unit = { imageType ->
        when (imageType) {
            AddImageType.DEFAULT -> {
                val file = RandomProfileImageUtil.getRandomProfileImageFile(context)
                if (file != null) {
                    updateImageFile.value = file
                    updateImagePath.value = "file://${file.path}"
                } else {
                    Toast.makeText(context, defaultImageFailedMsg, Toast.LENGTH_SHORT).show()
                }
                isNeedToBottomSheetOpen(false)
            }

            AddImageType.CAMERA -> {
                // 카메라 권한 확인
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    // 권한 있음 - 바로 카메라 실행
                    addImageAction.invoke(
                        ActionWithActivity.AddImage(
                            type = AddImageType.CAMERA,
                            failed = {
                                Toast.makeText(
                                    context,
                                    imageFailedMsg,
                                    Toast.LENGTH_SHORT
                                ).show()
                                isNeedToBottomSheetOpen(false)
                            },
                            succeed = { imageInfo ->
                                updateImagePath.value = imageInfo.path
                                updateImageFile.value = imageInfo.file
                                isNeedToBottomSheetOpen(false)
                            }
                        )
                    )
                } else {
                    // 권한 없음 - 바텀시트 닫고 토스트 메시지 표시
                    isNeedToBottomSheetOpen(false)
                    Toast.makeText(
                        context,
                        cameraPermissionMsg,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            AddImageType.GALLERY -> {
                // 갤러리 권한 확인 (제한된 액세스 포함)
                val galleryPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_IMAGES
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }

                val permissionStatus = ContextCompat.checkSelfPermission(
                    context,
                    galleryPermission
                )

                // PERMISSION_GRANTED(0) 또는 제한된 액세스 상태에서도 갤러리 접근 허용
                if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                    // 권한 있음 - 바로 갤러리 실행
                    addImageAction.invoke(
                        ActionWithActivity.AddImage(
                            type = AddImageType.GALLERY,
                            failed = {
                                Toast.makeText(
                                    context,
                                    imageFailedMsg,
                                    Toast.LENGTH_SHORT
                                ).show()
                                isNeedToBottomSheetOpen(false)
                            },
                            succeed = { imageInfo ->
                                updateImagePath.value = imageInfo.path
                                updateImageFile.value = imageInfo.file
                                isNeedToBottomSheetOpen(false)
                            }
                        )
                    )
                } else {
                    // 권한 없음 - 바텀시트 닫고 토스트 메시지 표시
                    isNeedToBottomSheetOpen(false)
                    Toast.makeText(
                        context,
                        galleryPermissionMsg,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    ModalBottomSheetLayout(
        sheetState = bottomSheetScaffoldState,
        sheetContent = {
            if (showSelectCameraType) {
                ImageSelectBottomScreen(selectedType = { imageType ->
                    handleImageTypeSelection(imageType)
                })
            }
        },
        sheetShape = RoundedCornerShape(topEnd = 16.dp, topStart = 16.dp)
    ) {
        profileInfo.value?.let { profile ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .background(Gray1),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ProfileSettingTitle(
                    saveButtonEnable = isDataChanged.value &&
                            isValidNicknameCheck(nickNameData.value.prevText) &&
                            nicknameCheckState !is NicknameCheckState.Checking,
                    backClicked = {
                        onBackPressed()
                    },
                    saveClicked = {
                        viewModel.updateMyProfile(
                            name = nickNameData.value.prevText,
                            bio = bioData.value.prevText,
                            profileImage = updateImageFile.value,
                            // 닉네임을 바꾼 경우에만 중복 검사를 거친다.
                            needNicknameCheck = profile.name != nickNameData.value.prevText
                        )
                    }
                )

                Spacer(modifier = Modifier.padding(top = 44.dp))

                ProfileUpdateView(
                    currentProfileUri = profile.imgUrl,
                    updatePath = updateImagePath,
                    imageUpdateButtonClicked = {
                        showSelectCameraType = true
                    })

                Spacer(modifier = Modifier.padding(top = 41.dp))

                ProfileEditView(
                    editData = nickNameData.value,
                    needLengthCheck = false,
                    dataChanged = { changedData ->
                        nickNameData.value = nickNameData.value.copy(prevText = changedData)
                    },
                    isAlreadyUsedName = nicknameCheckState
                        .isDuplicatedFor(nickNameData.value.prevText)
                )

                ProfileEditView(
                    editData = bioData.value,
                    needLengthCheck = true,
                    dataChanged = { changedData ->
                        bioData.value = bioData.value.copy(prevText = changedData)
                    },
                    isAlreadyUsedName = false
                )
            }
        }
    }

    if (showApiFailDialog.value) {
        ApiFailDialog(
            title = stringResource(id = R.string.loadFailProfile),
            message = stringResource(id = CommonR.string.retryDesc),
            dismissEvent = {
                showApiFailDialog.value = false
                onBackPressed()
            })
    }

    if (showUpdateSucceedDialog.value) {
        CommonDialogView(
            message = stringResource(id = R.string.profileUpdateSuccessTitle),
            dismissAvailable = false,
            rightButtonInfo = DialogButtonInfo(text = CommonR.string.confirm, color = Main4),
            rightButtonEvent = {
                showUpdateSucceedDialog.value = false
                onBackPressed()
            })
    }

    if (showUpdateFailDialog.value) {
        ApiFailDialog(
            message = stringResource(id = R.string.profileUpdateFailTitle),
            dismissEvent = {
                showUpdateFailDialog.value = false
            })
    }
}