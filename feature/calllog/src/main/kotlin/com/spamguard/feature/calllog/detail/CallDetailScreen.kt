package com.spamguard.feature.calllog.detail

import android.content.res.Configuration
import androidx.annotation.StringRes
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spamguard.core.ui.theme.SpamGuardTheme
import com.spamguard.feature.calllog.MessageState
import com.spamguard.feature.calllog.R
import kotlinx.coroutines.launch

/**
 * Nối [CallDetailViewModel] với [CallDetailScreen]. Gọi lại, sao chép và hộp thoại bỏ chặn
 * cần Android API nên do Fragment xử lý qua các callback.
 */
@Composable
internal fun CallDetailRoute(
    viewModel: CallDetailViewModel,
    onCallClick: (number: String) -> Unit,
    onCopyClick: (number: String) -> Unit,
    onUnblockClick: (BlockingRuleUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CallDetailScreen(
        uiState = uiState,
        onCallClick = onCallClick,
        onCopyClick = onCopyClick,
        onBlockClick = viewModel::onBlockNumber,
        onUnblockClick = onUnblockClick,
        onRetry = viewModel::onRetry,
        onUserMessageShown = viewModel::onUserMessageShown,
        modifier = modifier,
    )
}

/** Chỉ hiển thị [uiState]; mọi quyết định nằm ở ViewModel và [CallDetailUiMapper]. */
@Composable
internal fun CallDetailScreen(
    uiState: CallDetailUiState,
    onCallClick: (number: String) -> Unit,
    onCopyClick: (number: String) -> Unit,
    onBlockClick: () -> Unit,
    onUnblockClick: (BlockingRuleUi) -> Unit,
    onRetry: () -> Unit,
    onUserMessageShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    UserMessageEffect(
        messageRes = (uiState as? CallDetailUiState.Content)?.userMessageRes,
        snackbarHostState = snackbarHostState,
        onUserMessageShown = onUserMessageShown,
    )

    Scaffold(
        modifier = modifier,
        // Toolbar và thanh điều hướng dưới của Activity đã xử lý inset.
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        when (uiState) {
            CallDetailUiState.Loading -> Box(contentModifier, contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            CallDetailUiState.NotFound -> MessageState(
                messageRes = R.string.call_log_detail_not_found,
                modifier = contentModifier,
            )

            CallDetailUiState.Error -> MessageState(
                messageRes = R.string.call_log_error,
                actionRes = R.string.call_log_action_retry,
                onAction = onRetry,
                modifier = contentModifier,
            )

            is CallDetailUiState.Content -> DetailContent(
                detail = uiState.detail,
                onCallClick = onCallClick,
                onCopyClick = onCopyClick,
                onBlockClick = onBlockClick,
                onUnblockClick = onUnblockClick,
                modifier = contentModifier,
            )
        }
    }
}

@Composable
private fun DetailContent(
    detail: CallDetailUiModel,
    onCallClick: (number: String) -> Unit,
    onCopyClick: (number: String) -> Unit,
    onBlockClick: () -> Unit,
    onUnblockClick: (BlockingRuleUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Header(detail)

        detail.blockingRule?.let { rule ->
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(rule.labelRes, rule.pattern),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }

        Column {
            InfoRow(R.string.call_log_detail_label_type, stringResource(detail.typeLabelRes))
            HorizontalDivider()
            InfoRow(R.string.call_log_detail_label_time, detail.timeText)
            HorizontalDivider()
            InfoRow(
                R.string.call_log_detail_label_duration,
                detail.durationText ?: stringResource(R.string.call_log_detail_not_connected),
            )
        }

        Actions(detail, onCallClick, onCopyClick, onBlockClick, onUnblockClick)
    }
}

@Composable
private fun Header(
    detail: CallDetailUiModel,
    modifier: Modifier = Modifier,
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Icon(
            painter = painterResource(detail.typeIconRes),
            contentDescription = stringResource(detail.typeLabelRes),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(32.dp),
        )
        Column(Modifier.padding(start = 16.dp)) {
            Text(
                text = detail.title ?: stringResource(R.string.call_log_private_number),
                style = MaterialTheme.typography.headlineSmall,
            )
            detail.subtitle?.let { subtitle ->
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun InfoRow(
    @StringRes labelRes: Int,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .padding(vertical = 8.dp),
    ) {
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun Actions(
    detail: CallDetailUiModel,
    onCallClick: (number: String) -> Unit,
    onCopyClick: (number: String) -> Unit,
    onBlockClick: () -> Unit,
    onUnblockClick: (BlockingRuleUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier.fillMaxWidth()) {
        detail.number?.let { number ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = { onCallClick(number) }, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.call_log_detail_action_call))
                }
                OutlinedButton(onClick = { onCopyClick(number) }, modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.call_log_detail_action_copy))
                }
            }
        }
        if (detail.canBlock) {
            Button(
                onClick = onBlockClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                ),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.call_log_detail_action_block))
            }
        }
        detail.blockingRule?.let { rule ->
            OutlinedButton(onClick = { onUnblockClick(rule) }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.call_log_detail_action_unblock))
            }
        }
    }
}

