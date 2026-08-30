package com.zinc.data.api

import com.zinc.datastore.login.PreferenceDataStoreModule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject

class TokenInterceptor @Inject constructor(
    private val preferenceDataStoreModule: PreferenceDataStoreModule
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return runBlocking {
            // accessToken 은 절대 로그로 남기지 않는다. minify 가 꺼져 있어 릴리스 빌드에도 그대로 출력된다.
            val accessToken = preferenceDataStoreModule.loadAccessToken.first()
            val request = if (accessToken.isNotEmpty()) {
                chain.request().putTokenHeader(accessToken)
            } else {
                chain.request()
            }
            chain.proceed(request)
        }
    }

    private fun Request.putTokenHeader(accessToken: String): Request {
        return this.newBuilder()
            .addHeader(AUTHORIZATION, accessToken)
            .build()
    }

    companion object {
        private const val AUTHORIZATION = "Authorization"
    }
}