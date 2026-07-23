package com.zinc.common.models

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UserLimitInfoTest {

    private fun info(
        status: ProfileInfo.PremiumStatus,
        imageLimit: Int = 1,
        imageUsed: Int = 0,
        togetherLimit: Int = 3,
        togetherUsed: Int = 0
    ) = UserLimitInfo(
        premiumStatus = status,
        imageLimit = imageLimit,
        imageUsed = imageUsed,
        togetherLimit = togetherLimit,
        togetherUsed = togetherUsed
    )

    @Test
    fun `ACTIVE 구독자는 횟수와 무관하게 모두 사용 가능`() {
        val limit = info(
            ProfileInfo.PremiumStatus.ACTIVE,
            imageLimit = 1, imageUsed = 1,
            togetherLimit = 3, togetherUsed = 3
        )
        assertTrue(limit.isPremium)
        assertTrue(limit.canUseImage)
        assertTrue(limit.canUseTogether)
    }

    @Test
    fun `NONE 이어도 무료 횟수가 남아있으면 사용 가능`() {
        val limit = info(ProfileInfo.PremiumStatus.NONE, imageUsed = 0, togetherUsed = 2)
        assertFalse(limit.isPremium)
        assertTrue(limit.canUseImage)
        assertTrue(limit.canUseTogether)
    }

    @Test
    fun `EXPIRED 이고 무료 횟수를 소진하면 사용 불가`() {
        val limit = info(
            ProfileInfo.PremiumStatus.EXPIRED,
            imageLimit = 1, imageUsed = 1,
            togetherLimit = 3, togetherUsed = 3
        )
        assertFalse(limit.canUseImage)
        assertFalse(limit.canUseTogether)
    }

    @Test
    fun `이미지만 소진한 경우 이미지는 불가, 함께하기는 가능`() {
        val limit = info(
            ProfileInfo.PremiumStatus.NONE,
            imageLimit = 1, imageUsed = 1,
            togetherLimit = 3, togetherUsed = 0
        )
        assertFalse(limit.canUseImage)
        assertTrue(limit.canUseTogether)
    }

    @Test
    fun `NONE 폴백은 모두 사용 불가`() {
        assertFalse(UserLimitInfo.NONE.isPremium)
        assertFalse(UserLimitInfo.NONE.canUseImage)
        assertFalse(UserLimitInfo.NONE.canUseTogether)
    }
}
