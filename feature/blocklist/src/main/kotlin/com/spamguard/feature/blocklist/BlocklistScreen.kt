package com.spamguard.feature.blocklist

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spamguard.core.domain.model.MatchType
import com.spamguard.core.ui.theme.SpamGuardTheme
import com.spamguard.feature.blocklist.model.BlockRuleUiModel
import com.spamguard.feature.blocklist.screening.CallScreeningRole
import kotlinx.coroutines.launch

/**
 * Nối [BlocklistViewModel] với [BlocklistScreen]. Vai trò sàng lọc cuộc gọi cần Android API
 * (RoleManager, Activity Result) nên Route xử lý; mọi trạng thái khác nằm trong ViewModel.
 */
@Composable
internal fun BlocklistRoute(
    modifier: Modifier = Modifier,
    viewModel: BlocklistViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Kết quả được đọc lại khi màn hình resume, ngay sau khi hộp thoại vai trò đóng.
    val screeningRoleLauncher = rememberLauncherForActivityResult(StartActivityForResult()) {}
    // Người dùng có thể bật/tắt vai trò trong Cài đặt bất cứ lúc nào.
    LifecycleResumeEffect(Unit) {
        viewModel.onScreeningStatusChecked(CallScreeningRole.status(context))
        onPauseOrDispose {}
    }

    BlocklistScreen(
        uiState = uiState,
        onAddClick = viewModel::onAddClick,
        onAddRule = viewModel::onAddRule,
        onAddDismiss = viewModel::onAddDialogDismissed,
        onDeleteClick = { rule -> viewModel.onDeleteClick(rule.id) },
        onDeleteConfirm = viewModel::onDeleteConfirmed,
        onDeleteDismiss = viewModel::onDeleteDismissed,
        onEnableScreeningClick = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                screeningRoleLauncher.launch(CallScreeningRole.requestIntent(context))
            }
        },
        onUserMessageShown = viewModel::onUserMessageShown,
        modifier = modifier,
    )
}

/** Chỉ hiển thị [uiState], kể cả hộp thoại; mọi quyết định nằm ở ViewModel và [BlockRuleUiMapper]. */
@Composable
internal fun BlocklistScreen(
    uiState: BlocklistUiState,
    onAddClick: () -> Unit,
    onAddRule: (pattern: String, matchType: MatchType) -> Unit,
    onAddDismiss: () -> Unit,
    onDeleteClick: (BlockRuleUiModel) -> Unit,
    onDeleteConfirm: () -> Unit,
    onDeleteDismiss: () -> Unit,
    onEnableScreeningClick: () -> Unit,
    onUserMessageShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (uiState.isAddDialogVisible) {
        AddBlockRuleDialog(onConfirm = onAddRule, onDismiss = onAddDismiss)
    }
    uiState.pendingDeletion?.let { rule ->
        ConfirmDeleteRuleDialog(pattern = rule.pattern, onConfirm = onDeleteConfirm, onDismiss = onDeleteDismiss)
    }

    val snackbarHostState = remember { SnackbarHostState() }
    UserMessageEffect(uiState.userMessageRes, snackbarHostState, onUserMessageShown)

    Scaffold(
        modifier = modifier,
        // Thanh trên và thanh điều hướng dưới của app đã xử lý inset.
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(stringResource(R.string.blocklist_action_add_rule)) },
                icon = { Icon(painterResource(R.drawable.ic_add), contentDescription = null) },
                onClick = onAddClick,
            )
        },
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            ScreeningBanner(status = uiState.screeningStatus, onEnableClick = onEnableScreeningClick)

            val contentModifier = Modifier
                .fillMaxWidth()
                .weight(1f)
            when {
                uiState.isLoading -> Box(contentModifier, contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

                uiState.isEmpty -> Box(contentModifier, contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.blocklist_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(32.dp),
                    )
                }

                else -> RuleList(
                    rules = uiState.rules,
                    onDeleteClick = onDeleteClick,
                    modifier = contentModifier,
                )
            }
        }
    }
}

