package com.zinc.waver.ui_search.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zinc.datastore.login.PreferenceDataStoreModule
import com.zinc.domain.usecases.common.CopyOtherBucket
import com.zinc.domain.usecases.other.RequestFollowUser
import com.zinc.domain.usecases.other.RequestUnfollowUser
import com.zinc.domain.usecases.search.DeleteRecentWord
import com.zinc.domain.usecases.search.LoadSearchPopularAndRecommend
import com.zinc.domain.usecases.search.LoadSearchRecommend
import com.zinc.domain.usecases.search.LoadSearchResult
import com.zinc.waver.ui.viewmodel.CommonViewModel
import com.zinc.waver.ui_search.model.RecommendItem
import com.zinc.waver.ui_search.model.RecommendList
import com.zinc.waver.ui_search.model.RecommendType
import com.zinc.waver.ui_search.model.SearchBucketItem
import com.zinc.waver.ui_search.model.SearchResultItems
import com.zinc.waver.ui_search.model.parseUI
import com.zinc.waver.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val loadSearchPopularAndRecommend: LoadSearchPopularAndRecommend,
    private val loadSearchResult: LoadSearchResult,
    private val loadSearchRecommend: LoadSearchRecommend,
    private val deleteRecentWord: DeleteRecentWord,
    private val requestFollow: RequestFollowUser,
    private val requestUnFollow: RequestUnfollowUser,
    private val copyOtherBucket: CopyOtherBucket,
    private val preferenceDataStoreModule: PreferenceDataStoreModule,
) : CommonViewModel() {

    private val _recommendList = MutableLiveData<RecommendList>()
    val recommendList: LiveData<RecommendList> get() = _recommendList

    private val _searchRecentlyItems = MutableLiveData<List<String>>()
    val searchRecentlyItems: LiveData<List<String>> get() = _searchRecentlyItems

    private val _searchResultItems = MutableLiveData<SearchResultItems?>()
    val searchResultItems: LiveData<SearchResultItems?> get() = _searchResultItems

    private val _loadFail = MutableLiveData<String?>(null)
    val loadFail: LiveData<String?> get() = _loadFail

    private val _copySucceed = MutableLiveData<Boolean>()
    val copySucceed: LiveData<Boolean> get() = _copySucceed

    private val _actionFail = SingleLiveEvent<Nothing>()
    val actionFail: LiveData<Nothing> get() = _actionFail

    var prevSearchWord: String = ""
    private var userId = ""

    init {
        viewModelScope.launch {
            preferenceDataStoreModule.loadUserIdKey.collectLatest { value ->
                userId = value
            }
        }
    }

    fun loadRecommendList() {
        viewModelScope.launch(ceh(_loadFail, "")) {
            val response = loadSearchPopularAndRecommend.invoke()
            Log.e("ayhan", "loadRecommendList : $response")
            if (response.success) {
                _recommendList.value = response.data.parseUI(userId)
            } else {
                _loadFail.value = response.message
            }
        }
    }

    fun loadSearchRecentItems() {
        viewModelScope.launch(ceh(_loadFail, "")) {
            val response = loadSearchRecommend.invoke()
            Log.e("ayhan", "loadSearchRecommendItems : $response")
            if (response.success) {
                _searchRecentlyItems.value = response.data?.recentSearch
            } else {
                _loadFail.value = response.message
            }
        }
    }

    fun loadSearchResult(searchWord: String) {
        if (searchWord.isEmpty()) {
            return
        }

        _searchResultItems.value = null

        viewModelScope.launch(ceh(_loadFail, "")) {
            val response = loadSearchResult.invoke(searchWord)
            Log.e("ayhan", "loadSearchResult : $response")
            if (response.success) {
                prevSearchWord = searchWord
                _searchResultItems.value = response.data.parseUI(userId)
                loadSearchRecentItems()

            } else {
                _loadFail.value = response.message
            }
        }
    }

    fun deleteRecentWord(deleteWord: String) {
        viewModelScope.launch(ceh(_loadFail, "")) {
            val response = deleteRecentWord.invoke(deleteWord)
            if (response.success) {
                val originRecentWords =
                    _searchRecentlyItems.value?.toMutableList()
                originRecentWords?.remove(deleteWord)
                _searchRecentlyItems.value = originRecentWords.orEmpty()
            } else {
                _loadFail.value = response.message
            }
        }
    }

    fun requestFollow(userId: String, isAlreadyFollowed: Boolean) {
        viewModelScope.launch(ceh(_actionFail, null)) {
            if (isAlreadyFollowed) {
                val response = requestUnFollow.invoke(userId)
                if (response.success) {
                    loadSearchResult(prevSearchWord)
                } else {
                    _actionFail.call()
                }
            } else {
                val response = requestFollow.invoke(userId)
                if (response.success) {
                    loadSearchResult(prevSearchWord)
                } else {
                    _actionFail.call()
                }
            }
        }
    }

    fun copyOtherBucket(bucketId: String) {
        viewModelScope.launch(ceh(_actionFail, null)) {
            val response = copyOtherBucket.invoke(bucketId)
            Log.e("ayhan", "copyOtherBucket response : $response")
            _copySucceed.value = response.success
        }
    }

    // 복사 성공 토스트를 노출한 뒤 이벤트를 소비 처리 (중복 노출 방지)
    fun consumeCopySucceed() {
        _copySucceed.value = false
    }

    private fun loadRecommendListDummy(): RecommendList {
        val bucketItem = listOf(
            SearchBucketItem(
                isMine = false,
                bucketId = "1",
                thumbnail = "1",
                title = "버킷리스트 타이틀 2줄 가이드\n버킷리스트 타이틀 2줄일 경우",
                writerId = "32",
                isScrapAvailable = false
            ),
            SearchBucketItem(
                isMine = false,
                bucketId = "2",
                title = "버킷리스트 타이틀",
                writerId = "32",
                isScrapAvailable = true
            ),
            SearchBucketItem(
                isMine = false,
                bucketId = "3",
                title = "버킷리스트 타이틀 2줄 가이드\n버킷리스트 타이틀 2줄일 경우",
                writerId = "32",
                isScrapAvailable = false
            ),
            SearchBucketItem(
                isMine = false,
                bucketId = "4",
                title = "버킷리스트 타이틀",
                writerId = "32",
                isScrapAvailable = true
            )
        )

        val popularItem = RecommendItem(
            type = RecommendType.POPULAR,
            tagList = listOf("여행", "공부", "문화"),
            items = bucketItem,
            //title = "인기"
        )

        val recommendItem = RecommendItem(
            type = RecommendType.RECOMMEND,
            tagList = listOf("제주도", "1박2일", "가족여행"),
            items = bucketItem,
            // title = "추천"
        )

        return RecommendList(listOf(popularItem, recommendItem))
    }
}
