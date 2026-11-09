package com.spamguard.feature.blocklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spamguard.core.domain.model.MatchType
import com.spamguard.core.domain.usecase.AddBlockRuleResult
import com.spamguard.core.domain.usecase.AddBlockRuleUseCase
import com.spamguard.core.domain.usecase.ObserveBlockRulesUseCase
import com.spamguard.core.domain.usecase.RemoveBlockRuleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BlocklistViewModel @Inject constructor(
    observeBlockRules: ObserveBlockRulesUseCase,
    private val addBlockRule: AddBlockRuleUseCase,
    private val removeBlockRule: RemoveBlockRuleUseCase,
    private val uiMapper: BlockRuleUiMapper,
) : ViewModel() {

    private val userMessage = MutableStateFlow<Int?>(null)
    private val screeningStatus = MutableStateFlow(ScreeningStatus.Unsupported)

    /**
     * Hộp thoại nằm trong ViewModel nên còn nguyên khi xoay màn hình. Hộp thoại xóa chỉ giữ id:
     * id không rời ViewModel cho tới khi người dùng xác nhận (regression BUG-1).
     */
    private val dialogs = MutableStateFlow(Dialogs())

    val uiState: StateFlow<BlocklistUiState> =
        combine(observeBlockRules(), screeningStatus, userMessage, dialogs) { rules, status, messageRes, dialogs ->
            val ruleModels = rules.map(uiMapper::toUiModel)
            BlocklistUiState(
                isLoading = false,
                rules = ruleModels,
                screeningStatus = status,
                userMessageRes = messageRes,
                isAddDialogVisible = dialogs.isAddVisible,
                // Quy tắc đã bị xóa ở nơi khác thì hộp thoại tự đóng.
                pendingDeletion = ruleModels.find { it.id == dialogs.pendingDeletionId },
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = BlocklistUiState(),
        )

    fun onAddClick() {
        dialogs.update { it.copy(isAddVisible = true) }
    }

    fun onAddDialogDismissed() {
        dialogs.update { it.copy(isAddVisible = false) }
    }

    fun onAddRule(rawPattern: String, matchType: MatchType) {
        dialogs.update { it.copy(isAddVisible = false) }
        viewModelScope.launch {
            userMessage.value = when (addBlockRule(rawPattern, matchType)) {
                is AddBlockRuleResult.Added -> R.string.blocklist_message_added
                AddBlockRuleResult.Duplicate -> R.string.blocklist_message_duplicate
                AddBlockRuleResult.InvalidPattern -> R.string.blocklist_message_invalid
            }
        }
    }

    /** Mở hộp thoại hỏi lại trước khi xóa [ruleId]. */
    fun onDeleteClick(ruleId: Long) {
        dialogs.update { it.copy(pendingDeletionId = ruleId) }
    }

    fun onDeleteDismissed() {
        dialogs.update { it.copy(pendingDeletionId = null) }
    }

    /** Người dùng xác nhận trong hộp thoại xóa; id lấy từ chính ViewModel, không từ UI. */
    fun onDeleteConfirmed() {
        val ruleId = dialogs.value.pendingDeletionId ?: return
        dialogs.update { it.copy(pendingDeletionId = null) }
        viewModelScope.launch {
            removeBlockRule(ruleId)
            userMessage.value = R.string.blocklist_message_deleted
        }
    }

    /** Route đọc vai trò sàng lọc từ hệ thống (mỗi lần màn hình resume) và báo vào đây. */
    fun onScreeningStatusChecked(status: ScreeningStatus) {
        screeningStatus.value = status
    }

    fun onUserMessageShown() {
        userMessage.value = null
    }

    private data class Dialogs(
        val isAddVisible: Boolean = false,
        val pendingDeletionId: Long? = null,
    )

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
