package com.spamguard.feature.blocklist

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spamguard.feature.blocklist.model.BlockRuleUiModel
import kotlinx.coroutines.launch

/** Nối [BlocklistViewModel] với [BlocklistScreen]; dialog do Fragment mở qua [onAddClick] và [onDeleteClick]. */
@Composable
internal fun BlocklistRoute(
    viewModel: BlocklistViewModel,
    onAddClick: () -> Unit,
    onDeleteClick: (BlockRuleUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BlocklistScreen(
        uiState = uiState,
        onAddClick = onAddClick,
        onDeleteClick = onDeleteClick,
        onUserMessageShown = viewModel::onUserMessageShown,
        modifier = modifier,
    )
}

/** Chỉ hiển thị [uiState]; mọi quyết định nằm ở ViewModel và [BlockRuleUiMapper]. */
@Composable
internal fun BlocklistScreen(
    uiState: BlocklistUiState,
    onAddClick: () -> Unit,
    onDeleteClick: (BlockRuleUiModel) -> Unit,
    onUserMessageShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    UserMessageEffect(uiState.userMessageRes, snackbarHostState, onUserMessageShown)

    Scaffold(
        modifier = modifier,
        // Toolbar và thanh điều hướng dưới của Activity đã xử lý inset.
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
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
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
    BlocklistTheme {
        BlocklistScreen(uiState = uiState, onAddClick = {}, onDeleteClick = {}, onUserMessageShown = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun RulesPreview() =
    BlocklistScreenPreview(BlocklistUiState(isLoading = false, rules = previewRules))

@Preview(showBackground = true)
@Composable
private fun EmptyPreview() = BlocklistScreenPreview(BlocklistUiState(isLoading = false))

@Preview(showBackground = true)
@Composable
private fun LoadingPreview() = BlocklistScreenPreview(BlocklistUiState())
