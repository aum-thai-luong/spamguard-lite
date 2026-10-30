package com.spamguard.feature.calllog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spamguard.core.domain.error.CallLogUnavailableException
import com.spamguard.core.domain.usecase.GetClassifiedCallsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallLogViewModel @Inject constructor(
    private val getClassifiedCalls: GetClassifiedCallsUseCase,
    private val uiMapper: CallUiMapper,
) : ViewModel() {

    private val _uiState = MutableStateFlow<CallLogUiState>(CallLogUiState.PermissionRequired)
    val uiState: StateFlow<CallLogUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    fun onPermissionResult(granted: Boolean) {
        if (!granted) {
            _uiState.value = CallLogUiState.PermissionRequired
        }
        loadCalls()
    }

    fun onRetry() = loadCalls()

    private fun loadCalls() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = CallLogUiState.Loading
            getClassifiedCalls(MAX_CALLS)
                .map(uiMapper::toUiState)
                .catch { error ->
                    // Chỉ xử lý lỗi đã biết; lỗi lập trình phải lộ ra thay vì bị nuốt.
                    if (error !is CallLogUnavailableException) throw error
                    emit(CallLogUiState.Error)
                }
                .collect { state -> _uiState.value = state }
        }
    }

    private companion object {
        const val MAX_CALLS = 200
    }
}
