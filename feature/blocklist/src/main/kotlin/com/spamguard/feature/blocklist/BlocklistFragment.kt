package com.spamguard.feature.blocklist

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.snackbar.Snackbar
import com.spamguard.feature.blocklist.databinding.FragmentBlocklistBinding
import com.spamguard.feature.blocklist.model.BlockRuleUiModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BlocklistFragment : Fragment(R.layout.fragment_blocklist) {

    private val viewModel: BlocklistViewModel by viewModels()

    private lateinit var pendingDeletion: BlockRuleUiModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentBlocklistBinding.bind(view)
        val adapter = BlockRuleAdapter(onDeleteClick = ::confirmDeletion)
        binding.ruleList.adapter = adapter
        binding.addRuleButton.setOnClickListener { showAddRuleDialog() }

        listenForDialogResults()
        observeUiState(binding, adapter)
    }

    private fun showAddRuleDialog() {
        AddBlockRuleDialogFragment().show(childFragmentManager, AddBlockRuleDialogFragment.TAG)
    }

    private fun confirmDeletion(rule: BlockRuleUiModel) {
        pendingDeletion = rule
        ConfirmDeleteRuleDialogFragment.newInstance(rule.pattern)
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
        ) { _, _ ->
            viewModel.onDeleteRule(pendingDeletion.id)
        }
    }

    private fun observeUiState(binding: FragmentBlocklistBinding, adapter: BlockRuleAdapter) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> binding.render(state, adapter) }
            }
        }
    }

    private fun FragmentBlocklistBinding.render(state: BlocklistUiState, adapter: BlockRuleAdapter) {
        progress.isVisible = state.isLoading
        emptyMessage.isVisible = state.isEmpty
        ruleList.isVisible = state.rules.isNotEmpty()
        adapter.submitList(state.rules)

        state.userMessageRes?.let { messageRes ->
            Snackbar.make(root, messageRes, Snackbar.LENGTH_SHORT)
                .setAnchorView(addRuleButton)
                .show()
            viewModel.onUserMessageShown()
        }
    }
}
