package com.zinc.domain.usecases.detail

import com.zinc.domain.repository.DetailRepository
import javax.inject.Inject

class DeleteBucketComment @Inject constructor(
    private val detailRepository: DetailRepository
) {
    suspend operator fun invoke(id: String) =
        detailRepository.deleteBucketComment(id)
}


