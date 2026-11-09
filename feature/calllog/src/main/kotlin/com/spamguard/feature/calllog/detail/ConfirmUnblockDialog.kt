package com.spamguard.feature.calllog.detail

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.spamguard.core.ui.theme.SpamGuardTheme
import com.spamguard.feature.calllog.R

/** Hỏi lại trước khi bỏ chặn. Quy tắc đầu số có thể chặn nhiều số, nên thông điệp nói rõ điều đó. */
@Composable
internal fun ConfirmUnblockDialog(
    rule: BlockingRuleUi,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val messageRes = if (rule.isPrefix) {
        R.string.call_log_detail_unblock_message_prefix
    } else {
        R.string.call_log_detail_unblock_message_exact
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.call_log_detail_unblock_title)) },
        text = { Text(stringResource(messageRes, rule.pattern)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.call_log_detail_action_unblock))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.call_log_detail_action_cancel))
            }
        },
    )
}

@Preview
@Composable
private fun ConfirmUnblockDialogPreview() {
    SpamGuardTheme {
        ConfirmUnblockDialog(
            rule = BlockingRuleUi(
                ruleId = 1,
                pattern = "0900",
                isPrefix = true,
                labelRes = R.string.call_log_detail_blocked_by_prefix,
            ),
            onConfirm = {},
            onDismiss = {},
        )
    }
}
