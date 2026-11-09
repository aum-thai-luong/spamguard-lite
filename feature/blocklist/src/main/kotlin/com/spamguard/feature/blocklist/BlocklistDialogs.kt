package com.spamguard.feature.blocklist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.spamguard.core.domain.model.MatchType
import com.spamguard.core.ui.theme.SpamGuardTheme

/**
 * Nhập quy tắc chặn mới. Ô nhập và lựa chọn kiểu so khớp là state của chính hộp thoại
 * (`rememberSaveable`), còn mở/đóng hộp thoại do ViewModel quyết định.
 */
@Composable
internal fun AddBlockRuleDialog(
    onConfirm: (pattern: String, matchType: MatchType) -> Unit,
    onDismiss: () -> Unit,
) {
    var pattern by rememberSaveable { mutableStateOf("") }
    var matchType by rememberSaveable { mutableStateOf(MatchType.PREFIX) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.blocklist_add_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = pattern,
                    onValueChange = { pattern = it },
                    label = { Text(stringResource(R.string.blocklist_input_hint)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                )
                Column(Modifier.padding(top = 8.dp).selectableGroup()) {
                    MatchTypeOption(
                        labelRes = R.string.blocklist_match_prefix_option,
                        selected = matchType == MatchType.PREFIX,
                        onSelect = { matchType = MatchType.PREFIX },
                    )
                    MatchTypeOption(
                        labelRes = R.string.blocklist_match_exact_option,
                        selected = matchType == MatchType.EXACT,
                        onSelect = { matchType = MatchType.EXACT },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(pattern, matchType) }) {
                Text(stringResource(R.string.blocklist_action_add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.blocklist_action_cancel))
            }
        },
    )
}

@Composable
private fun MatchTypeOption(
    labelRes: Int,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .selectable(selected = selected, onClick = onSelect, role = Role.RadioButton),
    ) {
        // Cả dòng nhận chạm; RadioButton chỉ để hiển thị.
        RadioButton(selected = selected, onClick = null)
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 16.dp),
        )
    }
}

/** Hỏi lại trước khi xóa quy tắc [pattern]. */
@Composable
internal fun ConfirmDeleteRuleDialog(
    pattern: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.blocklist_delete_title)) },
        text = { Text(stringResource(R.string.blocklist_delete_message, pattern)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.blocklist_action_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.blocklist_action_cancel))
            }
        },
    )
}

@Preview
@Composable
private fun AddBlockRuleDialogPreview() {
    SpamGuardTheme { AddBlockRuleDialog(onConfirm = { _, _ -> }, onDismiss = {}) }
}

@Preview
@Composable
private fun ConfirmDeleteRuleDialogPreview() {
    SpamGuardTheme { ConfirmDeleteRuleDialog(pattern = "0900", onConfirm = {}, onDismiss = {}) }
}
