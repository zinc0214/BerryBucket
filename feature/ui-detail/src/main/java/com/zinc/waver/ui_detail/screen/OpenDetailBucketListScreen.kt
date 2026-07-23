package com.zinc.waver.ui_detail.screen

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.zinc.common.models.AddBucketCommentRequest
import com.zinc.common.models.BucketStatus
import com.zinc.common.models.YesOrNo
import com.zinc.waver.model.BucketDetailUiInfo
import com.zinc.waver.model.Comment
import com.zinc.waver.model.CommentLongClicked
import com.zinc.waver.model.CommentMentionInfo
import com.zinc.waver.model.DetailAppBarClickEvent
import com.zinc.waver.model.DetailClickEvent
import com.zinc.waver.model.DetailDescType
import com.zinc.waver.model.DetailLoadFailStatus
import com.zinc.waver.model.ReportInfo
import com.zinc.waver.model.ReportType
import com.zinc.waver.model.SuccessButtonInfo
import com.zinc.waver.model.WriteCategoryInfo
import com.zinc.waver.model.WriteOpenType
import com.zinc.waver.ui.design.util.Keyboard
import com.zinc.waver.ui.design.util.keyboardAsState
import com.zinc.waver.ui.presentation.component.dialog.ApiFailDialog
import com.zinc.waver.ui.util.WaverLoading
import com.zinc.waver.ui_common.R
import com.zinc.waver.ui_detail.component.CommentEditView
import com.zinc.waver.ui_detail.component.DetailSuccessButtonView
import com.zinc.waver.ui_detail.component.DetailTopAppBar
import com.zinc.waver.ui_detail.component.GoalCountUpdateDialog
import com.zinc.waver.ui_detail.component.MyCommentOptionClicked
import com.zinc.waver.ui_detail.component.MyCommentSelectedDialog
import com.zinc.waver.ui_detail.component.MyDetailAppBarMoreMenuDialog
import com.zinc.waver.ui_detail.component.OpenDetailContentView
import com.zinc.waver.ui_detail.component.OtherCommentOptionClicked
import com.zinc.waver.ui_detail.component.OtherCommentSelectedDialog
import com.zinc.waver.ui_detail.component.OtherDetailAppBarMoreMenuDialog
import com.zinc.waver.ui_detail.component.ShowReportScreen
import com.zinc.waver.ui_detail.model.GoalCountUpdateEvent
import com.zinc.waver.ui_detail.model.MyBucketMoreMenuEvent
import com.zinc.waver.ui_detail.model.OpenBucketDetailEvent2
import com.zinc.waver.ui_detail.model.OpenBucketDetailInternalEvent
import com.zinc.waver.ui_detail.model.OtherBucketMenuEvent
import com.zinc.waver.ui_detail.model.toUpdateUiModel
import com.zinc.waver.ui_detail.viewmodel.DetailViewModel
import com.zinc.waver.util.createImageInfoWithPath

