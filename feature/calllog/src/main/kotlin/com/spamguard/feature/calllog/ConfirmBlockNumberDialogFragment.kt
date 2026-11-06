package com.spamguard.feature.calllog

import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Hỏi lại trước khi chặn đúng một số (nhấn giữ cuộc gọi ở tab Cuộc gọi).
 * Gửi [REQUEST_KEY] khi người dùng xác nhận; đọc số bằng [parseResult].
 */
class ConfirmBlockNumberDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val number = requireArguments().getString(ARG_NUMBER).orEmpty()
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.call_log_block_title)
            .setMessage(getString(R.string.call_log_block_message, number))
            .setNegativeButton(R.string.call_log_detail_action_cancel, null)
            .setPositiveButton(R.string.call_log_action_block) { _, _ ->
                setFragmentResult(REQUEST_KEY, bundleOf(KEY_NUMBER to number))
            }
            .create()
    }

    companion object {
        const val TAG = "ConfirmBlockNumberDialog"
        const val REQUEST_KEY = "confirm_block_number"

        private const val ARG_NUMBER = "number"
        private const val KEY_NUMBER = "number"

        fun newInstance(number: String) = ConfirmBlockNumberDialogFragment().apply {
            arguments = bundleOf(ARG_NUMBER to number)
        }

        /** Số người dùng đã xác nhận chặn, đúng như truyền vào [newInstance]. */
        fun parseResult(result: Bundle): String = result.getString(KEY_NUMBER).orEmpty()
    }
}