/** Hiện snackbar một lần cho [messageRes], theo cách của màn hình Danh sách chặn. */
@Composable
private fun UserMessageEffect(
    @StringRes messageRes: Int?,
    snackbarHostState: SnackbarHostState,
    onUserMessageShown: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    LaunchedEffect(messageRes) {
        if (messageRes == null) return@LaunchedEffect
        val message = context.getString(messageRes)
        onUserMessageShown()
        snackbarHostState.currentSnackbarData?.dismiss()
        // Scope riêng: effect này bị hủy ngay khi messageRes về null.
        scope.launch { snackbarHostState.showSnackbar(message) }
    }
}

// --- Preview: chỉ dùng dữ liệu giả. ---

private val previewDetail = CallDetailUiModel(
    title = "Chị Lan",
    subtitle = "0919009001",
    number = "0919009001",
    typeIconRes = R.drawable.ic_call_incoming,
    typeLabelRes = R.string.call_log_type_incoming,
    timeText = "08:30 · 15/01/2026",
    durationText = "2:05",
    blockingRule = null,
)

private val previewBlockedDetail = CallDetailUiModel(
    title = "0900123456",
    subtitle = null,
    number = "0900123456",
    typeIconRes = R.drawable.ic_call_missed,
    typeLabelRes = R.string.call_log_type_missed,
    timeText = "07:12 · 15/01/2026",
    durationText = null,
    blockingRule = BlockingRuleUi(
        ruleId = 1,
        pattern = "0900",
        isPrefix = true,
        labelRes = R.string.call_log_detail_blocked_by_prefix,
    ),
)

@Composable
private fun CallDetailScreenPreview(uiState: CallDetailUiState) {
    SpamGuardTheme {
        // Surface tô nền theo theme để Preview tối không bị nền trắng của showBackground.
        Surface {
            CallDetailScreen(
                uiState = uiState,
                onCallClick = {},
                onCopyClick = {},
                onBlockClick = {},
                onUnblockClick = {},
                onRetry = {},
                onUserMessageShown = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ContentPreview() = CallDetailScreenPreview(CallDetailUiState.Content(previewDetail))

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ContentDarkPreview() = CallDetailScreenPreview(CallDetailUiState.Content(previewDetail))

@Preview(showBackground = true)
@Composable
private fun BlockedPreview() = CallDetailScreenPreview(CallDetailUiState.Content(previewBlockedDetail))

@Preview(showBackground = true)
@Composable
private fun PrivateNumberPreview() = CallDetailScreenPreview(
    CallDetailUiState.Content(previewDetail.copy(title = null, subtitle = null, number = null)),
)

@Preview(showBackground = true)
@Composable
private fun LoadingPreview() = CallDetailScreenPreview(CallDetailUiState.Loading)

@Preview(showBackground = true)
@Composable
private fun NotFoundPreview() = CallDetailScreenPreview(CallDetailUiState.NotFound)

@Preview(showBackground = true)
@Composable
private fun ErrorPreview() = CallDetailScreenPreview(CallDetailUiState.Error)
