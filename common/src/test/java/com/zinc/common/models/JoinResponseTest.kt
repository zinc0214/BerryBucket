package com.zinc.common.models

import com.google.gson.Gson
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class JoinResponseTest {

    private val gson = Gson()

    private fun parse(json: String): JoinResponse =
        gson.fromJson(json, JoinResponse::class.java)

    @Test
    fun `myburyYn 이 Y 면 마이버리 회원으로 판정한다`() {
        val response = parse(
            """{"success":true,"code":"200","message":"ok","data":{"myburyYn":"Y"}}"""
        )
        assertTrue(response.data?.isMyBuryUser() == true)
    }

    @Test
    fun `myburyYn 이 N 이면 마이버리 회원이 아니다`() {
        val response = parse(
            """{"success":true,"code":"200","message":"ok","data":{"myburyYn":"N"}}"""
        )
        assertFalse(response.data?.isMyBuryUser() == true)
    }

    @Test
    fun `data 가 아예 없으면 마이버리 회원이 아니다`() {
        val response = parse("""{"success":true,"code":"200","message":"ok"}""")
        assertNull(response.data)
        assertFalse(response.data?.isMyBuryUser() == true)
    }

    @Test
    fun `data 안에 myburyYn 이 없으면 마이버리 회원이 아니다`() {
        val response = parse("""{"success":true,"code":"200","message":"ok","data":{}}""")
        assertFalse(response.data?.isMyBuryUser() == true)
    }

    @Test
    fun `알 수 없는 myburyYn 값은 마이버리 회원이 아니다`() {
        val response = parse(
            """{"success":true,"code":"200","message":"ok","data":{"myburyYn":"MAYBE"}}"""
        )
        assertFalse(response.data?.isMyBuryUser() == true)
    }
}
