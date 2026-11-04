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

    val uiState: StateFlow<BlocklistUiState> =
        combine(observeBlockRules(), screeningStatus, userMessage) { rules, status, messageRes ->
            BlocklistUiState(
                isLoading = false,
                rules = rules.map(uiMapper::toUiModel),
                screeningStatus = status,
                userMessageRes = messageRes,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = BlocklistUiState(),
        )

    fun onAddRule(rawPattern: String, matchType: MatchType) {
        viewModelScope.launch {
            userMessage.value = when (addBlockRule(rawPattern, matchType)) {
                is AddBlockRuleResult.Added -> R.string.blocklist_message_added
                AddBlockRuleResult.Duplicate -> R.string.blocklist_message_duplicate
                AddBlockRuleResult.InvalidPattern -> R.string.blocklist_message_invalid
            }
        }
    }

    fun onDeleteRule(ruleId: Long) {
        viewModelScope.launch {
            removeBlockRule(ruleId)
            userMessage.value = R.string.blocklist_message_deleted
        }
    }

    /** Fragment đọc vai trò sàng lọc từ hệ thống (mỗi lần quay lại màn hình) và báo vào đây. */
    fun onScreeningStatusChecked(status: ScreeningStatus) {
        screeningStatus.value = status
    }

    fun onUserMessageShown() {
        userMessage.value = null
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
