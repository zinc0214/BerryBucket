package com.zinc.domain.usecases.my

import com.zinc.domain.repository.MyRepository
import javax.inject.Inject

class LoadFollowList @Inject constructor(
    private val myRepository: MyRepository
) {
    suspend operator fun invoke() =
        myRepository.loadFollowList()
}