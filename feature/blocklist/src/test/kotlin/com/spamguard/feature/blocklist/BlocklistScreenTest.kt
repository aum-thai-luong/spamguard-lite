package com.spamguard.feature.blocklist

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
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

    @Before
    fun setUp() {
        composeRule.setContent {
            SpamGuardTheme {
                BlocklistScreen(
                    uiState = uiState,
                    onAddClick = {},
                    onDeleteClick = {},
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

    private fun showMessage(messageRes: Int) {
        // Ghi trên UI thread để Compose thấy thay đổi ngay ở frame kế tiếp.
        composeRule.runOnIdle { uiState = uiState.copy(userMessageRes = messageRes) }
        // Chờ effect chạy và animation hiện snackbar xong; thời gian chờ tự ẩn không bị tua.
        composeRule.waitForIdle()
    }

    private fun text(resId: Int): String = ApplicationProvider.getApplicationContext<Context>().getString(resId)
}
