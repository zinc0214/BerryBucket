package com.zinc.waver.ui_write.presentation.options

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.zinc.waver.model.WriteFriend
import com.zinc.waver.ui.presentation.component.MyText
import com.zinc.waver.ui.presentation.component.SearchEditView
import com.zinc.waver.ui_common.R
import com.zinc.waver.ui_write.presentation.AddedFriendItem
import com.zinc.waver.ui_write.presentation.ShowAllFriendItem
import com.zinc.waver.ui_write.presentation.WriteAppBar
import com.zinc.waver.ui_write.presentation.WriteAppBarClickEvent
import com.zinc.waver.ui_write.presentation.WriteSelectFriendItem
import com.zinc.waver.ui_write.viewmodel.WriteBucketListViewModel

@Composable
fun WriteSelectFriendsScreen(
    closeClicked: () -> Unit,
    originFriends: List<WriteFriend>,
    selectedFriends: List<WriteFriend>,
    addFriendsClicked: (List<WriteFriend>) -> Unit
) {
    val viewModel: WriteBucketListViewModel = hiltViewModel()
    val searchFriendsResult by viewModel.searchFriendsResult.observeAsState()

    // 최종 친구 목록
    var updateFriends by remember { mutableStateOf(selectedFriends) }

    // 선택된 친구가 5명이 넘는 경우 일부만 노출
    var isFriendsExpanded by remember { mutableStateOf(selectedFriends.size <= 5) }
    val needShowAllFriendButton = !isFriendsExpanded && updateFriends.size > 5

    val scrollState = rememberLazyListState()
    val searchWord = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxSize()
    ) {
        WriteAppBar(
            modifier = Modifier.fillMaxWidth(),
            nextButtonClickable = true,
            rightText = R.string.saveDesc,
            clickEvent = {
                when (it) {
                    WriteAppBarClickEvent.CloseClicked -> {
                        closeClicked()
                    }

                    WriteAppBarClickEvent.NextClicked -> {
                        addFriendsClicked(updateFriends)
                    }
                }
            },
            isShowDivider = false
        )

        if (originFriends.isEmpty()) {
            MyText(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 100.dp),
                text = "아무도 없어요! 친구를 추가해주세요.",
                textAlign = TextAlign.Center
            )
        } else {
            SearchEditView(
                goToSearch = {
                    viewModel.searchFriends(it)
                    searchWord.value = it
                },
                currentSearchWord = searchWord.value
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                state = scrollState,
                contentPadding = PaddingValues(bottom = 50.dp)
            ) {
                if (updateFriends.isNotEmpty()) {
                    item {
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 15.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val list =
                                if (needShowAllFriendButton) updateFriends.take(5) else updateFriends
                            list.forEach {
                                AddedFriendItem(
                                    writeFriend = it,
                                    deleteFriend = { friend ->
                                        updateFriends = updateFriends - friend
                                    })
                            }
                            if (needShowAllFriendButton) {
                                ShowAllFriendItem(clicked = {
                                    isFriendsExpanded = true
                                })
                            }
                        }
                    }
                }

                searchFriendsResult?.let { friends ->
                    item {
                        Spacer(modifier = Modifier.height(15.dp))
                    }

                    items(
                        items = friends,
                        itemContent = { friend ->
                            var selected = updateFriends.any { it == friend }
                            WriteSelectFriendItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                                    .clickable(enabled = !selected, onClick = {
                                        updateFriends = updateFriends + friend
                                        selected = !selected
                                    }),
                                writeFriend = friend,
                                isSelected = selected
                            )
                        })
                }
            }
        }

    }
}