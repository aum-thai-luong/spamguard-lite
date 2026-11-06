package com.spamguard.feature.calllog

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.spamguard.core.ui.theme.SpamGuardTheme
import com.spamguard.feature.calllog.detail.callDetailRequest
import dagger.hilt.android.AndroidEntryPoint

/** Giữ chỗ trong Navigation và xin quyền hệ thống; giao diện vẽ bằng [CallLogScreen]. */
@AndroidEntryPoint
class CallLogFragment : Fragment() {

    private val viewModel: CallLogViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        // Bị từ chối mà hệ thống không đòi giải thích nữa: lần xin sau sẽ bị từ chối ngay, không hiện hộp thoại.
        val canAskAgain = granted || shouldShowRequestPermissionRationale(Manifest.permission.READ_CALL_LOG)
        viewModel.onPermissionResult(granted, canAskAgain)
    }

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
                    onRequestPermission = ::requestCallLogPermission,
                    onOpenSettings = ::openAppSettings,
                    onCallClick = ::openCallDetail,
                    onCallLongClick = ::confirmBlockNumber,
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        childFragmentManager.setFragmentResultListener(
            ConfirmBlockNumberDialogFragment.REQUEST_KEY,
            viewLifecycleOwner,
        ) { _, result ->
            viewModel.onBlockConfirmed(ConfirmBlockNumberDialogFragment.parseResult(result))
        }
        // Chỉ tự hỏi quyền lần đầu mở màn hình; khi khôi phục, người dùng tự bấm "Cấp quyền".
        if (savedInstanceState == null && !hasCallLogPermission()) {
            requestCallLogPermission()
        }
    }

    override fun onResume() {
        super.onResume()
        // Bắt kịp quyền vừa bật trong Cài đặt, hoặc đọc lại sau khi hệ thống dừng process.
        if (hasCallLogPermission()) {
            viewModel.onPermissionAvailable()
        }
    }

    private fun requestCallLogPermission() {
        permissionLauncher.launch(Manifest.permission.READ_CALL_LOG)
    }

    private fun openCallDetail(callId: Long) {
        findNavController().navigate(callDetailRequest(callId))
    }

    private fun confirmBlockNumber(number: String) {
        ConfirmBlockNumberDialogFragment
            .newInstance(number)
            .show(childFragmentManager, ConfirmBlockNumberDialogFragment.TAG)
    }

    private fun openAppSettings() {
        val uri = Uri.fromParts("package", requireContext().packageName, null)
        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri))
    }

    private fun hasCallLogPermission(): Boolean =
        ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CALL_LOG) ==
            PackageManager.PERMISSION_GRANTED
}
