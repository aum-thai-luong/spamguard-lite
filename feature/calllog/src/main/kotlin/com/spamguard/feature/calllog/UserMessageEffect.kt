package com.spamguard.feature.calllog

import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

/**
 * Hiện snackbar một lần cho [messageRes], theo cách của màn hình Danh sách chặn.
 * Dùng chung cho tab Cuộc gọi và màn chi tiết cuộc gọi.
 */
@Composable
internal fun UserMessageEffect(
    @StringRes messageRes: Int?,
    snackbarHostState: SnackbarHostState,
    onUserMessageShown: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    LaunchedEffect(messageRes) {
        if (messageRes == null) return@LaunchedEffect
        val message = context.getString(messageRes)
        onUserMessageShown()
        snackbarHostState.currentSnackbarData?.dismiss()
        // Scope riêng: effect này bị hủy ngay khi messageRes về null.
        scope.launch { snackbarHostState.showSnackbar(message) }
    }
}
