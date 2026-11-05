package com.spamguard.feature.calllog.detail

import androidx.annotation.StringRes

sealed interface CallDetailUiState {
    data object Loading : CallDetailUiState

    /** Cuộc gọi không còn trong nhật ký (ví dụ người dùng đã xóa trong app Điện thoại). */
    data object NotFound : CallDetailUiState

    data object Error : CallDetailUiState

    data class Content(
        val detail: CallDetailUiModel,
        /** Thông báo một lần (snackbar); UI gọi `onUserMessageShown()` sau khi hiển thị. */
        @StringRes val userMessageRes: Int? = null,
    ) : CallDetailUiState
}
