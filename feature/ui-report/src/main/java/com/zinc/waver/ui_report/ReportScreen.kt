package com.zinc.waver.ui_report

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.zinc.common.models.ReportItem
import com.zinc.common.models.ReportItems
import com.zinc.waver.model.ReportClickEvent
import com.zinc.waver.model.ReportInfo
import com.zinc.waver.model.ReportType
import com.zinc.waver.ui.design.theme.Gray1
import com.zinc.waver.ui.design.util.Keyboard
import com.zinc.waver.ui.design.util.keyboardAsState
import com.zinc.waver.ui.presentation.component.TitleIconType
import com.zinc.waver.ui.presentation.component.TitleView

@Composable
fun ReportScreen(
    reportInfo: ReportInfo, backPress: () -> Unit, succeedReported: () -> Unit
) {
    val viewModel: ReportViewModel = hiltViewModel()

    // 컴포지션 본문에서 호출하면 리컴포지션마다 다시 요청한다.
    LaunchedEffect(Unit) {
        viewModel.loadReportItems()
    }

    val reportItems by viewModel.reportItemList.observeAsState()
    val commentReportSucceedAsState by viewModel.commentReportSucceed.observeAsState()
    val commentReportFailAsSate by viewModel.requestFail.observeAsState()

    var commentReportSucceed by remember { mutableStateOf(commentReportSucceedAsState) }
    var commentReportFail by remember { mutableStateOf(commentReportFailAsSate) }

    LaunchedEffect(key1 = commentReportSucceedAsState) {
        if (commentReportSucceedAsState == true) {
            commentReportSucceed = true
        }
    }

    LaunchedEffect(key1 = commentReportFailAsSate) {
        if (commentReportFailAsSate == true) {
            commentReportFail = true
        }
    }

    val context = LocalContext.current

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray1)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        val (topAppBar, content, bottomButton) = createRefs()

        val scrollState = rememberLazyListState()
        val keyboardController = LocalSoftwareKeyboardController.current
        val isKeyboardStatus by keyboardAsState()
        var etcText by remember { mutableStateOf("") }
        val selectedItem: MutableState<ReportItem?> = remember {
            mutableStateOf(null)
        }

        val isScrolled by remember {
            derivedStateOf {
                scrollState.firstVisibleItemIndex != 0
            }
        }

        val title =
            if (reportInfo.reportType == ReportType.COMMENT) R.string.commentReportTitle
            else R.string.bucketReportTitle

        TitleView(modifier = Modifier
            .fillMaxWidth()
            .constrainAs(topAppBar) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            },
            title = stringResource(id = title),
            leftIconType = TitleIconType.BACK,
            isDividerVisible = isScrolled,
            onLeftIconClicked = {
                backPress()
            })

        ReportContentView(
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(content) {
                    top.linkTo(topAppBar.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                    height = Dimension.fillToConstraints
                }
                .padding(bottom = 70.dp),
            scrollState = scrollState,
            reportInfo = reportInfo,
            reportItems = reportItems ?: ReportItems(items = emptyList()),
            keyboardController = keyboardController,
            onImeAction = {
                etcText = it
            },
            clickEvent = {
                when (it) {
                    is ReportClickEvent.ReportItemSelected -> {
                        selectedItem.value = it.reportItem
                    }
                }

            }
        )

        if (isKeyboardStatus == Keyboard.Closed) {
            ReportButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(bottomButton) {
                        start.linkTo(parent.start)
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end)
                    }
                    .padding(bottom = 28.dp),
                reportButtonClicked = {
                    val reason = etcText.ifEmpty { selectedItem.value?.text.orEmpty() }
                    if (reportInfo.reportType == ReportType.BUCKET) {
                        viewModel.requestReportBucket(reportInfo.id, reason)
                    } else {
                        viewModel.requestReportComment(reportInfo.id, reason)
                    }
                }
            )
        }

        if (commentReportSucceed == true) {
            Toast.makeText(context, R.string.commentReportSucceed, Toast.LENGTH_SHORT).show()
            commentReportSucceed = false
            succeedReported()
        }

        if (commentReportFail == true) {
            Toast.makeText(context, R.string.commentReportFail, Toast.LENGTH_SHORT).show()
            commentReportFail = false
        }
    }
}