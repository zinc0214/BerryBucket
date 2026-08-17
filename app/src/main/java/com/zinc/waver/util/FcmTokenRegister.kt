package com.zinc.waver.util

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.zinc.datastore.login.PreferenceDataStoreModule
import com.zinc.domain.usecases.login.UpdateFcmToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 푸시 알림 수신용 FCM 토큰을 서버에 등록한다.
 *
 * 로그인 직후와 FCM 토큰 갱신 시점에 호출된다.
 * 전송 실패는 삼킨다. 푸시는 부가 기능이므로 로그인 흐름을 막아서는 안 된다.
 */
@Singleton
class FcmTokenRegister @Inject constructor(
    private val updateFcmToken: UpdateFcmToken,
    private val preferenceDataStoreModule: PreferenceDataStoreModule,
) {
    // viewModelScope 를 쓰면 로그인 직후 화면 전환으로 ViewModel 이 clear 될 때 요청이 취소된다.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * @param token 이미 알고 있는 FCM 토큰. null 이면 FirebaseMessaging 에서 조회한다.
     */
    fun register(token: String? = null) {
        scope.launch {
            runCatching {
                // onNewToken 은 미로그인 상태에서도 발화한다. 인증 없는 요청을 걸러낸다.
                if (preferenceDataStoreModule.loadAccessToken.first().isEmpty()) {
                    Log.d(TAG, "미로그인 상태이므로 FCM 토큰을 전송하지 않는다")
                    return@launch
                }
                val res = updateFcmToken(token ?: fetchFcmToken())
                Log.d(TAG, "FCM 토큰 전송 : success=${res.success}, code=${res.code}")
            }.onFailure {
                Log.e(TAG, "FCM 토큰 전송 실패", it)
            }
        }
    }

    private suspend fun fetchFcmToken(): String = suspendCancellableCoroutine { cont ->
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { cont.resume(it) }
            .addOnFailureListener { cont.resumeWithException(it) }
    }

    companion object {
        private const val TAG = "FCM_TOKEN"
    }
}
