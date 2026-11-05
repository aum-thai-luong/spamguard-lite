package com.spamguard.feature.calllog.detail

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spamguard.core.ui.theme.SpamGuardTheme
import com.spamguard.feature.calllog.R
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** [CallDetailScreen] hiện đúng nút theo trạng thái và chuyển sự kiện về đúng callback. */
@RunWith(AndroidJUnit4::class)
// SDK 35 của Robolectric cần JDK 21; repo build bằng JDK 17.
@Config(sdk = [34])
class CallDetailScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context: Context = ApplicationProvider.getApplicationContext()

    private var uiState by mutableStateOf<CallDetailUiState>(CallDetailUiState.Loading)
    private val calledNumbers = mutableListOf<String>()
    private val copiedNumbers = mutableListOf<String>()
    private var blockClicks = 0
    private val unblockedRules = mutableListOf<BlockingRuleUi>()
    private var retryClicks = 0
    private var shownCount = 0

    @Before
    fun setUp() {
        composeRule.setContent {
            SpamGuardTheme {
                CallDetailScreen(
                    uiState = uiState,
                    onCallClick = { calledNumbers += it },
                    onCopyClick = { copiedNumbers += it },
                    onBlockClick = { blockClicks++ },
                    onUnblockClick = { unblockedRules += it },
                    onRetry = { retryClicks++ },
                    // Giống ViewModel: báo đã hiện thì xóa thông báo khỏi state.
                    onUserMessageShown = {
                        shownCount++
                        (uiState as? CallDetailUiState.Content)?.let { uiState = it.copy(userMessageRes = null) }
                    },
                )
            }
        }
    }

    @Test
    fun `clean call offers call, copy and block`() {
        uiState = CallDetailUiState.Content(aDetail(number = "0987654321"))

        click(R.string.call_log_detail_action_call)
        click(R.string.call_log_detail_action_copy)
        click(R.string.call_log_detail_action_block)

        assertEquals(listOf("0987654321"), calledNumbers)
        assertEquals(listOf("0987654321"), copiedNumbers)
        assertEquals(1, blockClicks)
        composeRule.onNodeWithText(text(R.string.call_log_detail_action_unblock)).assertDoesNotExist()
    }

    @Test
    fun `blocked call shows the rule and offers unblock instead of block`() {
        val rule = BlockingRuleUi(3, "0900", isPrefix = true, R.string.call_log_detail_blocked_by_prefix)
        uiState = CallDetailUiState.Content(aDetail(number = "0900123456", blockingRule = rule))

        composeRule.onNodeWithText(context.getString(R.string.call_log_detail_blocked_by_prefix, "0900")).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.call_log_detail_action_block)).assertDoesNotExist()
        click(R.string.call_log_detail_action_unblock)

        assertEquals(listOf(rule), unblockedRules)
    }

    @Test
    fun `hidden number shows private label without call or block actions`() {
        uiState = CallDetailUiState.Content(aDetail(title = null, number = null))

        composeRule.onNodeWithText(text(R.string.call_log_private_number)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.call_log_detail_action_call)).assertDoesNotExist()
        composeRule.onNodeWithText(text(R.string.call_log_detail_action_copy)).assertDoesNotExist()
        composeRule.onNodeWithText(text(R.string.call_log_detail_action_block)).assertDoesNotExist()
    }

    @Test
    fun `unconnected call shows not connected`() {
        uiState = CallDetailUiState.Content(aDetail(durationText = null))

        composeRule.onNodeWithText(text(R.string.call_log_detail_not_connected)).assertIsDisplayed()
    }

    @Test
    fun `not found and error states`() {
        uiState = CallDetailUiState.NotFound
        composeRule.onNodeWithText(text(R.string.call_log_detail_not_found)).assertIsDisplayed()

        uiState = CallDetailUiState.Error
        composeRule.onNodeWithText(text(R.string.call_log_error)).assertIsDisplayed()
        // Trạng thái lỗi không nằm trong vùng cuộn: bấm thẳng.
        composeRule.onNodeWithText(text(R.string.call_log_action_retry)).performClick()

        assertEquals(1, retryClicks)
    }

    @Test
    fun `user message is shown once`() {
        uiState = CallDetailUiState.Content(aDetail(), userMessageRes = R.string.call_log_detail_message_blocked)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(text(R.string.call_log_detail_message_blocked)).assertIsDisplayed()
        assertEquals(1, shownCount)
    }

    /** Nút hành động nằm trong cột cuộn của nội dung chi tiết. */
    private fun click(@StringRes res: Int) {
        composeRule.onNodeWithText(text(res)).performScrollTo().performClick()
    }

    private fun text(@StringRes res: Int): String = context.getString(res)

    private fun aDetail(
        title: String? = "0987654321",
        number: String? = "0987654321",
        durationText: String? = "0:42",
        blockingRule: BlockingRuleUi? = null,
    ) = CallDetailUiModel(
        title = title,
        subtitle = null,
        number = number,
        typeIconRes = R.drawable.ic_call_incoming,
        typeLabelRes = R.string.call_log_type_incoming,
        timeText = "08:30 · 15/01/2026",
        durationText = durationText,
        blockingRule = blockingRule,
    )
}
