package com.spamguard.feature.blocklist.screening

import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
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
            // Hệ thống chỉ chờ vài giây; quá hạn thì cho cuộc gọi đi qua thay vì chặn nhầm.
            val rule = withTimeoutOrNull(LOOKUP_TIMEOUT_MILLIS) { findBlockingRule(number) }
            respondToCall(callDetails, if (rule != null) REJECT else ALLOW)
        }
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
