package com.zinc.common.models

import org.junit.Assert.assertEquals
import org.junit.Test

class MyBuryMigrationResultTest {

    private fun response(success: Boolean, code: String) =
        CommonResponse2(success = success, code = code, message = "")

    @Test
    fun `요청이 접수되면 REQUESTED`() {
        assertEquals(
            MyBuryMigrationResult.REQUESTED,
            MyBuryMigrationResult.from(response(success = true, code = "200"))
        )
    }

    @Test
    fun `8201 은 이미 이관이 완료된 상태`() {
        assertEquals(
            MyBuryMigrationResult.ALREADY_DONE,
            MyBuryMigrationResult.from(response(success = false, code = "8201"))
        )
    }

    @Test
    fun `8202 는 이미 이관이 요청된 상태`() {
        assertEquals(
            MyBuryMigrationResult.ALREADY_REQUESTED,
            MyBuryMigrationResult.from(response(success = false, code = "8202"))
        )
    }

    @Test
    fun `8200 마이버리 회원이 아니면 안내 없이 넘어간다`() {
        assertEquals(
            MyBuryMigrationResult.NONE,
            MyBuryMigrationResult.from(response(success = false, code = "8200"))
        )
    }

    @Test
    fun `알 수 없는 실패 코드는 안내 없이 넘어간다`() {
        assertEquals(
            MyBuryMigrationResult.NONE,
            MyBuryMigrationResult.from(response(success = false, code = "9999"))
        )
    }

    @Test
    fun `성공 응답은 코드와 무관하게 REQUESTED 로 판정한다`() {
        assertEquals(
            MyBuryMigrationResult.REQUESTED,
            MyBuryMigrationResult.from(response(success = true, code = "8201"))
        )
    }
}
