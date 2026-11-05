package com.spamguard.feature.calllog.detail

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.spamguard.core.ui.theme.SpamGuardTheme
import com.spamguard.feature.calllog.R
import dagger.hilt.android.AndroidEntryPoint

/**
 * Giữ chỗ trong Navigation cho [CallDetailScreen]; xử lý những việc cần Android API:
 * mở trình quay số, sao chép số và hộp thoại xác nhận bỏ chặn.
 */
@AndroidEntryPoint
class CallDetailFragment : Fragment() {

    private val viewModel: CallDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        // Mất quyền (thu hồi trong Cài đặt rồi app được khôi phục): không tạo ViewModel, không đọc provider.
        if (!hasCallLogPermission()) return@apply
        setContent {
            SpamGuardTheme {
                CallDetailRoute(
                    viewModel = viewModel,
                    onCallClick = ::openDialer,
                    onCopyClick = ::copyNumber,
                    onUnblockClick = ::confirmUnblock,
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!hasCallLogPermission()) {
            // Màn Cuộc gọi tự xin lại quyền.
            findNavController().popBackStack()
            return
        }
        childFragmentManager.setFragmentResultListener(
            ConfirmUnblockDialogFragment.REQUEST_KEY,
            viewLifecycleOwner,
        ) { _, result ->
            viewModel.onUnblockConfirmed(ConfirmUnblockDialogFragment.parseResult(result))
        }
    }

    /** ACTION_DIAL chỉ mở trình quay số đã điền sẵn số, không cần quyền CALL_PHONE. */
    private fun openDialer(number: String) {
        try {
            startActivity(Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", number, null)))
        } catch (e: ActivityNotFoundException) {
            viewModel.onDialerUnavailable()
        }
    }

    private fun copyNumber(number: String) {
        val clipboard = requireContext().getSystemService(ClipboardManager::class.java) ?: return
        val clip = ClipData.newPlainText(getString(R.string.call_log_detail_clip_label), number)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Số điện thoại là dữ liệu cá nhân: ẩn nội dung trong bản xem trước clipboard của hệ thống.
            clip.description.extras = PersistableBundle().apply {
                putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
            }
        }
        clipboard.setPrimaryClip(clip)
        // Từ Android 13 hệ thống tự báo đã sao chép; báo thêm sẽ thành hai thông báo.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            viewModel.onNumberCopied()
        }
    }

    private fun confirmUnblock(rule: BlockingRuleUi) {
        ConfirmUnblockDialogFragment
            .newInstance(ruleId = rule.ruleId, pattern = rule.pattern, isPrefix = rule.isPrefix)
            .show(childFragmentManager, ConfirmUnblockDialogFragment.TAG)
    }

    private fun hasCallLogPermission(): Boolean =
        ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_CALL_LOG) ==
            PackageManager.PERMISSION_GRANTED
}
