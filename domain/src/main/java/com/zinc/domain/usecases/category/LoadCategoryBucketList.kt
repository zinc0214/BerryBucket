package com.zinc.domain.usecases.category

import com.zinc.common.models.AllBucketListSortType
import com.zinc.domain.repository.CategoryRepository
import javax.inject.Inject

class LoadCategoryBucketList @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(id: String, sort: AllBucketListSortType) =
        categoryRepository.loadCategoryBucketList(id, sort)
}
