package com.spamguard.feature.calllog

import android.content.res.Configuration
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spamguard.core.ui.theme.SpamGuardTheme
import com.spamguard.feature.calllog.model.CallUiModel

/**
 * Nối [CallLogViewModel] với [CallLogScreen]. Quyền hệ thống cần Android API (Activity Result,
 * Cài đặt) nên Route xử lý; mọi trạng thái khác nằm trong ViewModel.
 */
@Composable
internal fun CallLogRoute(
    onCallClick: (callId: Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CallLogViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(RequestPermission()) { granted ->
        // Bị từ chối mà hệ thống không đòi giải thích nữa: lần xin sau sẽ bị từ chối ngay, không hiện hộp thoại.
        val canAskAgain = granted || activity == null ||
            ActivityCompat.shouldShowRequestPermissionRationale(activity, CALL_LOG_PERMISSION)
        viewModel.onPermissionResult(granted, canAskAgain)
    }

    // Chỉ tự hỏi quyền lần đầu mở màn hình; khi khôi phục, người dùng tự bấm "Cấp quyền".
    var permissionAsked by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!permissionAsked && !context.hasCallLogPermission()) {
            permissionAsked = true
            permissionLauncher.launch(CALL_LOG_PERMISSION)
        }
    }

    // Bắt kịp quyền vừa bật trong Cài đặt, hoặc đọc lại sau khi hệ thống dừng process.
    LifecycleResumeEffect(Unit) {
        if (context.hasCallLogPermission()) viewModel.onPermissionAvailable()
        onPauseOrDispose {}
    }

    CallLogScreen(
        uiState = uiState,
        onRequestPermission = { permissionLauncher.launch(CALL_LOG_PERMISSION) },
        onOpenSettings = context::openAppSettings,
        onRetry = viewModel::onRetry,
        onRefresh = viewModel::onRefresh,
        onCallClick = onCallClick,
        onCallLongClick = viewModel::onCallLongClick,
        onBlockConfirm = viewModel::onBlockConfirmed,
        onBlockDismiss = viewModel::onBlockDismissed,
        onUserMessageShown = viewModel::onUserMessageShown,
        modifier = modifier,
    )
}

