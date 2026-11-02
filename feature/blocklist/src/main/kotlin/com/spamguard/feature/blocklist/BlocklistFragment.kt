package com.spamguard.feature.blocklist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.spamguard.feature.blocklist.model.BlockRuleUiModel
import dagger.hilt.android.AndroidEntryPoint

/** Giữ chỗ trong Navigation và quản lý dialog; giao diện vẽ bằng [BlocklistScreen]. */
@AndroidEntryPoint
class BlocklistFragment : Fragment() {

    private val viewModel: BlocklistViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            BlocklistTheme {
                BlocklistRoute(
                    viewModel = viewModel,
                    onAddClick = ::showAddRuleDialog,
                    onDeleteClick = ::confirmDeletion,
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listenForDialogResults()
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
