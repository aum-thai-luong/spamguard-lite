package com.spamguard.feature.calllog.detail

import android.content.DialogInterface
import android.os.Bundle
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragment
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * Id quy tắc phải đi qua `arguments` của dialog và quay về qua Fragment Result, kể cả khi
 * dialog bị dựng lại (xoay màn hình) trước khi người dùng xác nhận.
 */
@RunWith(AndroidJUnit4::class)
// SDK 35 của Robolectric cần JDK 21; repo build bằng JDK 17.
@Config(sdk = [34])
class ConfirmUnblockDialogFragmentTest {

    @Test
    fun `confirming returns the rule id passed to newInstance`() {
        val scenario = launchDialog(ruleId = 42L)
        val results = scenario.listenForResults()

        scenario.clickButton(DialogInterface.BUTTON_POSITIVE)

        assertEquals(42L, results.single().let(ConfirmUnblockDialogFragment::parseResult))
    }

    @Test
    fun `rule id survives recreation before confirming`() {
        val scenario = launchDialog(ruleId = 42L)

        scenario.recreate()
        val results = scenario.listenForResults()
        scenario.clickButton(DialogInterface.BUTTON_POSITIVE)

        assertEquals(42L, results.single().let(ConfirmUnblockDialogFragment::parseResult))
    }

    @Test
    fun `cancelling sends no result`() {
        val scenario = launchDialog(ruleId = 42L)
        val results = scenario.listenForResults()

        scenario.clickButton(DialogInterface.BUTTON_NEGATIVE)

        assertTrue(results.isEmpty())
    }

    @Test
    fun `prefix rule warns that every number with the prefix is unblocked`() {
        val scenario = launchDialog(ruleId = 42L, isPrefix = true)

        scenario.onFragment { dialog ->
            val message = (dialog.requireDialog() as AlertDialog).findViewById<TextView>(android.R.id.message)?.text
            val expected = dialog.getString(com.spamguard.feature.calllog.R.string.call_log_detail_unblock_message_prefix, "0900")
            assertEquals(expected, message.toString())
        }
    }

    private fun launchDialog(ruleId: Long, isPrefix: Boolean = false): FragmentScenario<ConfirmUnblockDialogFragment> =
        launchFragment(
            // Lấy arguments từ newInstance để test đúng đường mà CallDetailFragment dùng.
            fragmentArgs = ConfirmUnblockDialogFragment.newInstance(ruleId, "0900", isPrefix).requireArguments(),
            // Đúng theme của app (MaterialAlertDialogBuilder cần theme Material).
            themeResId = com.spamguard.core.ui.R.style.Theme_SpamGuard,
        )

    /** Đăng ký listener trên FragmentManager chứa dialog; gọi lại sau mỗi lần recreate. */
    private fun FragmentScenario<ConfirmUnblockDialogFragment>.listenForResults(): List<Bundle> {
        val results = mutableListOf<Bundle>()
        onFragment { dialog ->
            dialog.parentFragmentManager.setFragmentResultListener(
                ConfirmUnblockDialogFragment.REQUEST_KEY,
                dialog.requireActivity(),
            ) { _, result -> results += result }
        }
        return results
    }

    private fun FragmentScenario<ConfirmUnblockDialogFragment>.clickButton(which: Int) {
        onFragment { dialog ->
            (dialog.requireDialog() as AlertDialog).getButton(which).performClick()
        }
        // AlertDialog gửi sự kiện bấm nút qua Handler của main looper.
        shadowOf(Looper.getMainLooper()).idle()
    }
}
