package com.spamguard.feature.calllog

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spamguard.core.ui.theme.SpamGuardTheme
import com.spamguard.feature.calllog.model.CallUiModel

/**
 * Nối [CallLogViewModel] với [CallLogScreen]; quyền hệ thống do Fragment xin hộ qua
 * [onRequestPermission], hoặc mở Cài đặt qua [onOpenSettings].
 */
@Composable
internal fun CallLogRoute(
    viewModel: CallLogViewModel,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CallLogScreen(
        uiState = uiState,
        onRequestPermission = onRequestPermission,
        onOpenSettings = onOpenSettings,
        onRetry = viewModel::onRetry,
        onRefresh = viewModel::onRefresh,
        modifier = modifier,
    )
}

/** Chỉ hiển thị [uiState]; mọi quyết định nằm ở ViewModel và [CallUiMapper]. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CallLogScreen(
    uiState: CallLogUiState,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        CallLogUiState.Loading -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        CallLogUiState.PermissionRequired -> MessageState(
            messageRes = R.string.call_log_permission_rationale,
            actionRes = R.string.call_log_action_grant_permission,
            onAction = onRequestPermission,
            modifier = modifier.fillMaxSize(),
        )

        CallLogUiState.PermissionDeniedPermanently -> MessageState(
            messageRes = R.string.call_log_permission_denied_permanently,
            actionRes = R.string.call_log_action_open_settings,
            onAction = onOpenSettings,
            modifier = modifier.fillMaxSize(),
        )

        CallLogUiState.Error -> MessageState(
            messageRes = R.string.call_log_error,
            actionRes = R.string.call_log_action_retry,
            onAction = onRetry,
            modifier = modifier.fillMaxSize(),
        )

        is CallLogUiState.Empty -> PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefresh,
            modifier = modifier.fillMaxSize(),
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
            modifier = modifier.fillMaxSize(),
        ) {
            CallList(calls = uiState.calls, blockedCount = uiState.blockedCount)
        }
    }
}

@Composable
private fun CallList(
    calls: List<CallUiModel>,
    blockedCount: Int,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier.fillMaxSize()) {
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
            CallItem(call)
        }
    }
}

private const val CONTENT_TYPE_SUMMARY = "summary"
private const val CONTENT_TYPE_CALL = "call"

@Composable
private fun CallItem(
    call: CallUiModel,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
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

@Composable
private fun MessageState(
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
        timeText = "08:30 · 15/01/2026",
        typeIconRes = R.drawable.ic_call_incoming,
        typeLabelRes = R.string.call_log_type_incoming,
        blockedPattern = null,
    ),
    CallUiModel(
        id = 2,
        title = "0900123456",
        subtitle = null,
        timeText = "07:12 · 15/01/2026",
        typeIconRes = R.drawable.ic_call_missed,
        typeLabelRes = R.string.call_log_type_missed,
        blockedPattern = "0900",
    ),
    CallUiModel(
        id = 3,
        title = null,
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
        CallLogScreen(
            uiState = uiState,
            onRequestPermission = {},
            onOpenSettings = {},
            onRetry = {},
            onRefresh = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ContentPreview() =
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
