package com.zinc.waver.ui_detail.component

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.zinc.waver.model.CommentMentionInfo
import com.zinc.waver.ui.design.theme.Gray1
import com.zinc.waver.ui.design.theme.Gray11
import com.zinc.waver.ui.design.theme.Gray3
import com.zinc.waver.ui.design.theme.Gray6
import com.zinc.waver.ui.design.theme.Gray9
import com.zinc.waver.ui.design.theme.Main5
import com.zinc.waver.ui.presentation.component.MyText
import com.zinc.waver.ui.presentation.component.MyTextField
import com.zinc.waver.ui.util.dpToSp
import com.zinc.waver.ui_detail.R
import com.zinc.waver.util.shadow

@Composable
fun CommentEditView(
    modifier: Modifier = Modifier,
    onCommentChanged: (String) -> Unit,
    sendCommend: (String) -> Unit,
    mentionableUsers: List<CommentMentionInfo> = emptyList(),
    isLike: Boolean,
    onLikeChanged: (Boolean) -> Unit,
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    var showMentionPopup by remember { mutableStateOf(false) }
    var isFocused by remember { mutableStateOf(false) }
    val mentionableUsersNameList = mentionableUsers.map { it.nickName }

    fun getMentionEndPosition(text: String, mentionStart: Int): Int {
        return text.indexOf(" ", mentionStart).let {
            if (it == -1) text.length else it
        }
    }

    // 현재 커서가 멘션 영역 안에 있는지 확인
    fun isInMentionRange(text: String, cursorPosition: Int): Boolean {
        var startIndex = 0
        while (startIndex < text.length) {
            val atIndex = text.indexOf("@", startIndex)
            if (atIndex == -1) break

            val endIndex = getMentionEndPosition(text, atIndex)
            val atText = text.substring(atIndex + 1, endIndex)
            val isUserText = mentionableUsersNameList.contains(atText)

            if (cursorPosition > atIndex && cursorPosition <= endIndex && isUserText) {
                return true
            }
            startIndex = endIndex + 1
        }
        return false
    }

    // 멘션을 완전히 삭제하고 새로운 텍스트를 반환
    fun deleteMention(text: String, cursorPosition: Int): String {
        var startIndex = 0
        while (startIndex < text.length) {
            val atIndex = text.indexOf("@", startIndex)
            if (atIndex == -1) break

            val endIndex = getMentionEndPosition(text, atIndex)
            if (cursorPosition > atIndex && cursorPosition <= endIndex) {
                val beforeMention = if (atIndex > 0) text.substring(0, atIndex) else ""
                val afterMention = if (endIndex < text.length) text.substring(endIndex) else ""
                return beforeMention + afterMention.trimStart()
            }
            startIndex = endIndex + 1
        }
        return text
    }

    // 멘션된 텍스트를 파란색으로 강조하는 함수
    fun createAnnotatedString(text: String): AnnotatedString {
        return buildAnnotatedString {
            val mentionedList = mutableListOf<TextRange>()

            var startIndex = 0
            while (startIndex < text.length) {
                val mentionStart = text.indexOf("@", startIndex)
                val mentionEnd = getMentionEndPosition(text, mentionStart)

                // @의 시작과 끝의 텍스트 확인
                val atText = text.substring(mentionStart + 1, mentionEnd)
                val isUserText = mentionableUsersNameList.contains(atText)

                if (mentionStart == -1) {
                    break
                }

                startIndex = mentionEnd

                if (isUserText) {
                    mentionedList.add(TextRange(mentionStart, mentionEnd))
                }
            }

            append(text)

            mentionedList.forEach {
                addStyle(
                    style = SpanStyle(color = Main5),
                    start = it.start,
                    end = it.end
                )
            }
        }
    }

    fun findNearesAtPreviousCusortPosition(textFieldValue: TextFieldValue): Int {
        val text = textFieldValue.text
        val cursorPosition = textFieldValue.selection.start

        if (cursorPosition == 0 || text.isEmpty()) {
            return -1
        }

        // 커서 위치 바로 전부터 문자열의 시작까지 역방향으로 탐색
        for (i in (cursorPosition - 1) downTo 0) {
            if (text[i] == '@') {
                return i // 가장 가까운 '@'의 인덱스 반환
            }
        }

        return -1 // 커서 앞에 '@'가 없는 경우
    }


    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Bottom
        ) {
            // 멘션 팝업
            if (showMentionPopup) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .padding(bottom = 8.dp)
                        .heightIn(max = 198.dp)
                        .shadow(
                            color = Gray11.copy(alpha = 0.2f),
                            offsetX = (0).dp,
                            offsetY = (0).dp,
                            blurRadius = 8.dp,
                        )
                        .background(color = Gray1, shape = RoundedCornerShape(8.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .fillMaxWidth()
                            .padding(top = 3.dp)
                    ) {
                        val startIndex = findNearesAtPreviousCusortPosition(textFieldValue)
                        val endIndex = getMentionEndPosition(textFieldValue.text, startIndex)
                        val searchText =
                            textFieldValue.text.substring(startIndex + 1, endIndex).lowercase()

                        Log.e(
                            "ayhan ",
                            "searchText : $searchText, startIndex : $startIndex, endIndex : $endIndex"
                        )

                        val filteredUsers = if (searchText.isEmpty()) {
                            mentionableUsers
                        } else {
                            mentionableUsers.filter { it.nickName.lowercase().contains(searchText) }
                        }

                        filteredUsers.forEach { user ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val needChangeIndex =
                                            findNearesAtPreviousCusortPosition(textFieldValue)
                                        if (needChangeIndex == -1) return@clickable

                                        val newText = "@${user.nickName} "
                                        val changeText = textFieldValue.text.replaceRange(
                                            needChangeIndex,
                                            needChangeIndex + 1,
                                            newText
                                        )
                                        Log.e("ayhan", "changeText :$changeText")

                                        val newCursorPosition = needChangeIndex + newText.length

                                        textFieldValue = TextFieldValue(
                                            annotatedString = createAnnotatedString(changeText),
                                            selection = TextRange(newCursorPosition)
                                        )
                                        showMentionPopup = false
                                        onCommentChanged(newText)
                                    }
                                    .padding(vertical = 8.dp, horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = user.profileImage,
                                    error = painterResource(com.zinc.waver.ui_common.R.drawable.profile_icon_blank),
                                    placeholder = painterResource(com.zinc.waver.ui_common.R.drawable.profile_icon_blank),
                                    // 프로필 이미지를 등록하지 않은 유저(model == null)
                                    fallback = painterResource(com.zinc.waver.ui_common.R.drawable.profile_icon_blank),
                                    contentDescription = "Profile image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                                MyText(
                                    user.nickName,
                                    color = Gray9,
                                    fontSize = dpToSp(13.dp)
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Gray3)
                    .padding(top = 8.dp, start = 12.dp, end = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(
                        id = if (isLike) com.zinc.waver.ui_common.R.drawable.btn_32_like_on
                        else com.zinc.waver.ui_common.R.drawable.btn_32_like_off
                    ),
                    contentDescription = "Like button",
                    modifier = Modifier
                        .size(32.dp)
                        .clickable {
                            onLikeChanged(!isLike)
                        }
                )

                Box(
                    modifier = Modifier
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(max = 78.dp, min = 36.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            MyTextField(
                                value = textFieldValue,
                                onValueChange = { newValue ->
                                    val cursorPosition = newValue.selection.start
                                    val oldCursorPosition = textFieldValue.selection.start
                                    val atIndex = findNearesAtPreviousCusortPosition(newValue)

                                    // 현재 입력이 삭제인지 확인
                                    val isDeleting =
                                        newValue.text.length < textFieldValue.text.length

                                    // 커서가 멘션 영역 안에 있는지 확인
                                    if (isInMentionRange(
                                            textFieldValue.text,
                                            oldCursorPosition
                                        ) && isDeleting
                                    ) {
                                        // 삭제 시도한 경우 멘션 전체 삭제
                                        val newText =
                                            deleteMention(textFieldValue.text, oldCursorPosition)
                                        textFieldValue = TextFieldValue(
                                            text = newText,
                                            selection = TextRange(newText.length) // TODO : 이거 원래는 삭제된 위치로 보내야함... 하...
                                        )
                                        onCommentChanged(newText)
                                        showMentionPopup = false
                                    } else {
                                        // 일반 텍스트 입력 (멘션 영역 밖이거나 새로운 입력)
                                        textFieldValue = newValue.copy()
                                        Log.e("ayhan", "newValue : ${newValue.text}")

                                        onCommentChanged(newValue.text)

                                        // 입력이 완료된 후 AnnotatedString 적용
                                        if (newValue.composition != null) {
                                            // 조합 중인 텍스트가 있으면 하이라이팅하지 않음
                                            // TODO : 이러면 입력 중에 하이라이팅이 아예 안먹는 문제가 있음 어케 해야되누...
                                            return@MyTextField
                                        }
                                        textFieldValue = textFieldValue.copy(
                                            annotatedString = createAnnotatedString(newValue.text)
                                        )

                                        // @ 입력 감지 및 팝업 표시
                                        val mentionText =
                                            if (atIndex != -1 && cursorPosition > atIndex) {
                                                val textAfterAt =
                                                    newValue.text.substring(atIndex + 1)
                                                if (textAfterAt.contains(" ")) {
                                                    // @ 뒤에 공백이 있으면 멘션이 완성된 것으로 간주
                                                    if (cursorPosition <= atIndex + textAfterAt.indexOf(
                                                            " "
                                                        )
                                                    ) {
                                                        textAfterAt.substring(
                                                            0,
                                                            textAfterAt.indexOf(" ")
                                                        ).trim()
                                                    } else {
                                                        ""
                                                    }
                                                } else {
                                                    textAfterAt.trim()
                                                }
                                            } else ""

                                        Log.e(
                                            "ayhan",
                                            "mentionText : $mentionText, atIndex : $atIndex, cursorPosition : $cursorPosition"
                                        )

                                        // @ 다음에 실제 입력된 텍스트가 있고, 공백 이전일 때만 팝업 표시
                                        showMentionPopup =
                                            mentionableUsers.isNotEmpty() && (mentionText.isNotEmpty() || (atIndex != -1 && cursorPosition == atIndex + 1))
                                    }
                                },
                                modifier = Modifier
                                    .padding(start = 10.dp, end = 10.dp)
                                    .onFocusChanged { isFocused = it.isFocused },
                                textStyle = TextStyle(
                                    color = Gray9,
                                    fontSize = dpToSp(13.dp),
                                    fontWeight = FontWeight.Medium
                                ),
                                keyboardOptions = KeyboardOptions(
                                    autoCorrect = false
                                ),
                                decorationBox = { innerTextField ->
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        if (textFieldValue.text.isEmpty()) {
                                            MyText(
                                                text = "버킷리스트를 응원해주세요!",
                                                color = Gray6,
                                                fontSize = dpToSp(13.dp),
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                        }

                        if (isFocused) {
                            Image(
                                painter = painterResource(
                                    id = if (textFieldValue.text.isEmpty()) {
                                        R.drawable.comment_send_off
                                    } else {
                                        R.drawable.comment_send_on
                                    }
                                ),
                                contentDescription = "Send comment",
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .size(28.dp)
                                    .clickable {
                                        if (textFieldValue.text.isNotEmpty()) {
                                            sendCommend(textFieldValue.text)
                                            textFieldValue = TextFieldValue("")
                                        }
                                    },
                            )
                        } else {
                            Spacer(modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CommentEditViewPreview() {
    val previewUsers = listOf(
        CommentMentionInfo("1", "", "김철수", true, false),
        CommentMentionInfo("2", "", "이영희", true, false),
        CommentMentionInfo("3", "", "박지민", true, false),
        CommentMentionInfo("4", "", "강동원", true, false)
    )

    var previewComment by remember { mutableStateOf("") }

    Box(modifier = Modifier) {
        CommentEditView(
            onCommentChanged = { previewComment = it },
            mentionableUsers = previewUsers,
            isLike = false,
            onLikeChanged = {},
            sendCommend = {}
        )
    }
}
