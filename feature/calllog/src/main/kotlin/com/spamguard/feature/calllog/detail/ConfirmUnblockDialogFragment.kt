package com.spamguard.feature.calllog.detail

import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.spamguard.feature.calllog.R

/**
 * Hỏi lại trước khi bỏ chặn. Quy tắc đầu số có thể chặn nhiều số, nên thông điệp nói rõ điều đó.
 * Gửi [REQUEST_KEY] khi người dùng xác nhận; đọc id quy tắc bằng [parseResult].
 */
class ConfirmUnblockDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val args = requireArguments()
        val pattern = args.getString(ARG_PATTERN).orEmpty()
        val messageRes = if (args.getBoolean(ARG_IS_PREFIX)) {
            R.string.call_log_detail_unblock_message_prefix
        } else {
            R.string.call_log_detail_unblock_message_exact
        }
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.call_log_detail_unblock_title)
            .setMessage(getString(messageRes, pattern))
            .setNegativeButton(R.string.call_log_detail_action_cancel, null)
            .setPositiveButton(R.string.call_log_detail_action_unblock) { _, _ ->
                setFragmentResult(REQUEST_KEY, bundleOf(KEY_RULE_ID to args.getLong(ARG_RULE_ID)))
            }
            .create()
    }

    companion object {
        const val TAG = "ConfirmUnblockDialog"
        const val REQUEST_KEY = "confirm_unblock"

        private const val ARG_RULE_ID = "rule_id"
        private const val ARG_PATTERN = "pattern"
        private const val ARG_IS_PREFIX = "is_prefix"
        private const val KEY_RULE_ID = "rule_id"

        fun newInstance(ruleId: Long, pattern: String, isPrefix: Boolean) = ConfirmUnblockDialogFragment().apply {
            arguments = bundleOf(ARG_RULE_ID to ruleId, ARG_PATTERN to pattern, ARG_IS_PREFIX to isPrefix)
        }

        /** Id của quy tắc người dùng đã xác nhận bỏ chặn. */
        fun parseResult(result: Bundle): Long = result.getLong(KEY_RULE_ID)
    }
}
