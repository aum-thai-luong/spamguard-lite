package com.spamguard.feature.calllog

import com.spamguard.feature.calllog.model.CallUiModel

sealed interface CallLogUiState {
    /** Chưa có quyền READ_CALL_LOG. */
    data object PermissionRequired : CallLogUiState

    /** Hệ thống không còn hiện hộp thoại xin quyền; người dùng chỉ bật lại được trong Cài đặt. */
    data object PermissionDeniedPermanently : CallLogUiState

    data object Loading : CallLogUiState

    /** [isRefreshing] bật khi người dùng kéo để làm mới. */
    data class Empty(
        val isRefreshing: Boolean = false,
    ) : CallLogUiState

    data object Error : CallLogUiState

    data class Content(
        val calls: List<CallUiModel>,
        val blockedCount: Int,
        val isRefreshing: Boolean = false,
    ) : CallLogUiState
}
