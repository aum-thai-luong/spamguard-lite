package com.spamguard.feature.blocklist

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.spamguard.core.ui.theme.SpamGuardTheme
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

/** Thông báo một lần của [BlocklistScreen]: hiện snackbar, báo đã hiện, thông báo mới thay thông báo cũ. */
@RunWith(AndroidJUnit4::class)
// SDK 35 của Robolectric cần JDK 21; repo build bằng JDK 17.
@Config(sdk = [34])
class BlocklistScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private var uiState by mutableStateOf(BlocklistUiState(isLoading = false))
    private var shownCount = 0
    private var enableScreeningClicks = 0

    @Before
    fun setUp() {
        composeRule.setContent {
            SpamGuardTheme {
                BlocklistScreen(
                    uiState = uiState,
                    onAddClick = {},
                    onDeleteClick = {},
                    onEnableScreeningClick = { enableScreeningClicks++ },
                    // Giống ViewModel: báo đã hiện thì xóa thông báo khỏi state.
                    onUserMessageShown = {
                        shownCount++
                        uiState = uiState.copy(userMessageRes = null)
                    },
                )
            }
        }
    }

    @Test
    fun `user message shows snackbar and is reported once`() {
        showMessage(R.string.blocklist_message_added)

        composeRule.onNodeWithText(text(R.string.blocklist_message_added)).assertIsDisplayed()
        assertEquals(1, shownCount)
    }

    @Test
    fun `new message replaces the one being shown`() {
        showMessage(R.string.blocklist_message_added)

        showMessage(R.string.blocklist_message_deleted)

        composeRule.onNodeWithText(text(R.string.blocklist_message_deleted)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.blocklist_message_added)).assertDoesNotExist()
        assertEquals(2, shownCount)
    }

    @Test
    fun `same message twice in a row is shown again`() {
        showMessage(R.string.blocklist_message_added)

        showMessage(R.string.blocklist_message_added)

        composeRule.onNodeWithText(text(R.string.blocklist_message_added)).assertIsDisplayed()
        assertEquals(2, shownCount)
    }

    @Test
    fun `screening banner is hidden when the role is unsupported`() {
        composeRule.onNodeWithText(text(R.string.blocklist_screening_disabled)).assertDoesNotExist()
        composeRule.onNodeWithText(text(R.string.blocklist_screening_enabled)).assertDoesNotExist()
    }

    @Test
    fun `disabled screening offers a button that requests the role`() {
        setScreeningStatus(ScreeningStatus.Disabled)

        composeRule.onNodeWithText(text(R.string.blocklist_screening_disabled)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.blocklist_action_enable_screening)).performClick()

        assertEquals(1, enableScreeningClicks)
    }

    @Test
    fun `enabled screening shows status without the enable button`() {
        setScreeningStatus(ScreeningStatus.Enabled)

        composeRule.onNodeWithText(text(R.string.blocklist_screening_enabled)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.blocklist_action_enable_screening)).assertDoesNotExist()
    }

    private fun setScreeningStatus(status: ScreeningStatus) {
        composeRule.runOnIdle { uiState = uiState.copy(screeningStatus = status) }
        composeRule.waitForIdle()
    }

    private fun showMessage(messageRes: Int) {
        // Ghi trên UI thread để Compose thấy thay đổi ngay ở frame kế tiếp.
        composeRule.runOnIdle { uiState = uiState.copy(userMessageRes = messageRes) }
        // Chờ effect chạy và animation hiện snackbar xong; thời gian chờ tự ẩn không bị tua.
        composeRule.waitForIdle()
    }

    private fun text(resId: Int): String = ApplicationProvider.getApplicationContext<Context>().getString(resId)
}