/** Chỉ hiển thị [uiState]; mọi quyết định nằm ở ViewModel và [CallUiMapper]. */
@Composable
internal fun CallLogScreen(
    uiState: CallLogUiState,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onCallClick: (callId: Long) -> Unit,
    onCallLongClick: (number: String) -> Unit,
    onBlockConfirm: () -> Unit,
    onBlockDismiss: () -> Unit,
    onUserMessageShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    (uiState as? CallLogUiState.Content)?.pendingBlockNumber?.let { number ->
        ConfirmBlockNumberDialog(number = number, onConfirm = onBlockConfirm, onDismiss = onBlockDismiss)
    }

    val snackbarHostState = remember { SnackbarHostState() }
    UserMessageEffect(
        messageRes = (uiState as? CallLogUiState.Content)?.userMessageRes,
        snackbarHostState = snackbarHostState,
        onUserMessageShown = onUserMessageShown,
    )

    Scaffold(
        modifier = modifier,
        // Thanh trên và thanh điều hướng dưới của app đã xử lý inset.
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        CallLogContent(
            uiState = uiState,
            onRequestPermission = onRequestPermission,
            onOpenSettings = onOpenSettings,
            onRetry = onRetry,
            onRefresh = onRefresh,
            onCallClick = onCallClick,
            onCallLongClick = onCallLongClick,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CallLogContent(
    uiState: CallLogUiState,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onCallClick: (callId: Long) -> Unit,
    onCallLongClick: (number: String) -> Unit,
    modifier: Modifier,
) {
    when (uiState) {
        CallLogUiState.Loading -> Box(modifier, contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        CallLogUiState.PermissionRequired -> MessageState(
            messageRes = R.string.call_log_permission_rationale,
            actionRes = R.string.call_log_action_grant_permission,
            onAction = onRequestPermission,
            modifier = modifier,
        )

        CallLogUiState.PermissionDeniedPermanently -> MessageState(
            messageRes = R.string.call_log_permission_denied_permanently,
            actionRes = R.string.call_log_action_open_settings,
            onAction = onOpenSettings,
            modifier = modifier,
        )

        CallLogUiState.Error -> MessageState(
            messageRes = R.string.call_log_error,
            actionRes = R.string.call_log_action_retry,
            onAction = onRetry,
            modifier = modifier,
        )

        is CallLogUiState.Empty -> PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefresh,
            modifier = modifier,
        ) {
            // LazyColumn để cử chỉ kéo vẫn hoạt động khi không có dữ liệu.
            LazyColumn(Modifier.fillMaxSize()) {
                item {
                    MessageState(
                        messageRes = R.string.call_log_empty,
                        modifier = Modifier.fillParentMaxSize(),
                    )
                }
            }
        }

        is CallLogUiState.Content -> PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefresh,
            modifier = modifier,
        ) {
            CallList(
                calls = uiState.calls,
                blockedCount = uiState.blockedCount,
                onCallClick = onCallClick,
                onCallLongClick = onCallLongClick,
            )
        }
    }
}

@Composable
private fun CallList(
    calls: List<CallUiModel>,
    blockedCount: Int,
    onCallClick: (callId: Long) -> Unit,
    onCallLongClick: (number: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier
            .fillMaxSize()
            .testTag(CALL_LOG_LIST_TAG),
    ) {
        // contentType giúp Compose chỉ tái sử dụng node giữa các dòng cùng loại khi cuộn.
        item(contentType = CONTENT_TYPE_SUMMARY) {
            Text(
                text = pluralStringResource(R.plurals.call_log_blocked_summary, blockedCount, blockedCount),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
        }
        items(calls, key = CallUiModel::id, contentType = { CONTENT_TYPE_CALL }) { call ->
            CallItem(
                call,
                onClick = { onCallClick(call.id) },
                onLongClick = { call.number?.takeIf { call.canBlock }?.let(onCallLongClick) },
            )
        }
    }
}

/** Module :baselineprofile tìm danh sách bằng tag này (`testTagsAsResourceId` bật ở gốc UI của :app). */
internal const val CALL_LOG_LIST_TAG = "call_log_list"

private const val CONTENT_TYPE_SUMMARY = "summary"
private const val CONTENT_TYPE_CALL = "call"

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun CallItem(
    call: CallUiModel,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                // Số ẩn hoặc đã bị chặn: vẫn bắt nhấn giữ để thả tay không bị tính là chạm (mở chi tiết),
                // nhưng không đặt nhãn "Chặn số này" cho TalkBack.
                onLongClickLabel = if (call.canBlock) stringResource(R.string.call_log_detail_action_block) else null,
                onLongClick = onLongClick,
            )
            .heightIn(min = 72.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Icon(
            painter = painterResource(call.typeIconRes),
            contentDescription = stringResource(call.typeLabelRes),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp),
        )
        Column(
            Modifier
                .weight(1f)
                .padding(start = 16.dp, end = 8.dp),
        ) {
            Text(
                text = call.title ?: stringResource(R.string.call_log_private_number),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            call.subtitle?.let { subtitle ->
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            call.blockedPattern?.let { pattern ->
                Text(
                    text = stringResource(R.string.call_log_blocked_by, pattern),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
        Text(
            text = call.timeText,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.Top),
        )
    }
}

/** Thông điệp giữa màn hình kèm nút hành động tùy chọn; màn hình chi tiết dùng lại. */
@Composable
internal fun MessageState(
    @StringRes messageRes: Int,
    modifier: Modifier = Modifier,
    @StringRes actionRes: Int? = null,
    onAction: () -> Unit = {},
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(32.dp),
    ) {
        Text(
            text = stringResource(messageRes),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        actionRes?.let { labelRes ->
            Button(onClick = onAction, modifier = Modifier.padding(top = 16.dp)) {
                Text(stringResource(labelRes))
            }
        }
    }
}

private val previewCalls = listOf(
    CallUiModel(
        id = 1,
        title = "Chị Lan",
        subtitle = "0919009001",
        number = "0919009001",
        timeText = "08:30 · 15/01/2026",
        typeIconRes = R.drawable.ic_call_incoming,
        typeLabelRes = R.string.call_log_type_incoming,
        blockedPattern = null,
    ),
    CallUiModel(
        id = 2,
        title = "0900123456",
        number = "0900123456",
        subtitle = null,
        timeText = "07:12 · 15/01/2026",
        typeIconRes = R.drawable.ic_call_missed,
        typeLabelRes = R.string.call_log_type_missed,
        blockedPattern = "0900",
    ),
    CallUiModel(
        id = 3,
        title = null,
        number = null,
        subtitle = null,
        timeText = "21:45 · 14/01/2026",
        typeIconRes = R.drawable.ic_call_outgoing,
        typeLabelRes = R.string.call_log_type_outgoing,
        blockedPattern = null,
    ),
)

@Composable
private fun CallLogScreenPreview(uiState: CallLogUiState) {
    SpamGuardTheme {
        // Surface tô nền theo theme để Preview tối không bị nền trắng của showBackground.
        Surface {
            CallLogScreen(
                uiState = uiState,
                onRequestPermission = {},
                onOpenSettings = {},
                onRetry = {},
                onRefresh = {},
                onCallClick = {},
                onCallLongClick = {},
                onBlockConfirm = {},
                onBlockDismiss = {},
                onUserMessageShown = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ContentPreview() =
    CallLogScreenPreview(CallLogUiState.Content(calls = previewCalls, blockedCount = 1))

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ContentDarkPreview() =
    CallLogScreenPreview(CallLogUiState.Content(calls = previewCalls, blockedCount = 1))

@Preview(showBackground = true)
@Composable
private fun EmptyPreview() = CallLogScreenPreview(CallLogUiState.Empty())

@Preview(showBackground = true)
@Composable
private fun ErrorPreview() = CallLogScreenPreview(CallLogUiState.Error)

@Preview(showBackground = true)
@Composable
private fun PermissionRequiredPreview() = CallLogScreenPreview(CallLogUiState.PermissionRequired)

@Preview(showBackground = true)
@Composable
private fun PermissionDeniedPermanentlyPreview() =
    CallLogScreenPreview(CallLogUiState.PermissionDeniedPermanently)

@Preview(showBackground = true)
@Composable
private fun LoadingPreview() = CallLogScreenPreview(CallLogUiState.Loading)

@Preview(showBackground = true)
@Composable
private fun BlockDialogPreview() = CallLogScreenPreview(
    CallLogUiState.Content(calls = previewCalls, blockedCount = 1, pendingBlockNumber = "0987654321"),
)
