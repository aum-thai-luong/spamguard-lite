package com.spamguard.feature.blocklist

import androidx.annotation.StringRes
import com.spamguard.feature.blocklist.model.BlockRuleUiModel

data class BlocklistUiState(
    val isLoading: Boolean = true,
    val rules: List<BlockRuleUiModel> = emptyList(),
    val screeningStatus: ScreeningStatus = ScreeningStatus.Unsupported,
    /** Thông báo một lần (snackbar); UI gọi `onUserMessageShown()` sau khi hiển thị. */
    @StringRes val userMessageRes: Int? = null,
) {
    val isEmpty: Boolean get() = !isLoading && rules.isEmpty()
}

/** App có đang tự từ chối cuộc gọi khớp quy tắc hay không (vai trò sàng lọc cuộc gọi). */
enum class ScreeningStatus {
    /** Android 9 trở xuống hoặc máy không có vai trò này: chỉ đánh dấu, không chặn. */
    Unsupported,

    /** Có thể bật nhưng người dùng chưa cấp vai trò. */
    Disabled,

    /** App đang giữ vai trò và từ chối cuộc gọi khớp quy tắc. */
    Enabled,
}
