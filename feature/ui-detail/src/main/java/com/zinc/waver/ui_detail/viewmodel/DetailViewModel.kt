package com.zinc.waver.ui_detail.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zinc.common.models.AddBucketCommentRequest
import com.zinc.common.models.DetailInfo
import com.zinc.common.models.ProfileInfo
import com.zinc.domain.usecases.common.SaveBucketLike
import com.zinc.domain.usecases.detail.AddBucketComment
import com.zinc.domain.usecases.detail.DeleteBucketComment
import com.zinc.domain.usecases.detail.DeleteMyBucket
import com.zinc.domain.usecases.detail.GoalCountUpdate
import com.zinc.domain.usecases.detail.HideBucketComment
import com.zinc.domain.usecases.detail.LoadBucketDetail
import com.zinc.domain.usecases.detail.LoadFriends
import com.zinc.domain.usecases.detail.LoadProfileInfo
import com.zinc.domain.usecases.my.AchieveMyBucket
import com.zinc.waver.model.BucketDetailUiInfo
import com.zinc.waver.model.CommentMentionInfo
import com.zinc.waver.model.DetailLoadFailStatus
import com.zinc.waver.ui.viewmodel.CommonViewModel
import com.zinc.waver.ui_detail.model.bucketDetailResponseToUiModel
import com.zinc.waver.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val loadBucketDetail: LoadBucketDetail,
    private val loadProfileInfo: LoadProfileInfo,
    private val achieveMyBucket: AchieveMyBucket,
    private val addBucketComment: AddBucketComment,
    private val deleteBucketComment: DeleteBucketComment,
    private val goalCountUpdate: GoalCountUpdate,
    private val saveBucketLike: SaveBucketLike,
    private val hideBucketComment: HideBucketComment,
    private val deleteMyBucket: DeleteMyBucket,
    private val loadFriends: LoadFriends,
) : CommonViewModel() {

    private val _bucketBucketDetailUiInfo = MutableLiveData<BucketDetailUiInfo>()
    val bucketBucketDetailUiInfo: LiveData<BucketDetailUiInfo> get() = _bucketBucketDetailUiInfo

    private val _validMentionList = MutableLiveData<List<CommentMentionInfo>>()
    val validMentionList: LiveData<List<CommentMentionInfo>> = _validMentionList

    private val _loadFail = SingleLiveEvent<DetailLoadFailStatus?>()
    val loadFail: LiveData<DetailLoadFailStatus?> get() = _loadFail

    private val _goToBack = SingleLiveEvent<Boolean>()
    val goToBack: LiveData<Boolean> get() = _goToBack

    private val _showLoading = MutableLiveData<Boolean>()
    val showLoading: LiveData<Boolean> get() = _showLoading

    private lateinit var bucketDetailData: DetailInfo
    private lateinit var profileInfo: ProfileInfo

    private var bucketId: String? = null
    private var writerId: String? = null
    private var isMine: Boolean? = true

    fun loadInitData(bucketId: String, writerId: String?, isMine: Boolean) {
        _loadFail.value = null
        viewModelScope.launch(ceh(_loadFail, DetailLoadFailStatus.LoadFail)) {
            val job1 = launch { getBucketDetail(bucketId, writerId, isMine) }
            val job2 = launch { getValidMentionList() }

            joinAll(job1, job2).runCatching {
                Log.e("ayhan", "runCatching")
            }.getOrElse {
                Log.e("ayhan", "getOrlElse, ${it.message}")
                _loadFail.value = DetailLoadFailStatus.LoadFail
                _showLoading.value = false
            }
        }
    }

    fun achieveMyBucket() {
        _loadFail.value = null

        viewModelScope.launch(ceh(_loadFail, DetailLoadFailStatus.AchieveFail)) {
            val response = achieveMyBucket(bucketId!!)
            if (response.success) {
                getBucketDetail(bucketId!!, writerId!!, isMine!!)
            } else {
                _loadFail.value = DetailLoadFailStatus.AchieveFail
            }
        }
    }

    fun saveBucketLike() {
        _loadFail.value = null

        viewModelScope.launch(ceh(_loadFail, DetailLoadFailStatus.LikeFail)) {
            val response = saveBucketLike(bucketId!!)
            if (response.success) {
                getBucketDetail(bucketId!!, writerId!!, isMine!!)
            } else {
                _loadFail.value = DetailLoadFailStatus.LikeFail
            }
        }
    }

    fun hideComment(commentId: String) {
        viewModelScope.launch(ceh(_loadFail, DetailLoadFailStatus.LoadFail)) {
            val response = hideBucketComment(commentId)
            Log.e("ayhan", "comment hide $response")
            if (response.success) {
                getBucketDetail(bucketId!!, writerId!!, isMine!!)
            } else {
                _loadFail.value = DetailLoadFailStatus.LoadFail
            }
        }
    }

    fun deleteMyBucket() {
        viewModelScope.launch(ceh(_loadFail, DetailLoadFailStatus.LoadFail)) {
            val response = deleteMyBucket(bucketId!!)
            Log.e("ayhan", "comment hide $response")
            if (response.success) {
                _goToBack.value = true
            } else {
                _loadFail.value = DetailLoadFailStatus.LoadFail
            }
        }
    }

    fun getBucketDetail(
        bucketId: String,
        writerId: String?,
        isMine: Boolean
    ) {
        _loadFail.value = null

        this.bucketId = bucketId
        this.writerId = writerId
        this.isMine = isMine

        viewModelScope.launch(ceh(_loadFail, DetailLoadFailStatus.LoadFail)) {
            _showLoading.value = true

            val job1 = launch { getBucketDetailData(bucketId, isMine) }
            val job2 = launch { getProfileInfo(isMine = isMine, writerId = writerId) }

            joinAll(job1, job2).runCatching {
                Log.e("ayhan", "runCatching")
            }.getOrElse {
                Log.e("ayhan", "getOrlElse, ${it.message}")
            }

            Log.e("ayhan", "getBucketDetail: $bucketDetailData , $profileInfo")

            _bucketBucketDetailUiInfo.value =
                bucketDetailResponseToUiModel(bucketDetailData, profileInfo, isMine, writerId)
            _showLoading.value = false
        }
    }

    private suspend fun getBucketDetailData(id: String, isMine: Boolean) {
        bucketDetailData = loadBucketDetail(id, isMine).data
    }

    private suspend fun getProfileInfo(writerId: String?, isMine: Boolean) {
        profileInfo = loadProfileInfo(isMine, writerId).data
    }

    // 멘션 목록은 댓글 작성용 부가 정보라, 실패해도 상세 로딩 실패 팝업을 띄우지 않는다.
    // (_loadFail 을 공유하면 버킷 상세는 정상인데 에러 팝업만 뜨는 문제가 생긴다)
    private fun getValidMentionList() {
        viewModelScope.launch(ceh(_validMentionList, emptyList())) {
            val response = loadFriends()
            if (response.success) {
                val mentionList = response.data.filter { it.mutualFollow }.map {
                    CommentMentionInfo(
                        userId = it.id,
                        profileImage = it.imgUrl,
                        nickName = it.name,
                        isFriend = it.mutualFollow,
                        isSelected = false
                    )
                }
                _validMentionList.value = mentionList
            } else {
                Log.e("ayhan", "멘션 목록 로드 실패: ${response.message}")
                _validMentionList.value = emptyList()
            }
        }
    }


    fun requestGoalCountUpdate(goalCount: Int) {
        _loadFail.value = null
        viewModelScope.launch(ceh(_loadFail, DetailLoadFailStatus.GoalCountUpdateFail)) {
            goalCountUpdate(bucketId!!, goalCount = goalCount)
            getBucketDetail(bucketId!!, writerId!!, true)
        }
    }

    fun requestAddBucketComment(request: AddBucketCommentRequest) {
        Log.e("ayhan", "comment request : $request")
        _loadFail.value = null
        viewModelScope.launch(ceh(_loadFail, DetailLoadFailStatus.AddCommentFail)) {
            val result = addBucketComment(request)
            Log.e("ayhan", "comment Result : $result")
            if (result.success) {
                getBucketDetail(bucketId!!, writerId!!, isMine!!)
            } else {
                _loadFail.value = DetailLoadFailStatus.AddCommentFail
            }
        }
    }

    fun requestDeleteBucketComment(id: String) {
        _loadFail.value = null
        viewModelScope.launch(ceh(_loadFail, DetailLoadFailStatus.DeleteCommentFail)) {
            val result = deleteBucketComment(id)
            Log.e("ayhan", "comment Result : $result")
            if (result.success) {
                getBucketDetail(bucketId!!, writerId!!, isMine!!)
            } else {
                _loadFail.value = DetailLoadFailStatus.DeleteCommentFail
            }
        }
    }
}