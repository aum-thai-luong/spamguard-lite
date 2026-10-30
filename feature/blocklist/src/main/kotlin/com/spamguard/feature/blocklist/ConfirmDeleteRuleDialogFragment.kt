package com.spamguard.feature.blocklist

import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/** Hỏi lại trước khi xóa quy tắc. Gửi [REQUEST_KEY] khi người dùng xác nhận. */
class ConfirmDeleteRuleDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val pattern = requireArguments().getString(ARG_PATTERN).orEmpty()
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.blocklist_delete_title)
            .setMessage(getString(R.string.blocklist_delete_message, pattern))
            .setNegativeButton(R.string.blocklist_action_cancel, null)
            .setPositiveButton(R.string.blocklist_action_delete) { _, _ ->
                setFragmentResult(REQUEST_KEY, bundleOf())
            }
            .create()
    }

    companion object {
        const val TAG = "ConfirmDeleteRuleDialog"
        const val REQUEST_KEY = "confirm_delete_rule"

        private const val ARG_PATTERN = "pattern"

        fun newInstance(pattern: String) = ConfirmDeleteRuleDialogFragment().apply {
            arguments = bundleOf(ARG_PATTERN to pattern)
        }
    }
}
