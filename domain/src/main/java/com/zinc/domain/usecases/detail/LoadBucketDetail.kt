package com.zinc.domain.usecases.detail

import com.zinc.common.models.AddBucketCommentRequest
import com.zinc.domain.repository.DetailRepository
import javax.inject.Inject

class LoadBucketDetail @Inject constructor(
    private val detailRepository: DetailRepository
) {
    suspend operator fun invoke(token: String, id: String, isMine: Boolean) =
        detailRepository.loadBucketDetail(token, id, isMine)

    suspend operator fun invoke(token: String, request: AddBucketCommentRequest) =
        detailRepository.addBucketComment(token, request)
}