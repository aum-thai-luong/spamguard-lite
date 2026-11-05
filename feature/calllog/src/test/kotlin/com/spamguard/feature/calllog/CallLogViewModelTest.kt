package com.spamguard.feature.calllog

import com.spamguard.core.domain.error.CallLogUnavailableException
import com.spamguard.core.domain.model.CallRecord
import com.spamguard.core.domain.model.MatchType
import com.spamguard.core.domain.phone.PhoneNumberNormalizer
import com.spamguard.core.domain.repository.CallLogRepository
import com.spamguard.core.domain.spam.SpamClassifier
import com.spamguard.core.domain.usecase.GetClassifiedCallsUseCase
import com.spamguard.core.testing.FakeBlockRuleRepository
import com.spamguard.core.testing.FakeCallLogRepository
import com.spamguard.core.testing.MainDispatcherRule
import com.spamguard.core.testing.aCallRecord
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.ZoneOffset

class CallLogViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val callLogRepository = FakeCallLogRepository()
    private val blockRuleRepository = FakeBlockRuleRepository()

    private val viewModel = createViewModel(callLogRepository)

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

        assertEquals(CallLogUiState.Empty(), viewModel.uiState.value)
    }

    @Test
    fun `denied permission shows rationale without reading call log`() = runTest {
        // Provider thật ném SecurityException khi app chưa có READ_CALL_LOG.
        callLogRepository.failure = SecurityException("Permission Denial: requires android.permission.READ_CALL_LOG")

        viewModel.onPermissionResult(granted = false)

        assertEquals(CallLogUiState.PermissionRequired, viewModel.uiState.value)
    }

    @Test
    fun `permanently denied permission offers settings without reading call log`() {
        callLogRepository.failure = SecurityException("Permission Denial: requires android.permission.READ_CALL_LOG")

        viewModel.onPermissionResult(granted = false, canAskAgain = false)

        assertEquals(CallLogUiState.PermissionDeniedPermanently, viewModel.uiState.value)
    }

    @Test
    fun `available permission loads calls when nothing was read yet`() {
        // Ví dụ: hệ thống dừng process, ViewModel mới bắt đầu ở PermissionRequired dù quyền vẫn còn.
        callLogRepository.calls = listOf(aCallRecord(id = 1))

        viewModel.onPermissionAvailable()

        val state = viewModel.uiState.value as CallLogUiState.Content
        assertEquals(listOf(1L), state.calls.map { it.id })
    }

    @Test
    fun `permission enabled in settings loads calls`() {
        viewModel.onPermissionResult(granted = false, canAskAgain = false)
        callLogRepository.calls = listOf(aCallRecord(id = 1))

        viewModel.onPermissionAvailable()

        assertTrue(viewModel.uiState.value is CallLogUiState.Content)
    }

    @Test
    fun `available permission does not reread calls already shown`() {
        callLogRepository.calls = listOf(aCallRecord(id = 1))
        viewModel.onPermissionResult(granted = true)
        callLogRepository.calls = listOf(aCallRecord(id = 2))

        viewModel.onPermissionAvailable()

        val state = viewModel.uiState.value as CallLogUiState.Content
        assertEquals(listOf(1L), state.calls.map { it.id })
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

    @Test
    fun `refresh reloads calls and clears refreshing flag`() {
        callLogRepository.calls = listOf(aCallRecord(id = 1))
        viewModel.onPermissionResult(granted = true)
        callLogRepository.calls = listOf(aCallRecord(id = 2), aCallRecord(id = 1))

        viewModel.onRefresh()

        val state = viewModel.uiState.value as CallLogUiState.Content
        assertEquals(listOf(2L, 1L), state.calls.map { it.id })
        assertFalse(state.isRefreshing)
    }

    @Test
    fun `refresh keeps current calls visible while reading`() {
        val gatedRepository = GatedCallLogRepository(calls = listOf(aCallRecord(id = 1)))
        val viewModel = createViewModel(gatedRepository)
        viewModel.onPermissionResult(granted = true)
        val gate = CompletableDeferred<Unit>()
        gatedRepository.gate = gate

        viewModel.onRefresh()

        val refreshing = viewModel.uiState.value as CallLogUiState.Content
        assertTrue(refreshing.isRefreshing)
        assertEquals(listOf(1L), refreshing.calls.map { it.id })

        gate.complete(Unit)

        assertFalse((viewModel.uiState.value as CallLogUiState.Content).isRefreshing)
    }

    @Test
    fun `refresh from empty state shows new calls`() {
        viewModel.onPermissionResult(granted = true)
        callLogRepository.calls = listOf(aCallRecord())

        viewModel.onRefresh()

        assertTrue(viewModel.uiState.value is CallLogUiState.Content)
    }

    @Test
    fun `refresh failure shows error`() {
        callLogRepository.calls = listOf(aCallRecord())
        viewModel.onPermissionResult(granted = true)
        callLogRepository.failure = CallLogUnavailableException("provider down")

        viewModel.onRefresh()

        assertEquals(CallLogUiState.Error, viewModel.uiState.value)
    }

    @Test
    fun `refresh is ignored before permission is granted`() {
        callLogRepository.calls = listOf(aCallRecord())

        viewModel.onRefresh()

        assertEquals(CallLogUiState.PermissionRequired, viewModel.uiState.value)
    }

    private fun createViewModel(callLogRepository: CallLogRepository) = CallLogViewModel(
        getClassifiedCalls = GetClassifiedCallsUseCase(
            callLogRepository = callLogRepository,
            blockRuleRepository = blockRuleRepository,
            classifier = SpamClassifier(PhoneNumberNormalizer()),
        ),
        uiMapper = CallUiMapper(ZoneOffset.UTC),
    )

    /** Repository chỉ trả kết quả khi [gate] hoàn tất, để quan sát trạng thái đang làm mới. */
    private class GatedCallLogRepository(
        private val calls: List<CallRecord>,
    ) : CallLogRepository {
        var gate: CompletableDeferred<Unit>? = null

        override suspend fun getRecentCalls(limit: Int): List<CallRecord> {
            gate?.await()
            return calls.take(limit)
        }

        override suspend fun getCall(id: Long): CallRecord? = calls.find { it.id == id }
    }
}
