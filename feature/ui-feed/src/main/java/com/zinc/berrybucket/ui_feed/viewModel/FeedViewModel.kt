package com.zinc.berrybucket.ui_feed.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zinc.berrybucket.CommonViewModel
import com.zinc.berrybucket.ui_feed.models.UIFeedInfo
import com.zinc.berrybucket.ui_feed.models.UIFeedKeyword
import com.zinc.berrybucket.ui_feed.models.parseUI
import com.zinc.berrybucket.ui_feed.models.toUIModel
import com.zinc.berrybucket.util.SingleLiveEvent
import com.zinc.datastore.login.LoginPreferenceDataStoreModule
import com.zinc.domain.usecases.feed.LoadFeedItems
import com.zinc.domain.usecases.feed.LoadFeedKeyWords
import com.zinc.domain.usecases.feed.SavedKeywordItems
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val loadFeedKeyWords: LoadFeedKeyWords,
    private val loadFeedItems: LoadFeedItems,
    private val savedKeywordItems: SavedKeywordItems,
    loginPreferenceDataStoreModule: LoginPreferenceDataStoreModule
) : CommonViewModel(loginPreferenceDataStoreModule) {
    private val _isKeyWordSelected = MutableLiveData<Boolean>()
    val isKeyWordSelected: LiveData<Boolean> get() = _isKeyWordSelected

    private val _feedKeyWords = MutableLiveData<List<UIFeedKeyword>>()
    val feedKeyWords: LiveData<List<UIFeedKeyword>> get() = _feedKeyWords

    private val _feedItems = MutableLiveData<List<UIFeedInfo>>()
    val feedItems: LiveData<List<UIFeedInfo>> get() = _feedItems

    private val _loadFail = SingleLiveEvent<Boolean>()
    val loadFail: LiveData<Boolean> get() = _loadFail

    private val _savedKeywordSucceed = MutableLiveData<Boolean>()
    val savedKeywordSucceed: LiveData<Boolean> get() = _savedKeywordSucceed

    fun loadKeyWordSelected() {
        _isKeyWordSelected.value = false
    }

    fun loadFeedKeyWords() {
        viewModelScope.launch(CoroutineExceptionHandler { coroutineContext, throwable ->
            _loadFail.value = true
        }) {
            runCatching {
                loadFeedKeyWords.invoke().apply {
                    Log.e("ayhan", "feed response : $this")
                    if (success) {
                        _feedKeyWords.value = data.parseUI()
                        _loadFail.value = false
                    } else {
                        _loadFail.value = true
                    }
                }
            }.getOrElse {
                _loadFail.value = true
            }
        }
    }

    fun loadFeedItems() {
        viewModelScope.launch(CEH(_loadFail, true)) {
            kotlin.runCatching {
                accessToken.value?.let { token ->
                    loadFeedItems.invoke(token).apply {
                        Log.e("ayhan", "feedListResponse : $this")
                        if (this.success.not()) {
                            _loadFail.value = true
                        } else if (this.code == "5000") {
                            _isKeyWordSelected.value = false
                        } else {
                            _feedItems.value = this.data.toUIModel()
                        }
                    }
                }

            }.getOrElse {
                _loadFail.value = true
            }

        }
    }

    fun setKeyWordSelected() {
        _isKeyWordSelected.value = true
    }

    fun savedKeywordList(list: List<String>) {
        viewModelScope.launch(CEH(_savedKeywordSucceed, false)) {
            accessToken.value?.let { token ->
                runCatching {
                    val response = savedKeywordItems.invoke(token, list)
                    _savedKeywordSucceed.value = response.success
                }.getOrElse {
                    _savedKeywordSucceed.value = false
                }
            }
        }
    }
}
