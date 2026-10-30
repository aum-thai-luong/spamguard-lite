package com.spamguard.feature.blocklist

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.spamguard.core.domain.model.MatchType
import com.spamguard.feature.blocklist.databinding.DialogAddBlockRuleBinding

/**
 * Hộp thoại nhập quy tắc chặn mới. Kết quả trả về qua Fragment Result API
 * với [REQUEST_KEY]; đọc bằng [parseResult].
 */
class AddBlockRuleDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogAddBlockRuleBinding.inflate(LayoutInflater.from(requireContext()))
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.blocklist_add_title)
            .setView(binding.root)
            .setNegativeButton(R.string.blocklist_action_cancel, null)
            .setPositiveButton(R.string.blocklist_action_add) { _, _ -> submit(binding) }
            .create()
    }

    private fun submit(binding: DialogAddBlockRuleBinding) {
        val matchType = if (binding.matchPrefix.isChecked) MatchType.PREFIX else MatchType.EXACT
        setFragmentResult(
            REQUEST_KEY,
            bundleOf(
                KEY_PATTERN to binding.patternInput.text?.toString().orEmpty(),
                KEY_MATCH_TYPE to matchType.name,
            ),
        )
    }

    data class Input(val pattern: String, val matchType: MatchType)

    companion object {
        const val TAG = "AddBlockRuleDialog"
        const val REQUEST_KEY = "add_block_rule"

        private const val KEY_PATTERN = "pattern"
        private const val KEY_MATCH_TYPE = "match_type"

        fun parseResult(result: Bundle) = Input(
            pattern = result.getString(KEY_PATTERN).orEmpty(),
            matchType = MatchType.valueOf(result.getString(KEY_MATCH_TYPE) ?: MatchType.EXACT.name),
        )
    }
}
