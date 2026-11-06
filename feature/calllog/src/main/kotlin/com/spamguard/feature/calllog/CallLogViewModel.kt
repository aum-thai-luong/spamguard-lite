package com.spamguard.feature.calllog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spamguard.core.domain.error.CallLogUnavailableException
import com.spamguard.core.domain.model.MatchType
import com.spamguard.core.domain.usecase.AddBlockRuleResult
import com.spamguard.core.domain.usecase.AddBlockRuleUseCase
import com.spamguard.core.domain.usecase.GetClassifiedCallsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallLogViewModel @Inject constructor(
    private val getClassifiedCalls: GetClassifiedCallsUseCase,
    private val addBlockRule: AddBlockRuleUseCase,
    private val uiMapper: CallUiMapper,
) : ViewModel() {

    /** Trạng thái danh sách; thông báo một lần nằm riêng để không mất khi danh sách phát lại. */
    private val listState = MutableStateFlow<CallLogUiState>(CallLogUiState.PermissionRequired)
    private val userMessage = MutableStateFlow<Int?>(null)

    // Eagerly: trạng thái luôn cập nhật kể cả khi chưa có collector, như khi còn là MutableStateFlow.
    val uiState: StateFlow<CallLogUiState> =
        combine(listState, userMessage) { state, messageRes ->
            if (state is CallLogUiState.Content) state.copy(userMessageRes = messageRes) else state
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = listState.value,
        )

    private var loadJob: Job? = null

    /** [canAskAgain] là `false` khi hệ thống sẽ không hiện hộp thoại xin quyền nữa. */
    fun onPermissionResult(granted: Boolean, canAskAgain: Boolean = true) {
        if (!granted) {
            listState.value = if (canAskAgain) {
                CallLogUiState.PermissionRequired
            } else {
                CallLogUiState.PermissionDeniedPermanently
            }
            return
        }
        loadCalls()
    }

    /**
     * Màn hình hiện lên khi app đã có quyền: mở lại sau khi hệ thống dừng process, hoặc vừa
     * bật quyền trong Cài đặt. Chỉ đọc khi chưa đọc, để xoay màn hình hay đổi tab không đọc lại.
     */
    fun onPermissionAvailable() {
        when (listState.value) {
            CallLogUiState.PermissionRequired,
            CallLogUiState.PermissionDeniedPermanently,
            -> loadCalls()

            else -> Unit
        }
    }

    fun onRetry() = loadCalls()

    /** Kéo để làm mới: giữ danh sách đang hiển thị trong lúc đọc lại nhật ký cuộc gọi. */
    fun onRefresh() {
        val refreshing = when (val state = listState.value) {
            is CallLogUiState.Content -> state.copy(isRefreshing = true)
            is CallLogUiState.Empty -> state.copy(isRefreshing = true)
            else -> return
        }
        listState.value = refreshing
        loadCalls(showLoading = false)
    }

    /**
     * Người dùng đã xác nhận chặn [number] trong hộp thoại (nhấn giữ một cuộc gọi).
     * Chuẩn hóa và kiểm tra trùng nằm trong [AddBlockRuleUseCase]; số ẩn (rỗng) bị bỏ qua.
     */
    fun onBlockConfirmed(number: String) {
        if (number.isBlank()) return
        viewModelScope.launch {
            userMessage.value = when (addBlockRule(number, MatchType.EXACT)) {
                is AddBlockRuleResult.Added -> R.string.call_log_detail_message_blocked
                AddBlockRuleResult.Duplicate -> R.string.call_log_detail_message_duplicate
                AddBlockRuleResult.InvalidPattern -> R.string.call_log_detail_message_invalid
            }
        }
    }

    fun onUserMessageShown() {
        userMessage.value = null
    }

    private fun loadCalls(showLoading: Boolean = true) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            if (showLoading) listState.value = CallLogUiState.Loading
            getClassifiedCalls(MAX_CALLS)
                .map(uiMapper::toUiState)
                .catch { error ->
                    // Chỉ xử lý lỗi đã biết; lỗi lập trình phải lộ ra thay vì bị nuốt.
                    if (error !is CallLogUnavailableException) throw error
                    emit(CallLogUiState.Error)
                }
                .collect { state -> listState.value = state }
        }
    }

    private companion object {
        const val MAX_CALLS = 200
    }
}
