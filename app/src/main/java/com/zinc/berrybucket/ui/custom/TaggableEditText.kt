package com.zinc.berrybucket.ui.custom

import android.content.Context
import android.os.Build
import android.text.Html
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.lifecycle.LifecycleOwner
import com.zinc.berrybucket.databinding.LayoutTaggableEdittextBinding
import com.zinc.berrybucket.model.CommentTagInfo
import com.zinc.berrybucket.ui.presentation.detail.DetailViewModel
import com.zinc.berrybucket.util.nonNullObserve
import com.zinc.berrybucket.util.onTextChanged
import com.zinc.berrybucket.util.setVisible

class TaggableEditText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var binding: LayoutTaggableEdittextBinding
    private var imm: InputMethodManager

    private lateinit var originCommentTaggableList: List<CommentTagInfo>
    private lateinit var updateValidTaggableList: (List<CommentTagInfo>) -> Unit
    private lateinit var commentSendButtonClicked: () -> Unit
    private lateinit var viewModel: DetailViewModel
    private lateinit var lifecycleOwner: LifecycleOwner


    init {
        binding = LayoutTaggableEdittextBinding.inflate(LayoutInflater.from(context), this, true)
        imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    fun setUpView(
        viewModel: DetailViewModel,
        lifecycleOwner: LifecycleOwner,
        originCommentTaggableList: List<CommentTagInfo>,
        updateValidTaggableList: ((List<CommentTagInfo>) -> Unit),
        commentSendButtonClicked: () -> Unit,
    ) {
        this.viewModel = viewModel
        this.lifecycleOwner = lifecycleOwner
        this.originCommentTaggableList = originCommentTaggableList
        this.updateValidTaggableList = updateValidTaggableList
        this.commentSendButtonClicked = commentSendButtonClicked

        Log.e("ayhan", "originCommentTaggableList : $originCommentTaggableList")
        setUpViews()
        setUpObservers()
    }

    private fun setUpObservers() {

        viewModel.commentEditString.nonNullObserve(lifecycleOwner) {
            changeEditText(it)
            val getLastIndex = binding.commentEditTextView.text.length
            binding.commentEditTextView.setSelection(getLastIndex)
        }

        viewModel.commentTagClicked.nonNullObserve(lifecycleOwner) {
            val editText = binding.commentEditTextView.text.toString()
            val currentBlock = getCurrentEditTextBlock(editText)
            viewModel.addCommentTaggedItem(it, currentBlock.startIndex, currentBlock.endIndex)
        }
    }

    private fun setUpViews() {

        binding.commentEditTextView.setOnFocusChangeListener { _, hasFocus ->
            binding.commentSendButton.setVisible(hasFocus)
            binding.commentEditTextView.maxLines = if (hasFocus) 3 else 1
        }

        binding.commentEditTextView.onTextChanged { text ->
            binding.commentSendButton.isEnabled = text.isNotBlank()

            val validTaggableList = getCurrentBlockTaggableList(text)
            updateValidTaggableList(validTaggableList)
            viewModel.updateCommentEditString(text)
        }

        binding.commentSendButton.setOnClickListener {
            commentSendButtonClicked()
            imm.hideSoftInputFromWindow(binding.commentEditTextView.windowToken, 0)
            binding.commentEditTextView.setText("")
            binding.commentEditTextView.clearFocus()
        }
    }

    // ?????? ????????? ????????? EditText ???????????? ?????? ????????? ????????? ??????
    private fun getCurrentBlockTaggableList(
        currentEditText: String
    ): List<CommentTagInfo> {
        val currentCursorBlock = getCurrentEditTextBlock(currentEditText).currentBlockText
        if (currentCursorBlock.isEmpty()) return emptyList()

        if (currentCursorBlock.isNotBlank() && currentCursorBlock.first() == '@') {
            val originString = currentCursorBlock.drop(1)
            return if (originString.isNotBlank()) {
                originCommentTaggableList.filter { commentInfo ->
                    commentInfo.nickName.contains(
                        originString
                    )
                }
            } else {
                originCommentTaggableList
            }
        }
        return emptyList()
    }

    // ?????? EditText ??? ????????? ?????? ????????? ??????
    private fun getCurrentEditTextBlock(currentEditText: String): CurrentBlock {
        val splitText = currentEditText.split(" ")
        var splitLength = 0
        var startIndex = 0
        val currentEditTextFocus = binding.commentEditTextView.selectionEnd
        splitText.forEach {
            splitLength += it.length + 1
            if (splitLength > currentEditTextFocus) {
                return CurrentBlock(
                    it,
                    startIndex,
                    startIndex + it.length
                )
            }
            startIndex += it.length + 1
        }
        return CurrentBlock(currentEditText, 0, 0)
    }

    private fun changeEditText(currentEditText: String) {
        val changedColorText = changeTaggedTextColor(currentEditText)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            binding.commentEditTextView.setText(
                Html.fromHtml(
                    changedColorText, Html.FROM_HTML_MODE_COMPACT
                )
            )
        } else {
            binding.commentEditTextView.setText(Html.fromHtml(changedColorText))
        }

        val getLastIndex = binding.commentEditTextView.text.length
        binding.commentEditTextView.setSelection(getLastIndex)
    }

    // ????????? ???????????? ?????? ???????????? ?????? ?????? ?????????
    private fun changeTaggedTextColor(currentEditText: String): String {
        val taggedList = getTaggedOrNotBlockList(currentEditText)
        var changedColorText = ""
        taggedList.forEach {
            changedColorText += if (it.first) {
                getColoredSpanned(it.second)
            } else {
                it.second
            }
            if (taggedList.last() != it) {
                changedColorText += " "
            }
        }
        return changedColorText
    }

    // ????????? ???????????? ?????? ???????????? ?????? ???????????? ??????
    private fun getTaggedOrNotBlockList(currentEditText: String): List<Pair<Boolean, String>> {
        val list = mutableListOf<Pair<Boolean, String>>()
        currentEditText.split(" ").forEach { text ->
            val isExistTagged = originCommentTaggableList.firstOrNull { "@${it.nickName}" == text }
            list.add(Pair(isExistTagged != null, text))
        }
        return list
    }


    private fun getColoredSpanned(text: String): String {
        return "<font color=#284f81>$text</font>"
    }

    data class CurrentBlock(
        val currentBlockText: String, val startIndex: Int, val endIndex: Int
    )

}