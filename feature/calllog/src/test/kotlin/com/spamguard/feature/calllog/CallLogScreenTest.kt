package com.spamguard.feature.calllog

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.longClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spamguard.core.ui.theme.SpamGuardTheme
import com.spamguard.feature.calllog.model.CallUiModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** [CallLogScreen] vẽ đúng từng trạng thái và chuyển nút bấm về đúng callback. */
@RunWith(AndroidJUnit4::class)
// SDK 35 của Robolectric cần JDK 21; repo build bằng JDK 17.
@Config(sdk = [34])
class CallLogScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context: Context = ApplicationProvider.getApplicationContext()

    private var uiState by mutableStateOf<CallLogUiState>(CallLogUiState.Loading)
    private var requestPermissionClicks = 0
    private var openSettingsClicks = 0
    private var retryClicks = 0
    private var clickedCallId: Long? = null
    private var longClickedNumber: String? = null
    private var userMessageShownCount = 0

    @Before
    fun setUp() {
        composeRule.setContent {
            SpamGuardTheme {
                CallLogScreen(
                    uiState = uiState,
                    onRequestPermission = { requestPermissionClicks++ },
                    onOpenSettings = { openSettingsClicks++ },
                    onRetry = { retryClicks++ },
                    onRefresh = {},
                    onCallClick = { clickedCallId = it },
                    onCallLongClick = { longClickedNumber = it },
                    onUserMessageShown = { userMessageShownCount++ },
                )
            }
        }
    }

    @Test
    fun `permission required asks for permission`() {
        uiState = CallLogUiState.PermissionRequired

        composeRule.onNodeWithText(text(R.string.call_log_permission_rationale)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.call_log_action_grant_permission)).performClick()

        assertEquals(1, requestPermissionClicks)
        assertEquals(0, openSettingsClicks)
    }

    @Test
    fun `permanently denied permission opens settings`() {
        uiState = CallLogUiState.PermissionDeniedPermanently

        composeRule.onNodeWithText(text(R.string.call_log_permission_denied_permanently)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.call_log_action_open_settings)).performClick()

        assertEquals(1, openSettingsClicks)
        assertEquals(0, requestPermissionClicks)
    }

    @Test
    fun `error offers retry`() {
        uiState = CallLogUiState.Error

        composeRule.onNodeWithText(text(R.string.call_log_error)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.call_log_action_retry)).performClick()

        assertEquals(1, retryClicks)
    }

    @Test
    fun `empty call log shows empty message`() {
        uiState = CallLogUiState.Empty()

        composeRule.onNodeWithText(text(R.string.call_log_empty)).assertIsDisplayed()
    }

    @Test
    fun `content shows list with blocked summary and matching rule`() {
        uiState = CallLogUiState.Content(
            calls = listOf(
                aCallUiModel(id = 1, title = "Chị Lan", subtitle = "0919009001"),
                aCallUiModel(id = 2, title = "0900123456", blockedPattern = "0900"),
            ),
            blockedCount = 1,
        )

        // :baselineprofile tìm danh sách bằng tag này.
        composeRule.onNodeWithTag(CALL_LOG_LIST_TAG).assertIsDisplayed()
        composeRule.onNodeWithText(
            context.resources.getQuantityString(R.plurals.call_log_blocked_summary, 1, 1),
        ).assertIsDisplayed()
        composeRule.onNodeWithText("Chị Lan").assertIsDisplayed()
        composeRule.onNodeWithText("0919009001").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.call_log_blocked_by, "0900")).assertIsDisplayed()
    }

    @Test
    fun `hidden number shows private number label`() {
        uiState = CallLogUiState.Content(calls = listOf(aCallUiModel(title = null)), blockedCount = 0)

        composeRule.onNodeWithText(text(R.string.call_log_private_number)).assertIsDisplayed()
    }

    @Test
    fun `clicking a call opens its detail`() {
        uiState = CallLogUiState.Content(
            calls = listOf(aCallUiModel(id = 1, title = "0911111111"), aCallUiModel(id = 2, title = "0922222222")),
            blockedCount = 0,
        )

        composeRule.onNodeWithText("0922222222").performClick()

        assertEquals(2L, clickedCallId)
    }

    @Test
    fun `long pressing a call asks to block its number`() {
        uiState = CallLogUiState.Content(
            calls = listOf(aCallUiModel(id = 1, title = "Chị Lan", subtitle = "0919009001", number = "0919009001")),
            blockedCount = 0,
        )

        composeRule.onNodeWithText("Chị Lan").performTouchInput { longClick() }

        assertEquals("0919009001", longClickedNumber)
        assertNull(clickedCallId)
    }

    @Test
    fun `long pressing a hidden number does nothing`() {
        uiState = CallLogUiState.Content(calls = listOf(aCallUiModel(title = null, number = null)), blockedCount = 0)

        composeRule.onNodeWithText(text(R.string.call_log_private_number)).performTouchInput { longClick() }

        assertNull(longClickedNumber)
    }

    @Test
    fun `long pressing an already blocked call does nothing`() {
        uiState = CallLogUiState.Content(
            calls = listOf(aCallUiModel(title = "0900123456", number = "0900123456", blockedPattern = "0900")),
            blockedCount = 1,
        )

        composeRule.onNodeWithText("0900123456").performTouchInput { longClick() }

        assertNull(longClickedNumber)
        assertNull(clickedCallId)
    }

    @Test
    fun `user message shows snackbar once`() {
        uiState = CallLogUiState.Content(
            calls = listOf(aCallUiModel()),
            blockedCount = 0,
            userMessageRes = R.string.call_log_detail_message_blocked,
        )

        composeRule.onNodeWithText(text(R.string.call_log_detail_message_blocked)).assertIsDisplayed()
        assertEquals(1, userMessageShownCount)
    }

    @Test
    fun `replayed list with the same message does not show snackbar again`() {
        val message = R.string.call_log_detail_message_blocked
        uiState = CallLogUiState.Content(calls = listOf(aCallUiModel()), blockedCount = 0, userMessageRes = message)
        composeRule.waitForIdle()

        // Danh sách phát lại (quy tắc mới) trước khi ViewModel kịp xoá thông báo.
        uiState = CallLogUiState.Content(
            calls = listOf(aCallUiModel(blockedPattern = "0987654321")),
            blockedCount = 1,
            userMessageRes = message,
        )
        composeRule.waitForIdle()

        assertEquals(1, userMessageShownCount)
    }

    private fun text(@StringRes res: Int): String = context.getString(res)

    private fun aCallUiModel(
        id: Long = 1,
        title: String? = "0987654321",
        subtitle: String? = null,
        number: String? = title,
        blockedPattern: String? = null,
    ) = CallUiModel(
        id = id,
        title = title,
        subtitle = subtitle,
        number = number,
        timeText = "08:30 · 15/01/2026",
        typeIconRes = R.drawable.ic_call_incoming,
        typeLabelRes = R.string.call_log_type_incoming,
        blockedPattern = blockedPattern,
    )
}
