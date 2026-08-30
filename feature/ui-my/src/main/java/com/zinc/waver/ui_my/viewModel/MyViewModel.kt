package com.zinc.waver.ui_my.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zinc.common.models.AllBucketListRequest
import com.zinc.common.models.AllBucketListSortType
import com.zinc.common.models.BucketStatus
import com.zinc.common.models.DdaySortType
import com.zinc.common.models.TopProfile
import com.zinc.common.models.YesOrNo
import com.zinc.datastore.bucketListFilter.FilterPreferenceDataStoreModule
import com.zinc.datastore.login.PreferenceDataStoreModule
import com.zinc.domain.usecases.category.SearchCategoryList
import com.zinc.domain.usecases.my.AchieveMyBucket
import com.zinc.domain.usecases.my.LoadAllBucketList
import com.zinc.domain.usecases.my.LoadHomeProfileInfo
import com.zinc.domain.usecases.my.SearchAllBucketList
import com.zinc.domain.usecases.my.SearchDdayBucketList
import com.zinc.waver.model.AllBucketList
import com.zinc.waver.model.MyTabType
import com.zinc.waver.model.MyTabType.ALL
import com.zinc.waver.model.MyTabType.CATEGORY
import com.zinc.waver.model.MyTabType.CHALLENGE
import com.zinc.waver.model.MyTabType.DDAY
import com.zinc.waver.model.parseToUI
import com.zinc.waver.model.parseUI
import com.zinc.waver.ui.viewmodel.CommonViewModel
import com.zinc.waver.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyViewModel @Inject constructor(
    private val loadHomeProfileInfo: LoadHomeProfileInfo,
    private val loadAllBucketList: LoadAllBucketList,
    private val searchAllBucketList: SearchAllBucketList,
    private val searchCategoryList: SearchCategoryList,
    private val searchDdayBucketList: SearchDdayBucketList,
    private val achieveMyBucket: AchieveMyBucket,
    private val filterPreferenceDataStoreModule: FilterPreferenceDataStoreModule,
    private val preferenceDataStoreModule: PreferenceDataStoreModule,
) : CommonViewModel() {

    private val _profileInfo = MutableLiveData<TopProfile>()
    val profileInfo: LiveData<TopProfile> get() = _profileInfo

    private val _searchBucketResult = MutableLiveData<Pair<MyTabType, List<*>>>()
    val searchResult: LiveData<Pair<MyTabType, List<*>>> get() = _searchBucketResult

    private val _prevSearchedResult = MutableLiveData<Pair<MyTabType, String>>()
    val prevSearchedResult: LiveData<Pair<MyTabType, String>> get() = _prevSearchedResult

    private val _allBucketItem = MutableLiveData<AllBucketList>()
    val allBucketItem: LiveData<AllBucketList> get() = _allBucketItem

    private val _ddayBucketList = MutableLiveData<AllBucketList>()
    val ddayBucketList: LiveData<AllBucketList> = _ddayBucketList

    private val _showProgress = MutableLiveData<Boolean>()
    val showProgress: LiveData<Boolean> get() = _showProgress

    private val _showSucceed = MutableLiveData<Boolean>()
    val showSucceed: LiveData<Boolean> get() = _showSucceed

    private val _orderType = MutableLiveData<Int>()
    val orderType: LiveData<Int> get() = _orderType

    private val _showDdayView = MutableLiveData<Boolean>()
    val showDdayView: LiveData<Boolean> get() = _showDdayView

    private val _isShowPlusDday = MutableLiveData<Boolean>()
    val isShowPlusDday: LiveData<Boolean> get() = _isShowPlusDday

    private val _isShownMinusDday = MutableLiveData<Boolean>()
    val isShownMinusDday: LiveData<Boolean> get() = _isShownMinusDday

    private val _dataLoadFailed = SingleLiveEvent<Boolean>()
    val dataLoadFailed: LiveData<Boolean> get() = _dataLoadFailed

    private val _searchFailed = SingleLiveEvent<Nothing>()
    val searchFailed: LiveData<Nothing> get() = _searchFailed

    private val _achieveBucketFail = SingleLiveEvent<Nothing>()
    val achieveBucketFail: LiveData<Nothing> get() = _achieveBucketFail

    private val _achieveSucceed = MutableLiveData<String>()
    val achieveSucceed: LiveData<String> get() = _achieveSucceed

    // 필터 저장 완료 이벤트. 값을 보관하지 않으므로 리컴포지션에서 재발행되지 않는다.
    private val _allFilterSaved = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val allFilterSaved: SharedFlow<Unit> = _allFilterSaved.asSharedFlow()

    private val _ddayFilterSaved = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val ddayFilterSaved: SharedFlow<Unit> = _ddayFilterSaved.asSharedFlow()

    private val searchCeh = CoroutineExceptionHandler { _, throwable ->
        Log.e("ayhan", "searchFail 1 : $throwable")
        _searchFailed.call()
    }

    private fun ceh(liveData: SingleLiveEvent<Nothing>) =
        CoroutineExceptionHandler { _, _ ->
            liveData.call()
        }

    /**
     * 저장된 필터를 먼저 읽어온 뒤 전체 버킷리스트를 조회한다.
     * 필터 로드와 목록 조회를 하나의 코루틴에서 순차 실행하므로,
     * 필터가 아직 없는 상태로 목록을 요청하는 일이 발생하지 않는다.
     */
    fun refreshAllBucketList() {
        viewModelScope.launch(ceh(_dataLoadFailed, true)) {
            loadAllFilterPref()
            fetchAllBucketList()
        }
    }

    /**
     * 저장된 필터를 먼저 읽어온 뒤 디데이 버킷리스트를 조회한다.
     * 정렬은 전체 탭 필터를 사용하므로 함께 읽어온다.
     */
    fun refreshDdayBucketList() {
        viewModelScope.launch(ceh(_dataLoadFailed, true)) {
            loadAllFilterPref()
            loadDdayFilterPref()
            fetchDdayBucketList()
        }
    }

    private suspend fun loadAllFilterPref() {
        filterPreferenceDataStoreModule.apply {
            _showProgress.value = loadIsProgress.first()
            _showSucceed.value = loadIsSucceed.first()
            _orderType.value = loadOrderType.first()
            _showDdayView.value = loadShowDday.first()
        }
    }

    private suspend fun loadDdayFilterPref() {
        filterPreferenceDataStoreModule.apply {
            _isShownMinusDday.value = loadIsDdayMinus.first()
            _isShowPlusDday.value = loadIsDdayPlus.first()
        }
    }

    fun loadProfile() {
        viewModelScope.launch(CoroutineExceptionHandler { _, _ ->
            _dataLoadFailed.value = false
        }) {
            val response = loadHomeProfileInfo.invoke()
            if (response.success) {
                val data = response.data
                val topProfile = TopProfile(
                    name = data.name,
                    imgUrl = data.imgUrl,
                    badgeImgUrl = data.badgeImgUrl,
                    badgeTitle = data.badgeTitle,
                    bio = data.bio,
                    followingCount = data.followingCount,
                    followerCount = data.followerCount,
                    percent = data.bucketInfo.grade()
                )

                preferenceDataStoreModule.setUserIdKey(data.id)

                Log.e("ayhan", "homeProfike : $topProfile")
                _profileInfo.value = topProfile
                _dataLoadFailed.value = false
            } else {
                //  Log.e("ayhan", "Fail Load Profile not success")
                _dataLoadFailed.value = false
            }
        }
    }

    /** 진행중/완료 상태별 목록 화면 진입용. 정렬 필터가 필요하므로 필터를 먼저 읽는다. */
    fun loadStatusBucketList(status: BucketStatus) {
        viewModelScope.launch(ceh(_dataLoadFailed, true)) {
            loadAllFilterPref()
            fetchAllBucketList(status)
        }
    }

    private suspend fun fetchAllBucketList(status: BucketStatus? = null) {
        val allBucketListRequest = AllBucketListRequest(
            dDayBucketOnly = null,
            isPassed = null,
            status = status ?: loadStatusFilter(),
            sort = loadSortFilter()
        )

        Log.e("ayhan", "allBucketListRequest : $allBucketListRequest")

        loadAllBucketList.invoke(allBucketListRequest).apply {
            if (this.success) {
                val data = this.data
                _allBucketItem.value = AllBucketList(
                    processingCount = data.progressCount.toString(),
                    succeedCount = data.completedCount.toString(),
                    bucketList = data.bucketlist.parseToUI()
                )
                _dataLoadFailed.value = false
            } else {
                _dataLoadFailed.value = true
            }
        }
    }

    private fun loadStatusFilter(): BucketStatus? =
        if (_showSucceed.value == true && _showProgress.value == true) null
        else if (_showSucceed.value == true) BucketStatus.COMPLETE
        else if (_showProgress.value == true) BucketStatus.PROGRESS
        else BucketStatus.PROGRESS

    private fun loadSortFilter() =
        if (_orderType.value == 1) AllBucketListSortType.CREATED else AllBucketListSortType.UPDATED

    private suspend fun fetchDdayBucketList() {
        val isPassed = if (isShowPlusDday.value == true && isShownMinusDday.value == true) {
            null
        } else if (isShowPlusDday.value == true) {
            YesOrNo.Y.name
        } else if (isShownMinusDday.value == true) {
            YesOrNo.N.name
        } else {
            null
        }

        val allBucketListRequest = AllBucketListRequest(
            dDayBucketOnly = YesOrNo.Y.name,
            isPassed = isPassed,
            status = null,
            sort = loadSortFilter()
        )

        loadAllBucketList.invoke(allBucketListRequest).apply {
            if (this.success) {
                val data = this.data
                val uiAllBucketList = AllBucketList(
                    processingCount = data.progressCount.toString(),
                    succeedCount = data.completedCount.toString(),
                    bucketList = data.bucketlist.parseToUI()
                )

                val filteredList =
                    if (_isShownMinusDday.value == true && _isShowPlusDday.value == true) {
                        uiAllBucketList.bucketList
                    } else if (_isShowPlusDday.value == true) {
                        uiAllBucketList.bucketList.filter { it.getDdayType() == DdaySortType.PLUS || it.getDdayType() == DdaySortType.D_DAY }
                    } else if (_isShownMinusDday.value == true) {
                        uiAllBucketList.bucketList.filter { it.getDdayType() == DdaySortType.MINUS }
                    } else {
                        uiAllBucketList.bucketList
                    }

                _ddayBucketList.value = uiAllBucketList.copy(bucketList = filteredList)
                _dataLoadFailed.value = false
            } else {
                _dataLoadFailed.value = true
            }
        }
    }

    fun searchList(type: MyTabType, searchWord: String) {
        _prevSearchedResult.value = type to searchWord
        when (type) {
            is ALL -> searchAllBucket(searchWord = searchWord)
            is DDAY -> searchDdayBucket(searchWord = searchWord)
            is CATEGORY -> searchCategoryItems(searchWord = searchWord)
            is CHALLENGE -> {
                // TODO : 챌린지 기능 추가 필요
            }
        }
    }

    fun updateAllBucketFilter(
        isProgress: Boolean?,
        isSucceed: Boolean?,
        orderType: Int?,
        showDday: Boolean?
    ) {
        viewModelScope.launch(ceh(_dataLoadFailed, true)) {
            filterPreferenceDataStoreModule.apply {
                isProgress?.let {
                    setProgress(isProgress)
                    _showProgress.value = it
                }
                isSucceed?.let {
                    setSucceed(isSucceed)
                    _showSucceed.value = it
                }
                orderType?.let {
                    setOrderType(it)
                    _orderType.value = it
                }
                showDday?.let {
                    setShowDday(it)
                    _showDdayView.value = it
                }
            }

            _allFilterSaved.tryEmit(Unit)
            fetchAllBucketList()
        }
    }

    fun updateDdayBucketFilter(
        isMinusShow: Boolean?,
        isPlusShow: Boolean?
    ) {
        viewModelScope.launch(ceh(_dataLoadFailed, true)) {
            filterPreferenceDataStoreModule.apply {
                isMinusShow?.let {
                    Log.e("ayhan", "minus :  $it")
                    setShowDdayMinus(isMinusShow)
                    _isShownMinusDday.value = it
                }
                isPlusShow?.let {
                    Log.e("ayhan", "plus :  $it")
                    setShowDdayPlus(isPlusShow)
                    _isShowPlusDday.value = it
                }
            }
            _ddayFilterSaved.tryEmit(Unit)
            fetchDdayBucketList()
        }
    }

    private fun searchAllBucket(searchWord: String) {
        viewModelScope.launch(searchCeh) {
            searchAllBucketList.invoke(searchWord).apply {
                if (this.success) {
                    _searchBucketResult.value = Pair(
                        ALL,
                        this.data.bucketlist
                    )
                } else {
                    Log.e("ayhan", "searchFail 3 : ${this.message}")
                    _searchFailed.call()
                }
            }
        }
    }

    private fun searchDdayBucket(searchWord: String) {
        viewModelScope.launch(searchCeh) {
            searchDdayBucketList.invoke(searchWord).apply {
                if (this.success) {
                    _searchBucketResult.value = Pair(
                        DDAY,
                        this.data.bucketlist
                    )
                } else {
                    Log.e("ayhan", "searchFail 3 : ${this.message}")
                    _searchFailed.call()
                }
            }
        }
    }

    private fun searchCategoryItems(searchWord: String) {
        viewModelScope.launch(searchCeh) {
            searchCategoryList.invoke(searchWord).apply {
                Log.e("ayhan", "category : $this")
                if (this.success) {
                    val parseData = data.parseUI()
                    _searchBucketResult.value = Pair(
                        CATEGORY,
                        parseData
                    )
                } else {
                    _searchFailed.call()
                    Log.e("ayhan", "searchCategoryItems Fail3 : $message")
                }
            }
        }
    }

    fun achieveBucket(id: String, type: MyTabType) {
        viewModelScope.launch(ceh(_achieveBucketFail)) {
            val response = achieveMyBucket(id)
            Log.e("ayhan", "Achieve Response : $response")
            if (response.success) {
                when (type) {
                    is ALL -> fetchAllBucketList()
                    is DDAY -> fetchDdayBucketList()
                    else -> {
                        // Do Nothing
                    }
                }
            } else {
                _achieveBucketFail.call()
            }
        }
    }
}