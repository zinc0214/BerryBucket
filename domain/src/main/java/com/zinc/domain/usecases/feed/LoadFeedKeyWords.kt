package com.zinc.domain.usecases.feed

import com.zinc.domain.repository.FeedRepository
import javax.inject.Inject

class LoadFeedKeyWords @Inject constructor(
    private val feedRepository: FeedRepository
) {
    suspend operator fun invoke() = feedRepository.loadSelectableFeedKeyWords()
}