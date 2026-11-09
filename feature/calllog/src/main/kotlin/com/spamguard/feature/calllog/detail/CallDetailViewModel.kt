package com.spamguard.feature.calllog.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spamguard.core.domain.error.CallLogUnavailableException
import com.spamguard.core.domain.model.MatchType
import com.spamguard.core.domain.usecase.AddBlockRuleResult
import com.spamguard.core.domain.usecase.AddBlockRuleUseCase
import com.spamguard.core.domain.usecase.GetCallDetailUseCase
import com.spamguard.core.domain.usecase.RemoveBlockRuleUseCase
import com.spamguard.feature.calllog.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * [callId] lấy từ `CallDetailKey` của back stack Navigation 3 qua assisted injection;
 * back stack được lưu lại nên id còn nguyên sau khi hệ thống dừng process.
 */
@HiltViewModel(assistedFactory = CallDetailViewModel.Factory::class)
class CallDetailViewModel @AssistedInject constructor(
    @Assisted private val callId: Long,
    private val getCallDetail: GetCallDetailUseCase,
    private val addBlockRule: AddBlockRuleUseCase,
    private val removeBlockRule: RemoveBlockRuleUseCase,
    private val uiMapper: CallDetailUiMapper,
) : ViewModel() {

    /** Tăng lên để đọc lại nhật ký cuộc gọi (nút "Thử lại"). */
    private val reloadTrigger = MutableStateFlow(0)
    private val userMessage = MutableStateFlow<Int?>(null)

    /** Hộp thoại bỏ chặn đang mở; quy tắc cần bỏ lấy từ chi tiết hiện tại, không từ UI. */
    private val isUnblockDialogVisible = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<CallDetailUiState> =
        combine(
            reloadTrigger.flatMapLatest { loadDetail() },
            userMessage,
            isUnblockDialogVisible,
        ) { state, messageRes, unblockDialogVisible ->
            if (state is CallDetailUiState.Content) {
                state.copy(
                    userMessageRes = messageRes,
                    // Số đã được bỏ chặn ở nơi khác thì hộp thoại tự đóng.
                    pendingUnblock = state.detail.blockingRule.takeIf { unblockDialogVisible },
                )
            } else {
                state
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = CallDetailUiState.Loading,
        )

    fun onRetry() {
        reloadTrigger.update { it + 1 }
    }

    /** Thêm quy tắc chặn đúng số; chuẩn hóa và kiểm tra trùng nằm trong [AddBlockRuleUseCase]. */
    fun onBlockNumber() {
        val number = currentDetail()?.takeIf { it.canBlock }?.number ?: return
        viewModelScope.launch {
            userMessage.value = when (addBlockRule(number, MatchType.EXACT)) {
                is AddBlockRuleResult.Added -> R.string.call_log_detail_message_blocked
                AddBlockRuleResult.Duplicate -> R.string.call_log_detail_message_duplicate
                AddBlockRuleResult.InvalidPattern -> R.string.call_log_detail_message_invalid
            }
        }
    }

    /** Mở hộp thoại hỏi lại trước khi bỏ chặn quy tắc đang chặn số này. */
    fun onUnblockClick() {
        isUnblockDialogVisible.value = true
    }

    fun onUnblockDismissed() {
        isUnblockDialogVisible.value = false
    }

    /** Người dùng đã xác nhận trong hộp thoại bỏ chặn. */
    fun onUnblockConfirmed() {
        val ruleId = currentDetail()?.blockingRule?.ruleId
        isUnblockDialogVisible.value = false
        if (ruleId == null) return
        viewModelScope.launch {
            removeBlockRule(ruleId)
            userMessage.value = R.string.call_log_detail_message_unblocked
        }
    }

    /** Route chỉ gọi trên Android 12 trở xuống; từ Android 13 hệ thống tự báo đã sao chép. */
    fun onNumberCopied() {
        userMessage.value = R.string.call_log_detail_message_copied
    }

    fun onDialerUnavailable() {
        userMessage.value = R.string.call_log_detail_message_no_dialer
    }

    fun onUserMessageShown() {
        userMessage.value = null
    }

    private fun loadDetail(): Flow<CallDetailUiState> =
        getCallDetail(callId)
            .map { call ->
                call?.let { CallDetailUiState.Content(uiMapper.toUiModel(it)) } ?: CallDetailUiState.NotFound
            }
            .onStart { emit(CallDetailUiState.Loading) }
            .catch { error ->
                // Chỉ xử lý lỗi đã biết; lỗi lập trình phải lộ ra thay vì bị nuốt.
                if (error !is CallLogUnavailableException) throw error
                emit(CallDetailUiState.Error)
            }

    private fun currentDetail(): CallDetailUiModel? =
        (uiState.value as? CallDetailUiState.Content)?.detail

    @AssistedFactory
    interface Factory {
        fun create(callId: Long): CallDetailViewModel
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
