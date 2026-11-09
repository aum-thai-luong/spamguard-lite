package com.spamguard.feature.calllog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.spamguard.core.ui.theme.SpamGuardTheme

/** Hỏi lại trước khi chặn đúng [number] (nhấn giữ cuộc gọi ở tab Cuộc gọi). */
@Composable
internal fun ConfirmBlockNumberDialog(
    number: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.call_log_block_title)) },
        text = { Text(stringResource(R.string.call_log_block_message, number)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.call_log_action_block))
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
private fun ConfirmBlockNumberDialogPreview() {
    SpamGuardTheme { ConfirmBlockNumberDialog(number = "0987654321", onConfirm = {}, onDismiss = {}) }
}
