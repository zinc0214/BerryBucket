package com.zinc.domain.usecases.my

import com.zinc.common.models.AllBucketListRequest
import com.zinc.domain.repository.MyRepository
import javax.inject.Inject

class LoadAllBucketList @Inject constructor(
    private val myRepository: MyRepository
) {
    suspend operator fun invoke(
        allBucketListRequest: AllBucketListRequest
    ) = myRepository.loadAllBucketList(allBucketListRequest)
}