@Composable
fun OpenDetailBucketListScreen(
    detailId: String,
    writerId: String,
    isMine: Boolean,
    goToEvent: (OpenBucketDetailEvent2) -> Unit,
    backPress: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val detailInfoAsState by viewModel.bucketBucketDetailUiInfo.observeAsState()
    val validMentionList by viewModel.validMentionList.observeAsState()
    val loadFailAsState by viewModel.loadFail.observeAsState()
    val bucketDeleteAsState by viewModel.goToBack.observeAsState()
    val showLoadingAsState by viewModel.showLoading.observeAsState()

    val keyboardController = LocalSoftwareKeyboardController.current
    val showLoading = remember { mutableStateOf(false) }
    var detailInfo by remember { mutableStateOf(detailInfoAsState) }
    var internalEvent: OpenBucketDetailInternalEvent by remember {
        mutableStateOf(
            OpenBucketDetailInternalEvent.None
        )
    }

    LaunchedEffect(Unit) {
        viewModel.loadInitData(detailId, writerId, isMine)
    }

    LaunchedEffect(showLoadingAsState) {
        showLoading.value = showLoadingAsState == true
    }

    LaunchedEffect(loadFailAsState) {
        loadFailAsState?.let { internalEvent = OpenBucketDetailInternalEvent.Error(it) }
    }

    LaunchedEffect(detailInfoAsState) {
        detailInfoAsState?.let {
            detailInfo = it
        }
    }

    LaunchedEffect(bucketDeleteAsState) {
        if (bucketDeleteAsState == true) {
            backPress()
        }
    }

    LaunchedEffect(internalEvent) {
        val event = internalEvent

        if (event is OpenBucketDetailInternalEvent.ViewModelEvent) {
            when (event) {
                OpenBucketDetailInternalEvent.ViewModelEvent.Achieve -> {
                    viewModel.achieveMyBucket()
                }

                is OpenBucketDetailInternalEvent.ViewModelEvent.DeleteMyComment -> {
                    viewModel.requestDeleteBucketComment(event.commentId)
                }

                OpenBucketDetailInternalEvent.ViewModelEvent.UpdateBucket -> {
                    viewModel.getBucketDetail(
                        bucketId = detailId,
                        writerId = writerId,
                        isMine = isMine
                    )
                }

                is OpenBucketDetailInternalEvent.ViewModelEvent.UpdateGoalCount -> {
                    viewModel.requestGoalCountUpdate(event.count)
                }

                is OpenBucketDetailInternalEvent.ViewModelEvent.AddComment -> {
                    Log.e("ayhan", "AddComment : ${event.comment}")
                    viewModel.requestAddBucketComment(event.comment)
                    keyboardController?.hide()
                }

                OpenBucketDetailInternalEvent.ViewModelEvent.BucketLike -> {
                    viewModel.saveBucketLike()
                }

                is OpenBucketDetailInternalEvent.ViewModelEvent.HideComment -> {
                    viewModel.hideComment(event.commentId)
                }

                OpenBucketDetailInternalEvent.ViewModelEvent.DeleteBucket -> {
                    viewModel.deleteMyBucket()
                }
            }

            internalEvent = OpenBucketDetailInternalEvent.None
        }
    }

    detailInfo?.let { info ->
        InternalOpenDetailScreen(
            detailInfo = info,
            internalEvent = internalEvent,
            validMentionList = validMentionList.orEmpty(),
            goToOutEvent = goToEvent,
            updateInternalEvent = { event ->
                internalEvent = event
            },
            backPress = {
                keyboardController?.hide()
                backPress()
            }
        )
    }

    if (showLoading.value) {
        WaverLoading()
    }
}

