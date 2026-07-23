package com.zinc.waver.ui_search.model

import com.zinc.domain.models.SearchPopularAndRecommendResponse
import com.zinc.domain.models.SearchResultResponse

fun SearchResultResponse.SearchResult.parseUI(userId: String): SearchResultItems {
    val bucketItems = this.bucketlist.map {
        SearchBucketItem(
            isMine = it.userId == userId,
            bucketId = it.id.toString(),
            writerId = it.userId,
            thumbnail = null,
            title = it.title,
            isScrapAvailable = it.scrapYn?.isYes() == true
        )
    }

    val users = this.users.map {
        UserItem(
            userId = it.id.toString(),
            profileImageUrl = "null",
            nickName = it.name,
            isFollowed = it.followYn.isYes()
        )
    }

    return SearchResultItems(
        bucketItems = bucketItems, userItems = users
    )
}

fun SearchPopularAndRecommendResponse.RecommendData.parseUI(userId: String): RecommendList {
    val popularItems =
        RecommendItem(
            type = RecommendType.POPULAR,
            tagList = popularKeyword,
            items = this.popularList.map {
                SearchBucketItem(
                    isMine = userId == it.userId,
                    bucketId = it.id.toString(),
                    writerId = it.userId,
                    thumbnail = null,
                    title = it.title,
                    isScrapAvailable = it.scrapYn?.isYes() == true
                )
            },
        )

    val recommendItems = RecommendItem(
        type = RecommendType.RECOMMEND,
        tagList = recommendKeyword,
        items = this.recommendList.map {
            SearchBucketItem(
                isMine = userId == it.userId,
                bucketId = it.id.toString(),
                writerId = it.userId,
                thumbnail = null,
                title = it.title,
                isScrapAvailable = it.scrapYn?.isYes() == true
            )
        })

    val items = listOf(
        popularItems,
        recommendItems
    ).filter { it.items.isNotEmpty() }

    return RecommendList(items = items)
}

data class RecommendList(
    val items: List<RecommendItem>
)

enum class RecommendType() {
    POPULAR, RECOMMEND
}

data class RecommendItem(
    val type: RecommendType,
    val tagList: List<String>,
    val items: List<SearchBucketItem>
)

data class SearchBucketItem(
    val isMine: Boolean,
    val bucketId: String,
    val writerId: String,
    val thumbnail: String? = null,
    val title: String,
    val isScrapAvailable: Boolean,
)

data class KeyWordItem(
    val id: String,
    val keyword: String,
    val count: String
)

data class SearchResultItems(
    val bucketItems: List<SearchBucketItem>,
    val userItems: List<UserItem>
) {
    fun hasItems() = bucketItems.isNotEmpty() || userItems.isNotEmpty()
}

data class UserItem(
    val userId: String,
    val profileImageUrl: String? = "null",
    val nickName: String,
    val isFollowed: Boolean
)

