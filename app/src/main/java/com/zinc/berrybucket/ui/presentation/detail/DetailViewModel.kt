package com.zinc.berrybucket.ui.presentation.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zinc.berrybucket.model.CommentInfo
import com.zinc.berrybucket.model.CommentMentionInfo
import com.zinc.berrybucket.model.Commenter
import com.zinc.berrybucket.model.CommonDetailDescInfo
import com.zinc.berrybucket.model.DetailInfo
import com.zinc.berrybucket.model.ImageInfo
import com.zinc.berrybucket.model.MemoInfo
import com.zinc.berrybucket.model.ProfileInfo
import com.zinc.domain.usecases.detail.LoadBucketDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val loadBucketDetail: LoadBucketDetail
) : ViewModel() {
//
//    private val _bucketDetailInfo = MutableLiveData<List<DetailDescType>>()
//    val bucketDetailInfo: LiveData<List<DetailDescType>> get() = _bucketDetailInfo

    private val _bucketDetailInfo = MutableLiveData<DetailInfo>()
    val bucketDetailInfo: LiveData<DetailInfo> get() = _bucketDetailInfo

    private val _validMentionList = MutableLiveData<List<CommentMentionInfo>>()
    val validMentionList: LiveData<List<CommentMentionInfo>> = _validMentionList


    fun getBucketDetail(id: String) {
//        viewModelScope.launch {
//            loadBucketDetail(id)
//        }

        if (id == "open") {
            _bucketDetailInfo.value = detailInfo1
        } else {
            _bucketDetailInfo.value = detailInfo1
        }
    }

    fun getValidMentionList() {
        val validMentionList = mutableListOf<CommentMentionInfo>()

        repeat(10) {
            validMentionList.add(
                CommentMentionInfo(
                    userId = "$it",
                    profileImage = "",
                    nickName = "가나다 $it",
                    isFriend = false,
                    isSelected = false
                )
            )
        }
        _validMentionList.value = validMentionList
    }

    private val detailInfo1 =
        DetailInfo(
            imageInfo = ImageInfo(
                imageList = listOf("A", "B", "C")
            ),
            profileInfo = ProfileInfo(
                profileImage = "",
                badgeImage = "",
                titlePosition = "멋쟁이 여행가",
                nickName = "한아크크룽삐옹"
            ),
            descInfo = CommonDetailDescInfo(
                dDay = "D+201",
                tagList = listOf("여행", "강남"),
                title = "가나다라마바사",
                goalCount = 10,
                userCount = 0
            ),
            memoInfo = MemoInfo(
                memo = "▶ 첫째날\n" +
                        "도두해안도로 - 도두봉키세스존 - 이호테우해변 - 오설록티뮤지엄 \n" +
                        "\n" +
                        "▶ 둘째날\n" +
                        " 쇠소깍 - 크엉해안경승지 - 이승악오름\n " +
                        "▶ 첫째날\n" +
                        "도두해안도로 - 도두봉키세스존 - 이호테우해변 - 오설록티뮤지엄 \n" +
                        "\n" +
                        "▶ 둘째날\n" +
                        " 쇠소깍 - 크엉해안경승지 - 이승악오름\n" + "▶ 첫째날\n" +
                        "도두해안도로 - 도두봉키세스존 - 이호테우해변 - 오설록티뮤지엄 \n" +
                        "\n" +
                        "▶ 둘째날\n" +
                        " 쇠소깍 - 크엉해안경승지 - 이승악오름\n" +
                        "▶ 첫째날\n" +
                        "도두해안도로 - 도두봉키세스존 - 이호테우해변 - 오설록티뮤지엄 \n" +
                        "\n" +
                        "▶ 둘째날\n" +
                        " 쇠소깍 - 크엉해안경승지 - 이승악오름\n"
            ),
            commentInfo = CommentInfo(
                commentCount = 2,
                listOf(
                    Commenter(
                        "1", "A", "아연이 내꺼지 너무너무 이쁘지", "@귀염둥이 이명선 베리버킷 댓글입니다.\n" +
                                "베리버킷 댓글입니다.", false
                    ),
                    Commenter(
                        "2", "B",
                        "Contrary to popular belief",
                        "Lorem Ipsum is not simply random text. It has roots in a piece of classical Latin literature from 45 BC, making it over 2000 years old. Richard McClintock, "
                    ),
                )
            )
        )

    private val detailInfo2 =
        DetailInfo(
            profileInfo = ProfileInfo(
                profileImage = "",
                badgeImage = "",
                titlePosition = "멋쟁이 여행가",
                nickName = "한아크크룽삐옹"
            ),
            descInfo = CommonDetailDescInfo(
                dDay = "D+201",
                tagList = listOf("여행", "강남"),
                title = "가나다라마바사",
                goalCount = 0,
                userCount = 0
            )
        )
}