@SuppressLint("UseOfNonLambdaOffsetOverload")
@Composable
private fun InternalOpenDetailScreen(
    detailInfo: BucketDetailUiInfo,
    validMentionList: List<CommentMentionInfo>,
    internalEvent: OpenBucketDetailInternalEvent,
    goToOutEvent: (OpenBucketDetailEvent2) -> Unit,
    updateInternalEvent: (OpenBucketDetailInternalEvent) -> Unit,
    backPress: () -> Unit,
) {

    val context = LocalContext.current
    val keyboardStatus by keyboardAsState()

    val listScrollState = rememberLazyListState()
    val titleIndex = if (detailInfo.imageInfo == null) 1 else 2

    val successButtonVisible by remember {
        derivedStateOf {
            val layoutInfo = listScrollState.layoutInfo
            if (layoutInfo.visibleItemsInfo.isEmpty()) {
                // 첫 레이아웃 전에는 모든 아이템 정보가 비어있어 "고정 버튼이 안 보인다"로 계산되고,
                // 그 결과 진입 시점에 플로팅 버튼이 잠깐 떴다가 사라진다.
                // 레이아웃 완료 전에는 고정 버튼이 보이는 것으로 간주해서 플로팅 버튼이 뜨지 않도록 한다.
                true
            } else {
                layoutInfo.visibleItemsInfo
                    .find { it.key == "successButton" }
                    .isSuccessButtonVisible(layoutInfo.viewportSize.height)
            }
        }
    }

    // 메모가 화면에 완전히 보이는지 확인
    val isMemoFullyVisible by remember {
        derivedStateOf {
            val memoItem = listScrollState.layoutInfo.visibleItemsInfo.find {
                it.key == "memoView"
            }

            if (memoItem == null) {
                false // 메모가 화면에 보이지 않음
            } else {
                // 메모의 상단이 화면 상단 이상에 있고 (offset >= 0)
                // 하단이 화면 하단 이내에 있으면 (offset + size <= viewportHeight) 완전히 보임
                val topIsVisible = memoItem.offset >= 0
                val bottomIsVisible =
                    (memoItem.offset + memoItem.size) <= listScrollState.layoutInfo.viewportSize.height
                topIsVisible && bottomIsVisible
            }
        }
    }

    // 댓글 라인이 화면에 보이는지 확인
    val isCommentLineVisible by remember {
        derivedStateOf {
            listScrollState.layoutInfo.visibleItemsInfo.any {
                it.key == "commentLine" || it.key == "commentLayer"
            }
        }
    }

    // 댓글 입력 화면 표시 조건:
    // 1. 메모가 완전히 보이면 (스크롤이 필요 없으면) → 항상 표시
    // 2. 메모가 완전히 보이지 않으면 (스크롤이 필요하면) → 댓글 라인이 보일 때만 표시
    val commentEditViewVisible by remember {
        derivedStateOf {
            if (isMemoFullyVisible) {
                true // 메모가 다 보임 → 항상 표시
            } else {
                isCommentLineVisible // 메모가 안 보임 (스크롤 필요) → 댓글 라인이 보일 때만 표시
            }
        }
    }

    LaunchedEffect(keyboardStatus) {
        if (keyboardStatus == Keyboard.Opened) {
            val lastIndex = listScrollState.layoutInfo.totalItemsCount - 1
            listScrollState.animateScrollToItem(lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .statusBarsPadding()
            .imePadding()
            .navigationBarsPadding()
    ) {
        DetailTopAppBar(
            listState = listScrollState,
            titlePosition = titleIndex,
            title = detailInfo.descInfo.title,
            clickEvent = {
                when (it) {
                    DetailAppBarClickEvent.CloseClicked -> {
                        backPress()
                    }

                    DetailAppBarClickEvent.MoreOptionClicked -> {
                        updateInternalEvent(OpenBucketDetailInternalEvent.MoreDialog(detailInfo.isMine))
                    }
                }
            })

        ConstraintLayout(
            modifier = Modifier.fillMaxSize()
        ) {
            val (contentView, floatingButtonView, editView) = createRefs()

            OpenDetailContentView(
                listState = listScrollState,
                info = detailInfo,
                clickEvent = {
                    when (it) {
                        is CommentLongClicked -> {
                            updateInternalEvent(OpenBucketDetailInternalEvent.CommentOption(it.commentIndex))
                        }

                        is DetailClickEvent.SuccessClicked -> {
                            updateInternalEvent(OpenBucketDetailInternalEvent.ViewModelEvent.Achieve)
                        }

                        is DetailClickEvent.GoToOtherProfile -> {
                            Log.e("ayhan", "GoToOtherProfile : ${it.id}")
                            goToOutEvent.invoke(OpenBucketDetailEvent2.GoToOtherProfile(it.id))
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(contentView) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        top.linkTo(parent.top)
                    })

            Column(
                modifier = Modifier.constrainAs(floatingButtonView) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                }
            ) {
                // 플로팅 버튼은 다음 조건에서만 노출:
                // 1. 자신의 버킷리스트이고 (!detailInfo.canShowCompleteButton == false)
                // 2. 고정 달성완료 버튼이 화면에 보이지 않을 때 (!successButtonVisible)
                // 3. 그리고 댓글 입력이 보이지 않을 때 (!commentEditViewVisible)
                AnimatedVisibility(
                    visible = detailInfo.canShowCompleteButton && !successButtonVisible && !commentEditViewVisible,
                    exit = fadeOut() + shrinkVertically(),
                    enter = slideInVertically(initialOffsetY = { it })
                ) {
                    if (keyboardStatus == Keyboard.Closed) {
                        DetailSuccessButtonView(
                            modifier = Modifier
                                .padding(bottom = 28.dp),
                            successClicked = {
                                updateInternalEvent(OpenBucketDetailInternalEvent.ViewModelEvent.Achieve)
                            },
                            successButtonInfo = SuccessButtonInfo(
                                goalCount = detailInfo.descInfo.goalCount,
                                userCount = detailInfo.descInfo.userCount
                            )
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .constrainAs(editView) {
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                    }) {
                AnimatedVisibility(
                    visible = commentEditViewVisible,
                    enter = slideInVertically(initialOffsetY = { it / 2 }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    CommentEditView(
                        onCommentChanged = {},
                        mentionableUsers = validMentionList,
                        sendCommend = {
                            updateInternalEvent(
                                OpenBucketDetailInternalEvent.ViewModelEvent.AddComment(
                                    AddBucketCommentRequest(
                                        bucketId = detailInfo.bucketId.toInt(),
                                        content = it,
                                        mentionIds = emptyList()
                                    )
                                )
                            )
                        },
                        isLike = detailInfo.isLike,
                        onLikeChanged = {
                            updateInternalEvent(OpenBucketDetailInternalEvent.ViewModelEvent.BucketLike)
                        }
                    )
                }
            }
        }
    }

    ShowOptionEvent(
        internalEvent = internalEvent,
        commentList = detailInfo.commentInfo?.commentList.orEmpty(),
        updateInternalEvent = updateInternalEvent,
    )

    if (internalEvent is OpenBucketDetailInternalEvent.BucketMore) {
        handleMoreEvent(
            context = context,
            info = detailInfo,
            moreEvent = internalEvent,
            goToEvent = goToOutEvent,
            updateInternalEvent = updateInternalEvent
        )
    }
}

private fun LazyListItemInfo?.isSuccessButtonVisible(viewportHeight: Int): Boolean {
    if (this == null) {
        return false
    }
    // 버튼의 상단이 뷰포트 하단보다 위에 있고 (버튼이 화면 위로 스크롤되지 않음)
    // 버튼의 하단이 뷰포트 상단보다 아래에 있으면 (버튼이 화면 아래로 스크롤되지 않음)
    val topIsVisible = this.offset < viewportHeight
    val bottomIsVisible = (this.offset + this.size) > 0
    return topIsVisible && bottomIsVisible
}

private fun handleMoreEvent(
    context: Context,
    info: BucketDetailUiInfo,
    moreEvent: OpenBucketDetailInternalEvent.BucketMore,
    goToEvent: (OpenBucketDetailEvent2) -> Unit,
    updateInternalEvent: (OpenBucketDetailInternalEvent) -> Unit
) {
    when (moreEvent) {
        is OpenBucketDetailInternalEvent.BucketMore.My -> {
            when (moreEvent.event) {
                is MyBucketMoreMenuEvent.GoToEdit -> {
                    goToEvent.invoke(
                        OpenBucketDetailEvent2.Update(
                            info.toUpdateUiModel(
                                createImageInfoWithPath(
                                    context, info.imageInfo?.imageList.orEmpty()
                                )
                            )
                        )
                    )
                }

                is MyBucketMoreMenuEvent.GoToGoalUpdate -> {
                    updateInternalEvent(
                        OpenBucketDetailInternalEvent.ShowUpdateGoalCountDialog(
                            info.descInfo.goalCount
                        )
                    )
                }

                is MyBucketMoreMenuEvent.GoToDelete -> {
                    updateInternalEvent(
                        OpenBucketDetailInternalEvent.ViewModelEvent.DeleteBucket
                    )
                }
            }
        }

        is OpenBucketDetailInternalEvent.BucketMore.Other -> {
            when (moreEvent.event) {
                is OtherBucketMenuEvent.GoToBlock -> {

                }

                is OtherBucketMenuEvent.GoToReport -> {
                    val reportInfo = ReportInfo(
                        id = info.bucketId,
                        writer = info.writerProfileInfo.nickName,
                        contents = info.descInfo.title,
                        reportType = ReportType.BUCKET
                    )
                    updateInternalEvent(OpenBucketDetailInternalEvent.ReportBucket(reportInfo))
                }
            }
        }
    }
}

@Composable
private fun ShowOptionEvent(
    commentList: List<Comment>,
    internalEvent: OpenBucketDetailInternalEvent,
    updateInternalEvent: (OpenBucketDetailInternalEvent) -> Unit
) {
    when (internalEvent) {
        is OpenBucketDetailInternalEvent.ReportBucket -> {
            ShowReportScreen(
                reportInfo = internalEvent.info,
                succeedReported = {
                    updateInternalEvent(
                        OpenBucketDetailInternalEvent.ViewModelEvent.UpdateBucket
                    )
                },
                closeEvent = {
                    updateInternalEvent(OpenBucketDetailInternalEvent.None)
                })
        }

        is OpenBucketDetailInternalEvent.ReportComment -> {
            ShowReportScreen(
                reportInfo = internalEvent.info,
                succeedReported = {
                    updateInternalEvent(
                        OpenBucketDetailInternalEvent.ViewModelEvent.UpdateBucket
                    )
                },
                closeEvent = {
                    updateInternalEvent(OpenBucketDetailInternalEvent.None)
                })
        }

        is OpenBucketDetailInternalEvent.ShowUpdateGoalCountDialog -> {
            GoalCountUpdateDialog(
                currentCount = internalEvent.count.toString()
            ) {
                when (it) {
                    GoalCountUpdateEvent.Close -> {
                        updateInternalEvent(OpenBucketDetailInternalEvent.None)
                    }

                    is GoalCountUpdateEvent.CountUpdate -> {
                        updateInternalEvent(
                            OpenBucketDetailInternalEvent.ViewModelEvent.UpdateGoalCount(
                                it.count
                            )
                        )
                    }
                }
            }
        }

        OpenBucketDetailInternalEvent.CommentTagListDialog -> TODO()

        is OpenBucketDetailInternalEvent.CommentOption -> {
            val commenter = commentList[internalEvent.index]

            if (commenter.isMine) {
                MyCommentSelectedDialog(
                    comment = commenter,
                    onDismissRequest = {
                        updateInternalEvent(OpenBucketDetailInternalEvent.None)
                    },
                    commentOptionClicked = {
                        when (it) {
                            is MyCommentOptionClicked.Delete -> {
                                updateInternalEvent(
                                    OpenBucketDetailInternalEvent.ViewModelEvent.DeleteMyComment(
                                        it.commentId
                                    )
                                )
                            }
                        }
                    }
                )
            } else {
                OtherCommentSelectedDialog(
                    comment = commenter,
                    onDismissRequest = {
                        updateInternalEvent(OpenBucketDetailInternalEvent.None)
                    },
                    commentOptionClicked = {
                        when (it) {
                            is OtherCommentOptionClicked.Hide -> {
                                updateInternalEvent(
                                    OpenBucketDetailInternalEvent.ViewModelEvent.HideComment(
                                        commentId = it.commentId
                                    )
                                )
                            }

                            is OtherCommentOptionClicked.Report -> {
                                val reportInfo = ReportInfo(
                                    id = commenter.commentId,
                                    writer = commenter.nickName,
                                    contents = commenter.comment,
                                    reportType = ReportType.COMMENT
                                )
                                updateInternalEvent(
                                    OpenBucketDetailInternalEvent.ReportComment(
                                        reportInfo
                                    )
                                )
                            }
                        }
                    }
                )
            }
        }

        is OpenBucketDetailInternalEvent.MoreDialog -> {
            if (internalEvent.isMine) {
                MyDetailAppBarMoreMenuDialog(dismiss = {
                    updateInternalEvent(OpenBucketDetailInternalEvent.None)
                }, event = { event ->
                    updateInternalEvent(event)
                })

            } else {
                OtherDetailAppBarMoreMenuDialog(dismiss = {
                    updateInternalEvent(OpenBucketDetailInternalEvent.None)
                }, event = { event ->
                    updateInternalEvent(event)
                })
            }
        }

        is OpenBucketDetailInternalEvent.Error -> {
            ShowErrorDialog(
                internalEvent.status,
                achieve = {},
                backPress = {},
                updateInternalEvent = updateInternalEvent
            )
        }

        else -> {
            // Do Nothing
        }
    }
}


@Composable
private fun ShowErrorDialog(
    event: DetailLoadFailStatus,
    achieve: () -> Unit,
    backPress: () -> Unit,
    updateInternalEvent: (OpenBucketDetailInternalEvent) -> Unit
) {
    val title = when (event) {
        DetailLoadFailStatus.AchieveFail -> stringResource(id = R.string.achieveBucketFail)
        DetailLoadFailStatus.LoadFail -> stringResource(id = R.string.loadBucketFail)
        else -> stringResource(id = R.string.apiFailTitle)
    }
    ApiFailDialog(
        title = title,
        message = stringResource(id = R.string.apiFailMessage)
    ) {
        when (event) {
            DetailLoadFailStatus.AchieveFail -> {
                achieve()
            }

            DetailLoadFailStatus.LoadFail -> {
                backPress()
            }

            else -> {
                // Do Nothing
            }
        }
        updateInternalEvent(OpenBucketDetailInternalEvent.None)
    }
}

@Preview(showBackground = true)
@Composable
private fun InternalOpenDetailScreenPreview() {

    InternalOpenDetailScreen(
        detailInfo = BucketDetailUiInfo(
            bucketId = "1",
            writeOpenType = WriteOpenType.PUBLIC,
            imageInfo = null,
            writerProfileInfo = DetailDescType.WriterProfileInfoUi(
                profileImage = null,
                badgeImage = "",
                badgeTitle = "안녕나는한아야",
                nickName = "김한아",
                userId = "1"
            ),
            descInfo = DetailDescType.CommonDetailDescInfo(
                dDay = null,
                status = BucketStatus.PROGRESS,
                keywordList = listOf(),
                title = "타이틀!",
                goalCount = 10,
                userCount = 0,
                categoryInfo = WriteCategoryInfo(
                    id = 0,
                    name = "카테고리",
                    defaultYn = YesOrNo.N
                ),
                isScrap = false,
                isMine = true
            ),
            memoInfo = DetailDescType.MemoInfo("메모를 썼다고 해보자구\n메모를 썼다고 해보자구\n메모를 썼다고 해보자구\n메모를 썼다고 해보자구\n메모를 썼다고 해보자구\n메모를 썼다고 해보자구\n메모를 썼다고 해보자구\n메모를 썼다고 해보자구\n메모를 썼다고 해보자구\n메모를 썼다고 해보자구\n메모를 썼다고 해보자구\n메모를 썼다고 해보자구\n메모를 썼다고 해보자구\n메모를 썼다고 해보자구\n메모를 썼다고 해보자구\n메모를 썼다고 해보자구\n메모를 썼다고 해보자구\n메모를 썼다고 해보자구\n메모를 썼다고 해보자구\n메모를 썼다고 해보자구\n메모를 썼다고 해보자구\n메모를 썼다고 해보자구\n메모를 썼다고 해보자구\n메모를 썼다고 해보자구\n메모를 썼다고 해보자구\n메모를 썼다고 해보자구\n메모를 썼다고 해보자구\n메모를 썼다고 해보자구\n메모를 썼다고 해보자구\n메모를 썼다고 해보자구\n메모를 썼다고 해보자구\n메모를 썼다고 해보자구\n메모를 썼다고 해보자구\n메모를 썼다고 해보자구\n메모를 썼다고 해보자구\n메모를 썼다고 해보자구\n메모를 썼다고 해보자구\n메모를 썼다고 해보자구\n메모를 썼다고 해보자구\n메모를 썼다고 해보자구\n아주긴ㄱㄹ로\n일단 스크롤이 되어야 하거든\n부탁 좀 핳게 !!!"),
            commentInfo = DetailDescType.CommentInfo(
                commentCount = 4402, commentList = listOf(
                    Comment(
                        commentId = "torquent",
                        userId = "11",
                        profileImage = "11111",
                        nickName = "Parker Montoya",
                        comment = "lorem",
                        isMine = false
                    )
                )

            ),
            togetherInfo = null,
            isMine = true,
            isDone = false,
            isLike = false
        ),
        validMentionList = emptyList(),
        internalEvent = OpenBucketDetailInternalEvent.None,
        goToOutEvent = {},
        updateInternalEvent = {},
        backPress = {}
    )
}
