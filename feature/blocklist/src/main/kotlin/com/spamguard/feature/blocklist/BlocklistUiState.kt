package com.spamguard.feature.blocklist

import androidx.annotation.StringRes
import com.spamguard.feature.blocklist.model.BlockRuleUiModel

data class BlocklistUiState(
    val isLoading: Boolean = true,
    val rules: List<BlockRuleUiModel> = emptyList(),
    /** Thông báo một lần (snackbar); UI gọi `onUserMessageShown()` sau khi hiển thị. */
    @StringRes val userMessageRes: Int? = null,
) {
    val isEmpty: Boolean get() = !isLoading && rules.isEmpty()
}
