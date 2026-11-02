package com.spamguard.feature.blocklist

import com.spamguard.core.domain.model.MatchType
import com.spamguard.core.domain.phone.PhoneNumberNormalizer
import com.spamguard.core.domain.usecase.AddBlockRuleUseCase
import com.spamguard.core.domain.usecase.ObserveBlockRulesUseCase
import com.spamguard.core.domain.usecase.RemoveBlockRuleUseCase
import com.spamguard.core.testing.FakeBlockRuleRepository
import com.spamguard.core.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class BlocklistViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeBlockRuleRepository()
    private val normalizer = PhoneNumberNormalizer()

    /**
     * Tạo trễ: constructor gọi `stateIn(viewModelScope)` nên phải chạy sau khi
     * [MainDispatcherRule] đã `setMain`, nếu không scope giữ Main dispatcher thật (không có trên JVM).
     */
    private val viewModel by lazy {
        BlocklistViewModel(
            observeBlockRules = ObserveBlockRulesUseCase(repository),
            addBlockRule = AddBlockRuleUseCase(repository, normalizer),
            removeBlockRule = RemoveBlockRuleUseCase(repository),
            uiMapper = BlockRuleUiMapper(ZoneOffset.UTC),
        )
    }

    @Test
    fun `starts loading until rules arrive`() {
        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `empty repository shows empty state`() = runTest {
        collectUiState()

        assertTrue(viewModel.uiState.value.isEmpty)
    }

    @Test
    fun `adding a valid rule lists it and confirms`() = runTest {
        collectUiState()

        viewModel.onAddRule("+84 900", MatchType.PREFIX)

        val state = viewModel.uiState.value
        assertEquals(listOf("0900"), state.rules.map { it.pattern })
        assertEquals(R.string.blocklist_message_added, state.userMessageRes)
    }

    @Test
    fun `adding a duplicate rule shows duplicate message`() = runTest {
        collectUiState()
        viewModel.onAddRule("0900", MatchType.PREFIX)

        viewModel.onAddRule("0900", MatchType.PREFIX)

        assertEquals(1, viewModel.uiState.value.rules.size)
        assertEquals(R.string.blocklist_message_duplicate, viewModel.uiState.value.userMessageRes)
    }

    @Test
    fun `adding an invalid rule shows invalid message`() = runTest {
        collectUiState()

        viewModel.onAddRule("abc", MatchType.EXACT)

        assertTrue(viewModel.uiState.value.rules.isEmpty())
        assertEquals(R.string.blocklist_message_invalid, viewModel.uiState.value.userMessageRes)
    }

    @Test
    fun `deleting a rule removes it`() = runTest {
        collectUiState()
        val ruleId = repository.addRule("0900", MatchType.PREFIX)

        viewModel.onDeleteRule(ruleId)

        assertTrue(viewModel.uiState.value.rules.isEmpty())
        assertEquals(R.string.blocklist_message_deleted, viewModel.uiState.value.userMessageRes)
    }

    @Test
    fun `message is cleared once shown`() = runTest {
        collectUiState()
        viewModel.onAddRule("0900", MatchType.PREFIX)

        viewModel.onUserMessageShown()

        assertNull(viewModel.uiState.value.userMessageRes)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    /** `uiState` dùng WhileSubscribed nên cần một subscriber để upstream chạy. */
    private fun TestScope.collectUiState() {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
    }
}
