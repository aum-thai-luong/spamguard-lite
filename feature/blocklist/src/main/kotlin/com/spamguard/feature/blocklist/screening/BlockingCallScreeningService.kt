package com.spamguard.feature.blocklist.screening

import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import com.spamguard.core.domain.error.BlockRulesUnavailableException
import com.spamguard.core.domain.usecase.FindBlockingRuleUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

/**
 * Hệ thống gọi service này với mỗi cuộc gọi đến khi app giữ vai trò sàng lọc cuộc gọi
 * (`RoleManager.ROLE_CALL_SCREENING`, Android 10+). Cuộc gọi khớp quy tắc bị từ chối
 * nhưng vẫn ghi vào nhật ký, để tab Cuộc gọi hiện là "Bị chặn".
 *
 * Không ghi log số điện thoại.
 */
@AndroidEntryPoint
class BlockingCallScreeningService : CallScreeningService() {

    @Inject
    lateinit var findBlockingRule: FindBlockingRuleUseCase

    // Room tự chạy truy vấn ngoài main thread; respondToCall phải gọi trên main thread.
    private val scope = MainScope()

    override fun onScreenCall(callDetails: Call.Details) {
        val number = callDetails.handle?.schemeSpecificPart
        if (number.isNullOrBlank() || !callDetails.isIncoming()) {
            respondToCall(callDetails, ALLOW)
            return
        }
        scope.launch {
            respondToCall(callDetails, if (shouldReject(number)) REJECT else ALLOW)
        }
    }

    /**
     * Không đọc được quy tắc hoặc quá hạn (hệ thống chỉ chờ vài giây) thì cho cuộc gọi đi qua
     * thay vì chặn nhầm hay để app crash giữa cuộc gọi. Lỗi khác vẫn lộ ra.
     */
    private suspend fun shouldReject(number: String): Boolean = try {
        withTimeoutOrNull(LOOKUP_TIMEOUT_MILLIS) { findBlockingRule(number) } != null
    } catch (@Suppress("UNUSED_PARAMETER") e: BlockRulesUnavailableException) {
        false
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    /** Trước Android 10 không có hướng cuộc gọi; khi đó hệ thống chỉ gửi cuộc gọi đến. */
    private fun Call.Details.isIncoming(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
            callDirection == Call.Details.DIRECTION_INCOMING

    private companion object {
        const val LOOKUP_TIMEOUT_MILLIS = 3_000L

        val ALLOW: CallResponse = CallResponse.Builder().build()

        val REJECT: CallResponse = CallResponse.Builder()
            .setDisallowCall(true)
            .setRejectCall(true)
            .setSkipNotification(true)
            .setSkipCallLog(false)
            .build()
    }
}