/** Cho biết app chỉ đánh dấu hay đang từ chối thật các cuộc gọi khớp quy tắc. */
@Composable
private fun ScreeningBanner(
    status: ScreeningStatus,
    onEnableClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (status == ScreeningStatus.Unsupported) return
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .heightIn(min = 56.dp)
                .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        ) {
            val textRes = when (status) {
                ScreeningStatus.Enabled -> R.string.blocklist_screening_enabled
                else -> R.string.blocklist_screening_disabled
            }
            Text(
                text = stringResource(textRes),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            if (status == ScreeningStatus.Disabled) {
                TextButton(onClick = onEnableClick) {
                    Text(stringResource(R.string.blocklist_action_enable_screening))
                }
            }
        }
    }
}

/**
 * Hiện snackbar cho thông báo một lần. Báo [onUserMessageShown] ngay khi bắt đầu hiện
 * (như bản XML) để xoay màn hình không hiện lại; thông báo mới thay thông báo đang hiện.
 */
@Composable
private fun UserMessageEffect(
    messageRes: Int?,
    snackbarHostState: SnackbarHostState,
    onUserMessageShown: () -> Unit,
) {
    val resources = LocalResources.current
    val scope = rememberCoroutineScope()
    LaunchedEffect(messageRes) {
        if (messageRes == null) return@LaunchedEffect
        val message = resources.getString(messageRes)
        onUserMessageShown()
        snackbarHostState.currentSnackbarData?.dismiss()
        // Scope riêng: effect này bị hủy ngay khi messageRes về null.
        scope.launch { snackbarHostState.showSnackbar(message) }
    }
}

@Composable
private fun RuleList(
    rules: List<BlockRuleUiModel>,
    onDeleteClick: (BlockRuleUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Chừa chỗ cho FAB để không che nút xóa của dòng cuối.
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(bottom = 88.dp)) {
        items(rules, key = BlockRuleUiModel::id) { rule ->
            RuleItem(rule = rule, onDeleteClick = { onDeleteClick(rule) })
        }
    }
}

@Composable
private fun RuleItem(
    rule: BlockRuleUiModel,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .padding(start = 16.dp, top = 12.dp, end = 4.dp, bottom = 12.dp),
    ) {
        Column(Modifier.weight(1f)) {
            Text(text = rule.pattern, style = MaterialTheme.typography.titleMedium)
            Text(
                text = stringResource(
                    R.string.blocklist_rule_details,
                    stringResource(rule.matchTypeLabelRes),
                    rule.createdText,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onDeleteClick) {
            Icon(
                painter = painterResource(R.drawable.ic_delete),
                contentDescription = stringResource(R.string.blocklist_delete_rule_description, rule.pattern),
            )
        }
    }
}

private val previewRules = listOf(
    BlockRuleUiModel(
        id = 1,
        pattern = "0900",
        matchTypeLabelRes = R.string.blocklist_match_prefix,
        createdText = "15/01/2026",
    ),
    BlockRuleUiModel(
        id = 2,
        pattern = "0987654321",
        matchTypeLabelRes = R.string.blocklist_match_exact,
        createdText = "16/01/2026",
    ),
)

@Composable
private fun BlocklistScreenPreview(uiState: BlocklistUiState) {
    SpamGuardTheme {
        BlocklistScreen(
            uiState = uiState,
            onAddClick = {},
            onAddRule = { _, _ -> },
            onAddDismiss = {},
            onDeleteClick = {},
            onDeleteConfirm = {},
            onDeleteDismiss = {},
            onEnableScreeningClick = {},
            onUserMessageShown = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RulesPreview() =
    BlocklistScreenPreview(BlocklistUiState(isLoading = false, rules = previewRules))

@Preview(showBackground = true)
@Composable
private fun ScreeningDisabledPreview() = BlocklistScreenPreview(
    BlocklistUiState(isLoading = false, rules = previewRules, screeningStatus = ScreeningStatus.Disabled),
)

@Preview(showBackground = true)
@Composable
private fun ScreeningEnabledPreview() = BlocklistScreenPreview(
    BlocklistUiState(isLoading = false, rules = previewRules, screeningStatus = ScreeningStatus.Enabled),
)

@Preview(showBackground = true)
@Composable
private fun EmptyPreview() = BlocklistScreenPreview(BlocklistUiState(isLoading = false))

@Preview(showBackground = true)
@Composable
private fun LoadingPreview() = BlocklistScreenPreview(BlocklistUiState())

@Preview(showBackground = true)
@Composable
private fun DeleteDialogPreview() = BlocklistScreenPreview(
    BlocklistUiState(isLoading = false, rules = previewRules, pendingDeletion = previewRules.first()),
)
