package com.spamguard.feature.calllog

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.spamguard.feature.calllog.detail.CallDetailRoute
import com.spamguard.feature.calllog.navigation.CallDetailKey
import com.spamguard.feature.calllog.navigation.CallLogKey

/**
 * Đăng ký màn hình của :feature:calllog vào `entryProvider` của `NavDisplay` trong :app.
 *
 * @param onNavigate đẩy một key lên back stack.
 * @param onBack bỏ màn hình trên cùng khỏi back stack.
 */
fun EntryProviderScope<NavKey>.callLogEntries(
    onNavigate: (NavKey) -> Unit,
    onBack: () -> Unit,
) {
    entry<CallLogKey> {
        CallLogRoute(onCallClick = { callId -> onNavigate(CallDetailKey(callId)) })
    }
    entry<CallDetailKey> { key ->
        CallDetailRoute(callId = key.callId, onPermissionLost = onBack)
    }
}
