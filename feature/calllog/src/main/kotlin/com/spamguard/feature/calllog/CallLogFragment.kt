package com.spamguard.feature.calllog

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.spamguard.core.ui.theme.SpamGuardTheme
import dagger.hilt.android.AndroidEntryPoint

/** Giữ chỗ trong Navigation và xin quyền hệ thống; giao diện vẽ bằng [CallLogScreen]. */
@AndroidEntryPoint
class CallLogFragment : Fragment() {

    private val viewModel: CallLogViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> viewModel.onPermissionResult(granted) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            SpamGuardTheme {
                CallLogRoute(
                    viewModel = viewModel,
                    onRequestPermission = { permissionLauncher.launch(Manifest.permission.READ_CALL_LOG) },
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null) {
            requestCallLogAccess()
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
}
