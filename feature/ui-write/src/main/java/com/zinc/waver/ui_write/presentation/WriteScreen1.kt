package com.zinc.waver.ui_write.presentation

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue.Hidden
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.zinc.waver.model.WriteCategoryInfo
import com.zinc.waver.model.WriteOption1Info
import com.zinc.waver.model.WriteOptionsType1
import com.zinc.waver.model.WriteOptionsType1.CATEGORY
import com.zinc.waver.model.WriteOptionsType1.D_DAY
import com.zinc.waver.model.WriteOptionsType1.GOAL
import com.zinc.waver.model.WriteOptionsType1.IMAGE
import com.zinc.waver.model.WriteOptionsType1.MEMO
import com.zinc.waver.model.WriteTotalInfo
import com.zinc.waver.model.parseWrite1Info
import com.zinc.waver.ui.design.util.Keyboard
import com.zinc.waver.ui.design.util.keyboardAsState
import com.zinc.waver.ui.presentation.component.ImageSelectBottomScreen
import com.zinc.waver.ui.presentation.component.gridItems
import com.zinc.waver.ui.presentation.model.ActionWithActivity
import com.zinc.waver.ui.presentation.screen.waverplus.WaverPlusGuideScreen
import com.zinc.waver.ui.util.CameraPermission
import com.zinc.waver.ui.util.parseWithDday
import com.zinc.waver.ui.util.toLocalData
import com.zinc.waver.ui.util.toStringData
import com.zinc.waver.ui_write.R
import com.zinc.waver.ui_write.model.WriteEvent
import com.zinc.waver.ui_write.presentation.bottomScreens.CalendarSelectBottomSheet
import com.zinc.waver.ui_write.presentation.bottomScreens.CategorySelectBottomScreen
import com.zinc.waver.ui_write.presentation.bottomScreens.GoalCountBottomScreen
import com.zinc.waver.ui_write.presentation.options.AddImageItem
import com.zinc.waver.ui_write.presentation.options.ImageItem
import com.zinc.waver.ui_write.presentation.options.MemoScreen
import com.zinc.waver.ui_write.presentation.options.OptionScreen
import com.zinc.waver.ui_write.viewmodel.WriteBucketListViewModel
import com.zinc.waver.util.loadImageFiles
import kotlinx.coroutines.launch
import java.time.LocalDate


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WriteScreen1(
    originWriteTotalInfo: WriteTotalInfo,
    viewModel: WriteBucketListViewModel,
    event: (WriteEvent) -> Unit,
    goToNextPage: (WriteTotalInfo) -> Unit
) {
    val context = LocalContext.current
    val originWriteInfo1 = originWriteTotalInfo.parseWrite1Info()
    val initialized = remember { mutableStateOf(false) }
    val isKeyboardStatus by keyboardAsState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val userLimitInfoAsState by viewModel.userLimitInfo.observeAsState()

    var canUseImage by remember { mutableStateOf(false) }

    // 지금 선택된 option
    var selectedOption: WriteOptionsType1? by remember { mutableStateOf(null) }

    // 저장된 option 값들
    val updatedWriteOptions = remember { mutableStateListOf<WriteOption1Info>() }

    if (!initialized.value) {
        Log.e("ayhan", "WriteScreen1: {${initialized.value}, ${originWriteInfo1.options}")
        updatedWriteOptions.addAll(originWriteInfo1.options)
        initialized.value = true
    }

    val nextButtonClickable = remember { mutableStateOf(false) }

    val showWaverPlus = remember { mutableStateOf(false) }

    // BottomSheet 관련
    val coroutineScope = rememberCoroutineScope()
    val bottomSheetScaffoldState = rememberModalBottomSheetState(
        initialValue = Hidden, skipHalfExpanded = true
    )
    val isNeedToBottomSheetOpen: (Boolean) -> Unit = {
        coroutineScope.launch {
            if (it) {
                bottomSheetScaffoldState.show()
            } else {
                bottomSheetScaffoldState.hide()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadCategory()
    }

    // 플러스 안내를 열 때는 조회하지 않는다. 닫고 돌아온 경우에만 다시 확인한다(구독하면 한도가 바뀐다).
    var plusScreenShown by remember { mutableStateOf(false) }
    LaunchedEffect(showWaverPlus.value) {
        if (showWaverPlus.value) {
            plusScreenShown = true
        } else {
            // 첫 진입이면 앞 단계에서 받아둔 값을 재사용한다.
            viewModel.checkUserLimit(forceRefresh = plusScreenShown)
        }
    }

    LaunchedEffect(bottomSheetScaffoldState.currentValue) {
        when (bottomSheetScaffoldState.currentValue) {
            Hidden -> {
                selectedOption = null
                isNeedToBottomSheetOpen(false)
            }

            else -> {
                // Do Nothing
            }
        }
    }

    LaunchedEffect(userLimitInfoAsState) {
        canUseImage = userLimitInfoAsState?.canUseImage ?: false
    }


    // 텍스트 필드 초기 포커스를 위한 상태
    val shouldFocusTitle = remember { mutableStateOf(true) }

    // 키보드 이벤트
    LaunchedEffect(isKeyboardStatus, selectedOption) {
        if (isKeyboardStatus == Keyboard.Closed || selectedOption != null) {
            if (selectedOption == MEMO || selectedOption == GOAL) {
                // Do Nothing
            } else {
                focusManager.clearFocus()
                keyboardController?.hide()
            }
        }
    }

    // 백키 이벤트
    BackHandler {
        if (selectedOption == null) {
            event.invoke(WriteEvent.GoToBack)
        } else {
            coroutineScope.launch {
                isNeedToBottomSheetOpen.invoke(false)
            }
            selectedOption = null
        }
    }

    val loadedImages = loadImageFiles(context, images = originWriteInfo1.getImagesPaths())

    val title = remember { mutableStateOf(originWriteInfo1.title) }
    val originMemo = remember { mutableStateOf(originWriteInfo1.getMemo()) }
    val originCount = remember { mutableStateOf(originWriteInfo1.getGoalCount()) }
    val originDdayDate = remember { mutableStateOf(originWriteInfo1.getDday()) }
    val imageList = remember { mutableStateOf(loadedImages) }

    nextButtonClickable.value = title.value.isNotEmpty()


    // 기존 option 값 제거?
    fun deleteOption(type: WriteOptionsType1) {
        updatedWriteOptions.find { it.type() == type }?.let {
            updatedWriteOptions.remove(it)
        }
    }

// 각 option 값이 들어왔을 때 해야하는 것들 정리
    if (selectedOption == MEMO) {
        MemoScreen(
            originMemo = originMemo.value,
            memoChanged = {
                originMemo.value = it
                deleteOption(MEMO)
                updatedWriteOptions.add(
                    WriteOption1Info.Memo(it)
                )
                selectedOption = null
            }, closeClicked = {
                selectedOption = null
            })
    }

    if (selectedOption == IMAGE) {
        CameraPermission(isAvailable = {
            selectedOption = if (it) {
                IMAGE
            } else {
                null
            }
        })
    }


// 바텀시트
    ModalBottomSheetLayout(
        sheetState = bottomSheetScaffoldState,
        modifier = Modifier
            .imePadding()
            .navigationBarsPadding()
            .statusBarsPadding(),
        sheetContent = {
            Box(Modifier.defaultMinSize(minHeight = 1.dp)) {
                when (selectedOption) {
                    IMAGE -> {
                        ImageSelectBottomScreen(selectedType = { it ->
                            event.invoke(
                                WriteEvent.ActivityAction(
                                    ActionWithActivity.AddImage(
                                        type = it,
                                        failed = {
                                            Toast.makeText(
                                                context,
                                                "이미지 로드에 실패했습니다.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            selectedOption = null
                                            isNeedToBottomSheetOpen.invoke(false)
                                        },
                                        succeed = { imageInfo ->
                                            Log.e("ayhan", "imageInfo : $imageInfo")
                                            imageList.value += imageInfo
                                            Log.e("ayhan", "imageList : ${imageList.value}")
                                            selectedOption = null
                                            isNeedToBottomSheetOpen.invoke(false)

                                            val prevImageOption =
                                                updatedWriteOptions.find { options -> options.type() == IMAGE }
                                            if (prevImageOption != null) {
                                                updatedWriteOptions.remove(prevImageOption)
                                            }
                                            updatedWriteOptions.add(
                                                WriteOption1Info.Images(imageList.value.map { it.path })
                                            )
                                        })
                                )
                            )
                        })
                        isNeedToBottomSheetOpen.invoke(true)
                    }

                    CATEGORY -> {
                        CategorySelectBottomScreen(
                            confirmed = { category ->
                                deleteOption(CATEGORY)
                                updatedWriteOptions.add(
                                    WriteOption1Info.Category(
                                        WriteCategoryInfo(
                                            category.id,
                                            category.name,
                                            category.defaultYn
                                        )
                                    )
                                )
                                selectedOption = null
                                isNeedToBottomSheetOpen.invoke(false)
                            },
                            goToAddCategory = {
                                event.invoke(WriteEvent.GoToAddCategory)
                                selectedOption = null
                                isNeedToBottomSheetOpen.invoke(false)
                            })
                        isNeedToBottomSheetOpen.invoke(true)
                    }

                    D_DAY -> {
                        CalendarSelectBottomSheet(
                            confirmed = {
                                deleteOption(D_DAY)
                                updatedWriteOptions.add(
                                    WriteOption1Info.Dday(
                                        it.toStringData(),
                                        it.parseWithDday()
                                    )
                                )
                                originDdayDate.value = WriteOption1Info.Dday(
                                    it.toStringData(),
                                    it.parseWithDday()
                                )
                                selectedOption = null
                                isNeedToBottomSheetOpen.invoke(false)
                            },
                            canceled = {
                                selectedOption = null
                                isNeedToBottomSheetOpen.invoke(false)
                            },
                            selectedDate = originDdayDate.value?.localDate?.toLocalData()
                                ?: LocalDate.now()
                        )
                        isNeedToBottomSheetOpen.invoke(true)
                    }

                    GOAL -> {
                        GoalCountBottomScreen(
                            originCount = originCount.value?.toString().orEmpty(),
                            canceled = {
                                selectedOption = null
                                isNeedToBottomSheetOpen.invoke(false)
                            }, confirmed = {
                                val goalCount = it.toIntOrNull() ?: return@GoalCountBottomScreen
                                deleteOption(GOAL)
                                updatedWriteOptions.add(
                                    WriteOption1Info.GoalCount(goalCount)
                                )
                                selectedOption = null
                                isNeedToBottomSheetOpen.invoke(false)
                                originCount.value = goalCount
                            })
                        isNeedToBottomSheetOpen.invoke(true)
                    }

                    else -> {
                        // Do Nothing
                    }
                }
            }
        },
        sheetShape = RoundedCornerShape(topEnd = 16.dp, topStart = 16.dp)
    ) {
        if (selectedOption != MEMO) {
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .imePadding()
            ) {
                val (appBar, contents, option) = createRefs()

                WriteAppBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .constrainAs(appBar) {
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            top.linkTo(parent.top)
                        },
                    nextButtonClickable = nextButtonClickable.value,
                    rightText = R.string.writeGoToNext,
                    clickEvent = {
                        when (it) {
                            WriteAppBarClickEvent.CloseClicked -> {
                                event.invoke(WriteEvent.GoToBack)
                            }

                            WriteAppBarClickEvent.NextClicked -> {
                                Log.e("ayhan", "WriteScreen1: ${updatedWriteOptions.toList()}")
                                goToNextPage(
                                    originWriteTotalInfo.copy(
                                        title = title.value,
                                        options = updatedWriteOptions.toList()
                                    )
                                )
                            }
                        }
                    })

                LazyColumn(
                    modifier = Modifier
                        .constrainAs(contents) {
                            top.linkTo(appBar.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            bottom.linkTo(option.top)
                            height = Dimension.fillToConstraints
                        }) {

                    item {
                        WriteTitleFieldView(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 28.dp)
                                .padding(horizontal = 28.dp),
                            title = title.value,
                            shouldFocus = shouldFocusTitle.value,
                            textChanged = {
                                title.value = it
                                nextButtonClickable.value = it.isNotEmpty()
                                shouldFocusTitle.value = false
                            })
                    }

                    item {
                        if (updatedWriteOptions.find { it.type() == MEMO } != null) {
                            MemoOptionView(
                                modifier = Modifier
                                    .padding(top = 28.dp)
                                    .clickable {
                                        selectedOption = MEMO
                                    },
                                memoText = originMemo.value
                            )
                        }
                    }

                    item {
                        val hasMemo = updatedWriteOptions.find { it.type() == MEMO } != null
                        if (updatedWriteOptions.find { it.type() == IMAGE } != null) {
                            gridItems(
                                data = imageList.value,
                                maxRow = 3,
                                modifier = Modifier
                                    .padding(horizontal = 28.dp)
                                    .padding(top = if (hasMemo) 28.dp else 32.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(
                                    32.dp,
                                    Alignment.CenterHorizontally
                                ),
                                verticalSpace = 28.dp,
                                itemContent = {
                                    ImageItem(
                                        imageInfo = it,
                                        deleteImage = { removeImage ->
                                            imageList.value -= removeImage
                                            if (imageList.value.isEmpty()) {
                                                updatedWriteOptions.removeIf { it.type() == IMAGE }
                                            }
                                        })

                                    if (imageList.value.isNotEmpty() && imageList.value.size < 3 && it == imageList.value.last()) {
                                        AddImageItem(canUseImage = canUseImage) {
                                            if (canUseImage) {
                                                selectedOption = IMAGE
                                            } else {
                                                showWaverPlus.value = true
                                            }

                                        }
                                    }
                                },
                                emptyContent = {
                                    Spacer(modifier = Modifier.size(80.dp))
                                }
                            )
                        }
                    }

                    item {
                        OptionScreen(options = updatedWriteOptions.toList()) {
                            selectedOption = it
                        }
                    }
                }

                BottomOptionView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .constrainAs(
                            option
                        ) {
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            bottom.linkTo(parent.bottom)
                        },
                    currentClickedOptions = updatedWriteOptions.map { it.type() }.toSet(),
                    optionClicked = { clickedOption ->
                        val isAlreadySelected =
                            updatedWriteOptions.find { it.type() == clickedOption }
                        if (isAlreadySelected != null) {
                            when (clickedOption) {
                                IMAGE,
                                MEMO,
                                CATEGORY,
                                D_DAY,
                                GOAL -> {
                                    updatedWriteOptions.remove(isAlreadySelected)
                                }
                            }
                        } else {
                            when (clickedOption) {
                                IMAGE -> {
                                    if (imageList.value.isEmpty()) {
                                        selectedOption = clickedOption
                                    } else {
                                        updatedWriteOptions.add(
                                            WriteOption1Info.Images(imageList.value.map { it.path })
                                        )
                                    }
                                }

                                MEMO, CATEGORY, D_DAY, GOAL -> {
                                    selectedOption = clickedOption
                                }
                            }
                        }
                    })
            }
        }
    }

    AnimatedVisibility(showWaverPlus.value, enter = fadeIn(), exit = fadeOut()) {
        if (showWaverPlus.value) {
            WaverPlusGuideScreen(onBackPressed = {
                showWaverPlus.value = false
            }, inAppBillingShow = { type ->
                event.invoke(WriteEvent.ActivityAction(ActionWithActivity.InAppBilling(type)))
            })
        }
    }
}
