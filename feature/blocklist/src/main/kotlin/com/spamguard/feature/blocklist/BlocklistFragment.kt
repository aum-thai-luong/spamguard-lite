package com.spamguard.feature.blocklist

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.spamguard.core.ui.theme.SpamGuardTheme
import com.spamguard.feature.blocklist.model.BlockRuleUiModel
import com.spamguard.feature.blocklist.screening.CallScreeningRole
import dagger.hilt.android.AndroidEntryPoint

/** Giữ chỗ trong Navigation và quản lý dialog; giao diện vẽ bằng [BlocklistScreen]. */
@AndroidEntryPoint
class BlocklistFragment : Fragment() {

    private val viewModel: BlocklistViewModel by viewModels()

    // Kết quả được đọc lại trong onResume, chạy ngay sau khi hộp thoại vai trò đóng.
    private val screeningRoleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            SpamGuardTheme {
                BlocklistRoute(
                    viewModel = viewModel,
                    onAddClick = ::showAddRuleDialog,
                    onDeleteClick = ::confirmDeletion,
                    onEnableScreeningClick = ::requestScreeningRole,
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listenForDialogResults()
    }

    override fun onResume() {
        super.onResume()
        // Người dùng có thể bật/tắt vai trò trong Cài đặt bất cứ lúc nào.
        viewModel.onScreeningStatusChecked(CallScreeningRole.status(requireContext()))
    }

    private fun requestScreeningRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            screeningRoleLauncher.launch(CallScreeningRole.requestIntent(requireContext()))
        }
    }

    private fun showAddRuleDialog() {
        AddBlockRuleDialogFragment().show(childFragmentManager, AddBlockRuleDialogFragment.TAG)
    }

    private fun confirmDeletion(rule: BlockRuleUiModel) {
        ConfirmDeleteRuleDialogFragment.newInstance(rule.id, rule.pattern)
            .show(childFragmentManager, ConfirmDeleteRuleDialogFragment.TAG)
    }

    private fun listenForDialogResults() {
        childFragmentManager.setFragmentResultListener(
            AddBlockRuleDialogFragment.REQUEST_KEY,
            viewLifecycleOwner,
        ) { _, result ->
            val input = AddBlockRuleDialogFragment.parseResult(result)
            viewModel.onAddRule(input.pattern, input.matchType)
        }
        childFragmentManager.setFragmentResultListener(
            ConfirmDeleteRuleDialogFragment.REQUEST_KEY,
            viewLifecycleOwner,
        ) { _, result ->
            viewModel.onDeleteRule(ConfirmDeleteRuleDialogFragment.parseResult(result))
        }
    }
}
