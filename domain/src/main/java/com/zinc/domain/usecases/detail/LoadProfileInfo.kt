package com.zinc.domain.usecases.detail

import com.zinc.domain.repository.DetailRepository
import javax.inject.Inject

class LoadProfileInfo @Inject constructor(
    private val detailRepository: DetailRepository
) {
    suspend operator fun invoke(isMine: Boolean, otherId: String?) =
        detailRepository.loadProfile(isMine, otherId)
}
