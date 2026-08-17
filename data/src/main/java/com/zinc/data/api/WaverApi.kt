package com.zinc.data.api

import com.zinc.common.models.AddBucketCommentRequest
import com.zinc.common.models.AddNewCategoryRequest
import com.zinc.common.models.MyPushResponse
import com.zinc.common.models.AllBucketListResponse
import com.zinc.common.models.AllBucketListSortType
import com.zinc.common.models.BucketDetailResponse
import com.zinc.common.models.BucketStatus
import com.zinc.common.models.CheckUserLimitResponse
import com.zinc.common.models.CheckUserStatusRequest
import com.zinc.common.models.CheckUserStatusResponse
import com.zinc.common.models.CommonResponse
import com.zinc.common.models.CommonResponse2
import com.zinc.common.models.EditCategoryNameRequest
import com.zinc.common.models.FollowResponse
import com.zinc.common.models.HomeProfileResponse
import com.zinc.common.models.JoinResponse
import com.zinc.common.models.KeywordResponse
import com.zinc.common.models.LoadCategoryResponse
import com.zinc.common.models.LoadMyWaveBadgeResponse
import com.zinc.common.models.LoadMyWaveInfoResponse
import com.zinc.common.models.LoadTokenByEmailRequest
import com.zinc.common.models.LoadTokenByEmailResponse
import com.zinc.common.models.LoadWriteSelectableFriendsResponse
import com.zinc.common.models.ProfileResponse
import com.zinc.common.models.RefreshTokenResponse
import com.zinc.common.models.ReorderedCategoryRequest
import com.zinc.common.models.YesOrNo
import com.zinc.domain.models.FeedListResponse
import com.zinc.domain.models.FollowOtherUserRequest
import com.zinc.domain.models.OtherHomeResponse
import com.zinc.domain.models.ReportReasonRequest
import com.zinc.domain.models.RequestGoalCountUpdate
import com.zinc.domain.models.SavedKeywordItemsRequest
import com.zinc.domain.models.SearchPopularAndRecommendResponse
import com.zinc.domain.models.SearchRecommendResponse
import com.zinc.domain.models.SearchResultResponse
import com.zinc.domain.models.SubscriptionStartRequest
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface WaverApi {

    // 토큰재발급
    @GET("/waver/token/refresh")
    suspend fun refreshToken(): RefreshTokenResponse

    // 프로필생성
    @POST("/waver/user/join")
    @Multipart
    suspend fun crateProfile(
        @Part accountType: MultipartBody.Part, // 기기 유형
        @Part email: MultipartBody.Part,
        @Part uid: MultipartBody.Part,
        @Part name: MultipartBody.Part,
        @Part bio: MultipartBody.Part?,
        @Part profileImage: MultipartBody.Part?
    ): JoinResponse

    // 마이버리 데이터 이관 요청 (mybury 기존 회원만 요청 가능, 스케줄러가 순차 이관)
    @POST("/waver/user/migration")
    suspend fun requestMyBuryMigration(): CommonResponse2

    // 로그인
    @POST("/waver/login")
    suspend fun requestLogin(@Body loadTokenByEmail: LoadTokenByEmailRequest): LoadTokenByEmailResponse

    // 이메일 상태 조회
    @POST("/waver/user/status")
    suspend fun checkUserStatus(@Body loadTokenByEmail: CheckUserStatusRequest): CheckUserStatusResponse
    // 홈 > 프로필 조회
    @GET("/waver/my")
    suspend fun loadMyProfileInfo(): HomeProfileResponse

    // 전체버킷리스트 조회
    @GET("/waver/bucket")
    suspend fun loadAllBucketList(
        @Query("dDayBucketOnly") dDayBucketOnly: String? = null,
        @Query("isPassed") isPassed: String? = null,
        @Query("status") status: BucketStatus? = null,
        @Query("sort") sort: AllBucketListSortType? = AllBucketListSortType.ORDERED,
        @Query("categoryId") categoryId: String? = null,
        @Query("query") query: String? = null,
    ): AllBucketListResponse

    // 카테고리 전체 로드
    @GET("/waver/category")
    suspend fun loadCategoryList(): LoadCategoryResponse

    // 카테고리 별 로드
    @GET("/waver/category")
    suspend fun searchCategoryList(
        @Query("query") query: String
    ): LoadCategoryResponse

    // 카테고리 추가
    @POST("/waver/category")
    suspend fun addNewCategory(
        @Body request: AddNewCategoryRequest
    ): CommonResponse

    // 카테고리 수정
    @PATCH("/waver/category/{id}")
    suspend fun editCategoryName(
        @Path("id") id: Int,
        @Body request: EditCategoryNameRequest
    ): CommonResponse

    // 카테고리 삭제
    @HTTP(method = "DELETE", path = "/waver/category/{id}")
    suspend fun removeCategoryItem(
        @Path("id") id: Int
    ): CommonResponse

    // 카테고리 순서 변경
    @PATCH("/waver/category/seq")
    suspend fun reorderedCategory(
        @Body request: ReorderedCategoryRequest
    ): CommonResponse

    // 버킷리스트 상세
    @GET("/waver/bucket/{id}")
    suspend fun loadBucketDetail(
        @Path("id") id: String
    ): BucketDetailResponse

    // 피드 > 키워드 선택 > 저장
    @POST("/waver/feeds/keyword")
    suspend fun savedFeedKeywords(
        @Body request: SavedKeywordItemsRequest
    ): CommonResponse

    // 피드 조회
    @GET("/waver/feeds")
    suspend fun loadFeedItems(
        @Query("nextKey") nextKey: Int?
    ): FeedListResponse

    // 버킷리스트 등록
    @POST("/waver/bucket")
    @Multipart
    suspend fun addNewBucketList(
        @Part bucketType: MultipartBody.Part,
        @Part exposureStatus: MultipartBody.Part, // 공개여부
        @Part title: MultipartBody.Part,
        @Part memo: MultipartBody.Part?, // 메모
        @Part keywords: MultipartBody.Part?, // 키워드 (태그) 키워드 목록(최대 5) - ","로 구분
        @Part friendUserIds: MultipartBody.Part?, // 함께할 친구 ID - 함께하기인 경우 최소 1명 필수(최대 5)
        @Part scrapYn: MultipartBody.Part, // 스크랩 여부
        @Part images: List<MultipartBody.Part>?, // 이미지 목록(최대 3)
        @Part targetDate: MultipartBody.Part?, // 목표완료일(yyyy-MM-dd)
        @Part goalCount: MultipartBody.Part, //  목표 횟수
        @Part categoryId: MultipartBody.Part // 카테고리 ID
    ): CommonResponse

    // 버킷리스트 수정
    @POST("/waver/bucket/{id}")
    @Multipart
    suspend fun updateBucketList(
        @Part bucketType: MultipartBody.Part,
        @Part exposureStatus: MultipartBody.Part, // 공개여부
        @Part title: MultipartBody.Part,
        @Part memo: MultipartBody.Part?, // 메모
        @Part keywords: MultipartBody.Part?, // 키워드 (태그) 키워드 목록(최대 5) - ","로 구분
        @Part friendUserIds: MultipartBody.Part?, // 함께할 친구 ID - 함께하기인 경우 최소 1명 필수(최대 5)
        @Part scrapYn: MultipartBody.Part, // 스크랩 여부
        @Part images: List<MultipartBody.Part>?, // 이미지 목록(최대 3)
        @Part targetDate: MultipartBody.Part?, // 목표완료일(yyyy-MM-dd)
        @Part goalCount: MultipartBody.Part, //  목표 횟수
        @Part categoryId: MultipartBody.Part, // 카테고리 ID
        @Path("id") id: String
    ): CommonResponse

    // 받은 푸시 알림 내역
    @GET("/waver/my/push")
    suspend fun loadMyPushList(): MyPushResponse

    // 피드, 버킷리스트 생성(수정) 시 키워드 아이템 로드
    @GET("/waver/explore/keywords")
    suspend fun loadKeywords(): KeywordResponse

    // 팔로우 목록
    @GET("/waver/follow")
    suspend fun loadFollowList(): FollowResponse

    // 펄로오 > 언팔하기
    @POST("/waver/follow/unfollow")
    suspend fun requestUnfollow(
        @Body followUserId: FollowOtherUserRequest
    ): CommonResponse

    // 팔로우 하기
    @POST("/waver/follow")
    suspend fun requestFollow(
        @Body followUserId: FollowOtherUserRequest
    ): CommonResponse

    // 프로필편집 > 프로필조회
    @GET("/waver/user/profile")
    suspend fun loadMyProfile(): ProfileResponse

    // 프로필편집 > 프로필 수정
    @PATCH("/waver/user/profile")
    @Multipart
    suspend fun updateMyProfile(
        @Part name: MultipartBody.Part,
        @Part bio: MultipartBody.Part,
        @Part profileImage: MultipartBody.Part?
    ): CommonResponse

    // 닉네임 중복 확인
    @GET("/waver/user/profile/name")
    suspend fun checkAlreadyUsedNickname(
        @Query("name") name: String
    ): CommonResponse2

    // 버킷리스트 전체 검색
    @GET("/waver/bucket")
    suspend fun searchAllBucketList(
        @Query("query") query: String
    ): AllBucketListResponse

    // 디데이 버킷리스트 검색
    @GET("/waver/bucket")
    suspend fun searchDdayBucketList(
        @Query("dDayBucketOnly") dDayBucketOnly: String? = YesOrNo.Y.name
    ): AllBucketListResponse

    // 버킷리스트 달성
    @GET("/waver/bucket/{id}/achieve")
    suspend fun achieveBucket(@Path("id") id: String): CommonResponse

    // 검색 > 인기검색어, 추천버킷리스트 조회
    @GET("waver/bucket/popular")
    suspend fun loadSearchPopularAndRecommend(): SearchPopularAndRecommendResponse

    // 검색 > 버킷리스트 검색
    @GET("/waver/explore")
    suspend fun loadSearchResult(@Query("query") query: String): SearchResultResponse

    // 검색 > 추천검색어, 최근검색어 조회
    @GET("/waver/explore/searchOptions")
    suspend fun loadSearchRecommend(): SearchRecommendResponse

    // 검색 > 최근검색어 > 삭제
    @DELETE("/waver/explore/recentSearch/{keyword}")
    suspend fun deleteSearchRecentWord(
        @Path("keyword") keyword: String
    ): CommonResponse

    // 피드 > 버킷리스트 좋아요
    @POST("/waver/feeds/{id}/like")
    suspend fun saveBucketLike(@Path("id") id: String): CommonResponse

    // 피드 > 다른사람 버킷리스트 상세
    @GET("/waver/bucket/{id}")
    suspend fun loadOtherBucketDetail(@Path("id") id: String): BucketDetailResponse

    // 버킷리스트 상세 > 댓글 달기
    @POST("/waver/comment")
    suspend fun addBucketComment(@Body request: AddBucketCommentRequest): CommonResponse

    // 내 버킷리스트 > 달성횟수 변경
    @PATCH("/waver/bucket/{id}/goalCount")
    suspend fun requestGoalCountUpdate(
        @Path("id") id: String,
        @Body goalCount: RequestGoalCountUpdate
    ): CommonResponse

    @GET("/waver/follow/mutual")
    suspend fun loadWriteSelectableFriends(): LoadWriteSelectableFriendsResponse

    @GET("/waver/user/profile")
    suspend fun loadOtherProfileInfo(
        @Query("otherUserId") id: String,
    ): ProfileResponse

    @GET("/waver/my/{otherUserId}")
    suspend fun loadOtherHome(@Path("otherUserId") id: String): OtherHomeResponse

    // 댓글 삭제
    @DELETE("/waver/comment/{id}")
    suspend fun deleteComment(@Path("id") id: String): CommonResponse

    // 댓글 숨김
    @POST("/waver/comment/{id}/hide")
    suspend fun hideComment(@Path("id") id: String): CommonResponse

    // 버킷리스트 삭제
    @DELETE("/waver/bucket/{id}")
    suspend fun deleteMyBucket(@Path("id") id: String): CommonResponse

    // 버킷리스트 신고
    @POST("/waver/feeds/{id}/report")
    suspend fun requestBucketReport(
        @Path("id") id: String,
        @Body reason: ReportReasonRequest
    ): CommonResponse

    // 댓글 신고
    @PATCH("/waver/comment/{id}/report")
    suspend fun requestBucketCommentReport(
        @Path("id") id: String,
        @Body reason: ReportReasonRequest
    ): CommonResponse

    // 버킷 복사
    @POST("/waver/feeds/{id}/scrap")
    suspend fun copyOtherBucket(
        @Path("id") id: String
    ): CommonResponse

    // 내 웨이브 뱃지
    @GET("/waver/badge")
    suspend fun loadMyWaveBadge(): LoadMyWaveBadgeResponse

    // 내 웨이브 정보
    @GET("/waver/my/info")
    suspend fun loadMyWaveInfo(): LoadMyWaveInfoResponse

    // 뱃지 업데이트
    @POST("/waver/badge/{badgeId}")
    suspend fun updateMyBadge(@Path("badgeId") badgeId: Int): CommonResponse

    // 회원탈퇴
    @POST("/waver/user/withdraw")
    suspend fun requestWithdraw(): CommonResponse

    // 웨이버 플러스 구독 시작 알림
    @POST("/waver/subscription/start")
    suspend fun startSubscription(
        @Body request: SubscriptionStartRequest
    ): CommonResponse

    // 웨이버 플러스 구독 상태 / 무료 혜택 한도 확인
    @GET("/waver/user/check/limit")
    suspend fun checkUserLimit(): CheckUserLimitResponse

}
