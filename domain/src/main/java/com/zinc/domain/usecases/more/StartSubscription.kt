package com.zinc.domain.usecases.more

import com.zinc.domain.models.SubscriptionStartRequest
import com.zinc.domain.repository.MoreRepository
import javax.inject.Inject

class StartSubscription @Inject constructor(
    private val moreRepository: MoreRepository
) {
    suspend operator fun invoke(request: SubscriptionStartRequest) =
        moreRepository.startSubscription(request)
}
