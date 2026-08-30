package com.zinc.common.models

import com.google.gson.Gson
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 로그인/가입 응답은 실패 시 data 를 빼고 오거나, 앱이 모르는 상태값을 담아올 수 있다.
 * Gson 은 논널 필드에도 null 을 그대로 넣으므로 모델이 널을 허용해야 한다.
 */
class LoginResponseNullabilityTest {

    private val gson = Gson()

    @Test
    fun `로그인 실패 응답에 data 가 없어도 파싱된다`() {
        val response = gson.fromJson(
            """{"success":false,"code":"4001","message":"not found"}""",
            LoadTokenByEmailResponse::class.java
        )
        assertFalse(response.success)
        assertNull(response.data)
    }

    @Test
    fun `로그인 성공 응답에서 accessToken 을 읽는다`() {
        val response = gson.fromJson(
            """{"success":true,"code":"200","message":"ok","data":{"accessToken":"abc"}}""",
            LoadTokenByEmailResponse::class.java
        )
        assertEquals("abc", response.data?.accessToken)
    }

    @Test
    fun `유저 상태 응답에 data 가 없어도 파싱된다`() {
        val response = gson.fromJson(
            """{"success":false,"code":"5000","message":"error"}""",
            CheckUserStatusResponse::class.java
        )
        assertNull(response.data)
    }

    @Test
    fun `앱이 모르는 유저 상태값은 null 로 떨어진다`() {
        val response = gson.fromJson(
            """{"success":true,"code":"200","message":"ok","data":{"status":"DORMANT"}}""",
            CheckUserStatusResponse::class.java
        )
        assertNull(response.data?.status)
    }

    @Test
    fun `아는 유저 상태값은 그대로 파싱된다`() {
        val response = gson.fromJson(
            """{"success":true,"code":"200","message":"ok","data":{"status":"WITHDRAWN"}}""",
            CheckUserStatusResponse::class.java
        )
        assertEquals(CheckUserStatusResponse.Status.WITHDRAWN, response.data?.status)
    }
}
