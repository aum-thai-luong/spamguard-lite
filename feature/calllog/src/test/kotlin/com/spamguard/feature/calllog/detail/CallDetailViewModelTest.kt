package com.spamguard.feature.calllog.detail

import androidx.lifecycle.SavedStateHandle
import com.spamguard.core.domain.error.CallLogUnavailableException
import com.spamguard.core.domain.model.MatchType
import com.spamguard.core.domain.phone.PhoneNumberNormalizer
import com.spamguard.core.domain.spam.SpamClassifier
import com.spamguard.core.domain.usecase.AddBlockRuleUseCase
import com.spamguard.core.domain.usecase.GetCallDetailUseCase
import com.spamguard.core.domain.usecase.RemoveBlockRuleUseCase
import com.spamguard.core.testing.FakeBlockRuleRepository
import com.spamguard.core.testing.FakeCallLogRepository
import com.spamguard.core.testing.MainDispatcherRule
import com.spamguard.core.testing.aCallRecord
import com.spamguard.feature.calllog.R
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.ZoneOffset

class CallDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val callLogRepository = FakeCallLogRepository()
    private val blockRuleRepository = FakeBlockRuleRepository()

    @Test
    fun `reads the call id from navigation arguments`() = runTest {
        callLogRepository.calls = listOf(aCallRecord(id = 1, number = "0911111111"), aCallRecord(id = 7, number = "0977777777"))
        val viewModel = createViewModel(callId = 7)
        collectUiState(viewModel)

        assertEquals("0977777777", viewModel.content().detail.number)
    }

    @Test
    fun `missing call shows not found`() = runTest {
        val viewModel = createViewModel(callId = 99)
        collectUiState(viewModel)

        assertEquals(CallDetailUiState.NotFound, viewModel.uiState.value)
    }

    @Test
    fun `unreadable call log shows error, retry loads again`() = runTest {
        callLogRepository.calls = listOf(aCallRecord(id = 7))
        callLogRepository.failure = CallLogUnavailableException("no cursor")
        val viewModel = createViewModel(callId = 7)
        collectUiState(viewModel)
        assertEquals(CallDetailUiState.Error, viewModel.uiState.value)

        callLogRepository.failure = null
        viewModel.onRetry()

        assertTrue(viewModel.uiState.value is CallDetailUiState.Content)
    }

    @Test
    fun `blocking the number adds a normalized exact rule and shows it as blocked`() = runTest {
        callLogRepository.calls = listOf(aCallRecord(id = 7, number = "+84 900 123 456"))
        val viewModel = createViewModel(callId = 7)
        collectUiState(viewModel)

        viewModel.onBlockNumber()

        val state = viewModel.content()
        assertEquals(R.string.call_log_detail_message_blocked, state.userMessageRes)
        assertEquals("0900123456", state.detail.blockingRule?.pattern)
        assertTrue(blockRuleRepository.exists("0900123456", MatchType.EXACT))
    }

    @Test
    fun `already blocked number is not blocked again`() = runTest {
        callLogRepository.calls = listOf(aCallRecord(id = 7, number = "0900123456"))
        blockRuleRepository.addRule("0900", MatchType.PREFIX)
        val viewModel = createViewModel(callId = 7)
        collectUiState(viewModel)

        viewModel.onBlockNumber()

        assertNull(viewModel.content().userMessageRes)
        assertEquals(1, blockRuleRepository.observeRules().first().size)
    }

    @Test
    fun `hidden number cannot be blocked`() = runTest {
        callLogRepository.calls = listOf(aCallRecord(id = 7, number = ""))
        val viewModel = createViewModel(callId = 7)
        collectUiState(viewModel)

        viewModel.onBlockNumber()

        assertNull(viewModel.content().userMessageRes)
        assertTrue(blockRuleRepository.observeRules().first().isEmpty())
    }

    @Test
    fun `unblocking removes the rule and confirms`() = runTest {
        callLogRepository.calls = listOf(aCallRecord(id = 7, number = "0900123456"))
        val ruleId = blockRuleRepository.addRule("0900", MatchType.PREFIX)
        val viewModel = createViewModel(callId = 7)
        collectUiState(viewModel)
        assertEquals(ruleId, viewModel.content().detail.blockingRule?.ruleId)

        viewModel.onUnblockConfirmed(ruleId)

        val state = viewModel.content()
        assertNull(state.detail.blockingRule)
        assertEquals(R.string.call_log_detail_message_unblocked, state.userMessageRes)
    }

    @Test
    fun `shown message is cleared`() = runTest {
        callLogRepository.calls = listOf(aCallRecord(id = 7))
        val viewModel = createViewModel(callId = 7)
        collectUiState(viewModel)

        viewModel.onNumberCopied()
        assertEquals(R.string.call_log_detail_message_copied, viewModel.content().userMessageRes)
        viewModel.onUserMessageShown()

        assertNull(viewModel.content().userMessageRes)
    }

    private fun createViewModel(callId: Long): CallDetailViewModel {
        val normalizer = PhoneNumberNormalizer()
        return CallDetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf(CALL_ID_ARG to callId)),
            getCallDetail = GetCallDetailUseCase(callLogRepository, blockRuleRepository, SpamClassifier(normalizer)),
            addBlockRule = AddBlockRuleUseCase(blockRuleRepository, normalizer),
            removeBlockRule = RemoveBlockRuleUseCase(blockRuleRepository),
            uiMapper = CallDetailUiMapper(ZoneOffset.UTC),
        )
    }

    /** `uiState` dùng `WhileSubscribed`: phải có collector thì mới đọc dữ liệu. */
    private fun TestScope.collectUiState(viewModel: CallDetailViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
    }

    private fun CallDetailViewModel.content() = uiState.value as CallDetailUiState.Content
}
