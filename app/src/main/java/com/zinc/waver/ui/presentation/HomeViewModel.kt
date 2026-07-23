package com.zinc.waver.ui.presentation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.zinc.datastore.login.PreferenceDataStoreModule
import com.zinc.domain.models.BillingCycle
import com.zinc.domain.models.SubscriptionStartRequest
import com.zinc.domain.usecases.more.CheckUserLimit
import com.zinc.domain.usecases.more.StartSubscription
import com.zinc.waver.ui.viewmodel.CommonViewModel
import com.zinc.waver.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val preferenceDataStoreModule: PreferenceDataStoreModule,
    private val checkUserLimitUseCase: CheckUserLimit,
    private val startSubscription: StartSubscription
) : CommonViewModel() {
    private val _logoutSucceed = SingleLiveEvent<Boolean>()
    val logoutSucceed: LiveData<Boolean> get() = _logoutSucceed

    private val _doNothing = SingleLiveEvent<Nothing>()

    // 이번 세션에 서버로 구독 시작을 이미 알린 subscribeId(purchaseToken) 목록
    private val notifiedSubscribeIds = mutableSetOf<String>()

    fun logout() {
        viewModelScope.launch {
            preferenceDataStoreModule.clearLoginEmail()
            _logoutSucceed.value = true
        }
    }

    // 로그인/앱 시작 시 구독 상태 확인 (판단은 각 화면에서 API 호출로 수행)
    fun checkUserLimit() {
        viewModelScope.launch(ceh(_doNothing, null)) {
            val response = checkUserLimitUseCase.invoke()
            Log.d("HomeViewModel", "checkUserLimit: ${response.data}")
        }
    }

    // 구독 완료(구매/앱 시작 시 활성 구독 감지) 시 서버에 구독 시작을 알린다.
    // 앱 시작 시 YEAR/MONTH 두 설정이 같은 구매를 매칭하므로, 세션당 같은 subscribeId는 한 번만 전송한다.
    fun notifySubscriptionStarted(billingCycle: BillingCycle, subscribeId: String) {
        if (!notifiedSubscribeIds.add(subscribeId)) return
        viewModelScope.launch(ceh(_doNothing, null)) {
            startSubscription(
                SubscriptionStartRequest(
                    billingCycle = billingCycle,
                    subscribeId = subscribeId
                )
            )
        }
    }
}
