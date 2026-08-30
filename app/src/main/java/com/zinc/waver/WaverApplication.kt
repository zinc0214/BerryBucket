package com.zinc.waver

import android.app.Application
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WaverApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Firebase 초기화
        try {
            // Firebase Messaging 토큰 얻기
            // 토큰 값은 로그로 남기지 않는다. 서버 등록은 FcmTokenRegister 가 담당한다.
            Firebase.messaging.token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.e("FCM_INIT", "Failed to get FCM token", task.exception)
                }
            }
        } catch (e: Exception) {
            Log.e("FCM_INIT", "Error initializing Firebase", e)
        }
    }
}