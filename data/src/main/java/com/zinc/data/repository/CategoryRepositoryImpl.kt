package com.zinc.data.repository

import com.zinc.common.models.AddNewCategoryRequest
import com.zinc.common.models.AllBucketListResponse
import com.zinc.common.models.AllBucketListSortType
import com.zinc.common.models.CommonResponse
import com.zinc.common.models.EditCategoryNameRequest
import com.zinc.common.models.LoadCategoryResponse
import com.zinc.common.models.ReorderedCategoryRequest
import com.zinc.data.api.BerryBucketApi
import com.zinc.domain.repository.CategoryRepository
import javax.inject.Inject

internal class CategoryRepositoryImpl @Inject constructor(
    private val berryBucketApi: BerryBucketApi
) : CategoryRepository {
    override suspend fun loadCategoryList(token: String): LoadCategoryResponse {
        return berryBucketApi.loadCategoryList(token)
    }

    override suspend fun addNewCategory(token: String, name: String): CommonResponse {
        return berryBucketApi.addNewCategory(token, AddNewCategoryRequest(name))
    }

    override suspend fun editCategoryName(token: String, id: Int, name: String): CommonResponse {
        return berryBucketApi.editCategoryName(token, EditCategoryNameRequest(id, name))
    }

    override suspend fun removeCategory(token: String, id: Int): CommonResponse {
        return berryBucketApi.removeCategoryItem(token, id)
    }

    override suspend fun reorderCategory(token: String, orderedIds: List<String>): CommonResponse {
        return berryBucketApi.reorderedCategory(token, ReorderedCategoryRequest(orderedIds))
    }

    override suspend fun searchCategoryList(token: String, query: String): LoadCategoryResponse {
        return berryBucketApi.searchCategoryList(token, query)
    }

    override suspend fun loadCategoryBucketList(
        token: String,
        categoryId: String,
        sort: AllBucketListSortType
    ): AllBucketListResponse {
        return berryBucketApi.loadAllBucketList(
            token = token,
            categoryId = categoryId,
            sort = sort
        )
    }
}