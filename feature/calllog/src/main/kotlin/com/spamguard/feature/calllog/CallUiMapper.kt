package com.spamguard.feature.calllog

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.spamguard.core.domain.model.CallType
import com.spamguard.core.domain.model.ClassifiedCall
import com.spamguard.core.domain.model.SpamVerdict
import com.spamguard.feature.calllog.model.CallUiModel
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/** Chuyển model domain thành trạng thái và model hiển thị của màn hình nhật ký cuộc gọi. */
class CallUiMapper @Inject constructor(
    private val zoneId: ZoneId,
) {
    private val timeFormatter = DateTimeFormatter.ofPattern(TIME_PATTERN)

    fun toUiState(calls: List<ClassifiedCall>): CallLogUiState =
        if (calls.isEmpty()) {
            CallLogUiState.Empty()
        } else {
            CallLogUiState.Content(
                calls = calls.map(::toUiModel),
                blockedCount = calls.count { it.verdict is SpamVerdict.Blocked },
            )
        }

    fun toUiModel(classifiedCall: ClassifiedCall): CallUiModel {
        val call = classifiedCall.call
        val contactName = call.contactName?.takeIf { it.isNotBlank() }
        val number = call.number.takeIf { it.isNotBlank() }
        return CallUiModel(
            id = call.id,
            title = contactName ?: number,
            subtitle = number.takeIf { contactName != null },
            timeText = timeFormatter.format(call.timestamp.atZone(zoneId)),
            typeIconRes = call.type.iconRes(),
            typeLabelRes = call.type.labelRes(),
            blockedPattern = (classifiedCall.verdict as? SpamVerdict.Blocked)?.rule?.pattern,
        )
    }

    @DrawableRes
    private fun CallType.iconRes(): Int = when (this) {
        CallType.INCOMING, CallType.UNKNOWN -> R.drawable.ic_call_incoming
        CallType.OUTGOING -> R.drawable.ic_call_outgoing
        CallType.MISSED, CallType.REJECTED -> R.drawable.ic_call_missed
        CallType.BLOCKED -> R.drawable.ic_call_blocked
    }

    @StringRes
    private fun CallType.labelRes(): Int = when (this) {
        CallType.INCOMING -> R.string.call_log_type_incoming
        CallType.OUTGOING -> R.string.call_log_type_outgoing
        CallType.MISSED -> R.string.call_log_type_missed
        CallType.REJECTED -> R.string.call_log_type_rejected
        CallType.BLOCKED -> R.string.call_log_type_blocked
        CallType.UNKNOWN -> R.string.call_log_type_unknown
    }

    private companion object {
        const val TIME_PATTERN = "HH:mm · dd/MM/yyyy"
    }
}
