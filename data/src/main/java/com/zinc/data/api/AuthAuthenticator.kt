package com.zinc.data.api

import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

/**
 * 401 을 받았을 때의 처리.
 *
 * 토큰 갱신은 아직 구현돼 있지 않다. 로그인 응답(LoadTokenByEmailResponse)이 accessToken 만 주고
 * refreshToken 을 주지 않아 [com.zinc.datastore.login.PreferenceDataStoreModule.setRefreshToken] 은
 * 한 번도 호출되지 않는다. 갱신을 붙이려면 서버가 refreshToken 을 내려주는 것이 먼저다.
 *
 * 그때까지는 null 을 돌려주어 401 을 그대로 호출부에 전달한다. 같은 요청을 되돌려주면 OkHttp 가
 * 동일한 요청을 follow-up 한도(20회)까지 재시도해, 실패 한 번에 401 요청이 20번 나간다.
 */
class AuthAuthenticator @Inject constructor() : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? = null
}
