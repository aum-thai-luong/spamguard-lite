package com.spamguard.lite

import android.Manifest
import androidx.annotation.StringRes
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.rule.GrantPermissionRule
import com.spamguard.core.domain.model.MatchType
import com.spamguard.core.testing.FakeBlockRuleRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import com.spamguard.feature.blocklist.R as BlocklistR
import com.spamguard.feature.calllog.R as CallLogR

/**
 * Chạy app thật trên thiết bị với dữ liệu giả (xem `FakeDataModule`): điều hướng giữa tab,
 * sang màn chi tiết và hộp thoại còn nguyên sau khi Activity bị dựng lại.
 */
@HiltAndroidTest
class NavigationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(Manifest.permission.READ_CALL_LOG)

    @get:Rule(order = 2)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var blockRuleRepository: FakeBlockRuleRepository

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun callLogShowsCallsFromRepository() {
        composeRule.onNodeWithText("Chị Lan").assertIsDisplayed()
        composeRule.onNodeWithText("0900123456").assertIsDisplayed()
    }

    @Test
    fun tappingCallOpensDetailAndUpReturnsToList() {
        composeRule.onNodeWithText("Chị Lan").performClick()

        composeRule.onNodeWithText(text(R.string.destination_call_detail)).assertIsDisplayed()
        composeRule.onNodeWithText(text(CallLogR.string.call_log_detail_action_call)).assertIsDisplayed()

        composeRule.onNodeWithContentDescription(text(R.string.navigate_up)).performClick()

        composeRule.onNodeWithText(text(CallLogR.string.call_log_detail_action_call)).assertDoesNotExist()
        composeRule.onNodeWithText("Chị Lan").assertIsDisplayed()
    }

    @Test
    fun addingRuleFromBlocklistTabListsIt() {
        composeRule.onNodeWithTag(BLOCKLIST_TAB_TAG).performClick()
        // Chữ của nút nổi nằm trong cây semantics chưa gộp.
        composeRule.onNodeWithText(text(BlocklistR.string.blocklist_action_add_rule), useUnmergedTree = true)
            .performClick()

        composeRule.onNodeWithText(text(BlocklistR.string.blocklist_input_hint)).performTextInput("0900")
        composeRule.onNodeWithText(text(BlocklistR.string.blocklist_action_add)).performClick()

        composeRule.onNodeWithText("0900").assertIsDisplayed()
    }

    /** Regression BUG-1 ở mức UI: hộp thoại xóa còn sau khi xoay màn hình và xóa đúng quy tắc. */
    @Test
    fun deleteDialogSurvivesRecreationAndDeletesTheRule() {
        runBlocking { blockRuleRepository.addRule("0900", MatchType.PREFIX) }
        composeRule.onNodeWithTag(BLOCKLIST_TAB_TAG).performClick()
        composeRule.onNodeWithContentDescription(
            composeRule.activity.getString(BlocklistR.string.blocklist_delete_rule_description, "0900"),
        ).performClick()

        composeRule.activityRule.scenario.recreate()

        composeRule.onNodeWithText(text(BlocklistR.string.blocklist_delete_title)).assertIsDisplayed()
        composeRule.onNodeWithText(text(BlocklistR.string.blocklist_action_delete)).performClick()

        composeRule.onNodeWithText(text(BlocklistR.string.blocklist_empty)).assertIsDisplayed()
    }

    private fun text(@StringRes res: Int): String = composeRule.activity.getString(res)

    private companion object {
        /** Trùng `TopLevelDestination.Blocklist.testTag`. */
        const val BLOCKLIST_TAB_TAG = "blocklistFragment"
    }
}
