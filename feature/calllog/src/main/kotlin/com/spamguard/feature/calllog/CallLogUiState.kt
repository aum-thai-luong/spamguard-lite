package com.spamguard.feature.calllog

import androidx.annotation.StringRes
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
        /** Thông báo một lần (snackbar); UI gọi `onUserMessageShown()` sau khi hiển thị. */
        @StringRes val userMessageRes: Int? = null,
        /** Số đang chờ người dùng xác nhận chặn; `null` khi không có hộp thoại chặn. */
        val pendingBlockNumber: String? = null,
    ) : CallLogUiState
}
