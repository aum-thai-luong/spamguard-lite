package com.spamguard.feature.calllog

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.spamguard.feature.calllog.databinding.FragmentCallLogBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CallLogFragment : Fragment(R.layout.fragment_call_log) {

    private val viewModel: CallLogViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> viewModel.onPermissionResult(granted) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentCallLogBinding.bind(view)
        val adapter = CallLogAdapter()
        binding.callList.adapter = adapter

        observeUiState(binding, adapter)
        if (savedInstanceState == null) {
            requestCallLogAccess()
        }
    }

    private fun observeUiState(binding: FragmentCallLogBinding, adapter: CallLogAdapter) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> binding.render(state, adapter) }
            }
        }
    }

    private fun requestCallLogAccess() {
        if (hasCallLogPermission()) {
            viewModel.onPermissionResult(granted = true)
        } else {
            permissionLauncher.launch(Manifest.permission.READ_CALL_LOG)
        }
    }

    private fun hasCallLogPermission(): Boolean =
        ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CALL_LOG) ==
            PackageManager.PERMISSION_GRANTED

    private fun FragmentCallLogBinding.render(state: CallLogUiState, adapter: CallLogAdapter) {
        progress.isVisible = state == CallLogUiState.Loading
        summary.isVisible = state is CallLogUiState.Content
        callList.isVisible = state is CallLogUiState.Content
        stateContainer.isVisible = state == CallLogUiState.PermissionRequired ||
            state == CallLogUiState.Empty ||
            state == CallLogUiState.Error

        when (state) {
            is CallLogUiState.Content -> {
                adapter.submitList(state.calls)
                summary.text = resources.getQuantityString(
                    R.plurals.call_log_blocked_summary,
                    state.blockedCount,
                    state.blockedCount,
                )
            }

            CallLogUiState.PermissionRequired -> showMessage(
                messageRes = R.string.call_log_permission_rationale,
                actionRes = R.string.call_log_action_grant_permission,
                onAction = { permissionLauncher.launch(Manifest.permission.READ_CALL_LOG) },
            )

            CallLogUiState.Empty -> showMessage(messageRes = R.string.call_log_empty)

            CallLogUiState.Error -> showMessage(
                messageRes = R.string.call_log_error,
                actionRes = R.string.call_log_action_retry,
                onAction = viewModel::onRetry,
            )

            CallLogUiState.Loading -> Unit
        }
    }

    private fun FragmentCallLogBinding.showMessage(
        @StringRes messageRes: Int,
        @StringRes actionRes: Int? = null,
        onAction: () -> Unit = {},
    ) {
        stateMessage.setText(messageRes)
        stateAction.isVisible = actionRes != null
        actionRes?.let { stateAction.setText(it) }
        stateAction.setOnClickListener { onAction() }
    }
}
