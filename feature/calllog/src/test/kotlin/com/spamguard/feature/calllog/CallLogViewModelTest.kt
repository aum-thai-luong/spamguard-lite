package com.spamguard.feature.calllog

import com.spamguard.core.domain.error.CallLogUnavailableException
import com.spamguard.core.domain.model.MatchType
import com.spamguard.core.domain.phone.PhoneNumberNormalizer
import com.spamguard.core.domain.spam.SpamClassifier
import com.spamguard.core.domain.usecase.GetClassifiedCallsUseCase
import com.spamguard.core.testing.FakeBlockRuleRepository
import com.spamguard.core.testing.FakeCallLogRepository
import com.spamguard.core.testing.MainDispatcherRule
import com.spamguard.core.testing.aCallRecord
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.ZoneOffset

class CallLogViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val callLogRepository = FakeCallLogRepository()
    private val blockRuleRepository = FakeBlockRuleRepository()

    private val viewModel = CallLogViewModel(
        getClassifiedCalls = GetClassifiedCallsUseCase(
            callLogRepository = callLogRepository,
            blockRuleRepository = blockRuleRepository,
            classifier = SpamClassifier(PhoneNumberNormalizer()),
        ),
        uiMapper = CallUiMapper(ZoneOffset.UTC),
    )

    @Test
    fun `starts by asking for call log permission`() {
        assertEquals(CallLogUiState.PermissionRequired, viewModel.uiState.value)
    }

    @Test
    fun `granted permission shows calls with blocked count`() = runTest {
        blockRuleRepository.addRule("0900", MatchType.PREFIX)
        callLogRepository.calls = listOf(
            aCallRecord(id = 1, number = "0900123456"),
            aCallRecord(id = 2, number = "0987654321"),
        )

        viewModel.onPermissionResult(granted = true)

        val state = viewModel.uiState.value as CallLogUiState.Content
        assertEquals(listOf(1L, 2L), state.calls.map { it.id })
        assertEquals(1, state.blockedCount)
    }

    @Test
    fun `granted permission with empty call log shows empty state`() {
        viewModel.onPermissionResult(granted = true)

        assertEquals(CallLogUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `unavailable call log shows error`() {
        callLogRepository.failure = CallLogUnavailableException("provider down")

        viewModel.onPermissionResult(granted = true)

        assertEquals(CallLogUiState.Error, viewModel.uiState.value)
    }

    @Test
    fun `retry after error loads calls`() {
        callLogRepository.failure = CallLogUnavailableException("provider down")
        viewModel.onPermissionResult(granted = true)
        callLogRepository.failure = null
        callLogRepository.calls = listOf(aCallRecord())

        viewModel.onRetry()

        assertTrue(viewModel.uiState.value is CallLogUiState.Content)
    }
}
