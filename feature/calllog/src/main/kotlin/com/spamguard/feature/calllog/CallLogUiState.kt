package com.spamguard.feature.calllog

import com.spamguard.feature.calllog.model.CallUiModel

sealed interface CallLogUiState {
    /** Chưa có quyền READ_CALL_LOG. */
    data object PermissionRequired : CallLogUiState

    data object Loading : CallLogUiState

    data object Empty : CallLogUiState

    data object Error : CallLogUiState

    data class Content(
        val calls: List<CallUiModel>,
        val blockedCount: Int,
    ) : CallLogUiState
}
