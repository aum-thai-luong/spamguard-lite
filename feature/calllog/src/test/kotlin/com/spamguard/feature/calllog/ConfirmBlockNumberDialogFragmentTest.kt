package com.spamguard.feature.calllog

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
 * Số cần chặn phải đi qua `arguments` của dialog và quay về qua Fragment Result, kể cả khi
 * dialog bị dựng lại (xoay màn hình) trước khi người dùng xác nhận.
 */
@RunWith(AndroidJUnit4::class)
// SDK 35 của Robolectric cần JDK 21; repo build bằng JDK 17.
@Config(sdk = [34])
class ConfirmBlockNumberDialogFragmentTest {

    @Test
    fun `confirming returns the number passed to newInstance`() {
        val scenario = launchDialog(number = NUMBER)
        val results = scenario.listenForResults()

        scenario.clickButton(DialogInterface.BUTTON_POSITIVE)

        assertEquals(NUMBER, results.single().let(ConfirmBlockNumberDialogFragment::parseResult))
    }

    @Test
    fun `number survives recreation before confirming`() {
        val scenario = launchDialog(number = NUMBER)

        scenario.recreate()
        val results = scenario.listenForResults()
        scenario.clickButton(DialogInterface.BUTTON_POSITIVE)

        assertEquals(NUMBER, results.single().let(ConfirmBlockNumberDialogFragment::parseResult))
    }

    @Test
    fun `cancelling sends no result`() {
        val scenario = launchDialog(number = NUMBER)
        val results = scenario.listenForResults()

        scenario.clickButton(DialogInterface.BUTTON_NEGATIVE)

        assertTrue(results.isEmpty())
    }

    @Test
    fun `message names the number to block`() {
        val scenario = launchDialog(number = NUMBER)

        scenario.onFragment { dialog ->
            val message = (dialog.requireDialog() as AlertDialog).findViewById<TextView>(android.R.id.message)?.text
            assertEquals(dialog.getString(R.string.call_log_block_message, NUMBER), message.toString())
        }
    }

    private fun launchDialog(number: String): FragmentScenario<ConfirmBlockNumberDialogFragment> =
        launchFragment(
            // Lấy arguments từ newInstance để test đúng đường mà CallLogFragment dùng.
            fragmentArgs = ConfirmBlockNumberDialogFragment.newInstance(number).requireArguments(),
            // Đúng theme của app (MaterialAlertDialogBuilder cần theme Material).
            themeResId = com.spamguard.core.ui.R.style.Theme_SpamGuard,
        )

    /** Đăng ký listener trên FragmentManager chứa dialog; gọi lại sau mỗi lần recreate. */
    private fun FragmentScenario<ConfirmBlockNumberDialogFragment>.listenForResults(): List<Bundle> {
        val results = mutableListOf<Bundle>()
        onFragment { dialog ->
            dialog.parentFragmentManager.setFragmentResultListener(
                ConfirmBlockNumberDialogFragment.REQUEST_KEY,
                dialog.requireActivity(),
            ) { _, result -> results += result }
        }
        return results
    }

    private fun FragmentScenario<ConfirmBlockNumberDialogFragment>.clickButton(which: Int) {
        onFragment { dialog ->
            (dialog.requireDialog() as AlertDialog).getButton(which).performClick()
        }
        // AlertDialog gửi sự kiện bấm nút qua Handler của main looper.
        shadowOf(Looper.getMainLooper()).idle()
    }

    private companion object {
        /** Số giả, chỉ dùng cho test. */
        const val NUMBER = "0987654321"
    }
}
