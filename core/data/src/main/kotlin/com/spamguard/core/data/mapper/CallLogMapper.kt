package com.spamguard.core.data.mapper

import android.provider.CallLog
import com.spamguard.core.data.calllog.CallLogEntry
import com.spamguard.core.domain.model.CallRecord
import com.spamguard.core.domain.model.CallType
import java.time.Duration
import java.time.Instant

internal fun CallLogEntry.toDomain() = CallRecord(
    id = id,
    number = number,
    contactName = cachedName,
    type = type.toCallType(),
    timestamp = Instant.ofEpochMilli(dateEpochMillis),
    duration = Duration.ofSeconds(durationSeconds),
)

private fun Int.toCallType(): CallType = when (this) {
    CallLog.Calls.INCOMING_TYPE -> CallType.INCOMING
    CallLog.Calls.OUTGOING_TYPE -> CallType.OUTGOING
    CallLog.Calls.MISSED_TYPE -> CallType.MISSED
    CallLog.Calls.REJECTED_TYPE -> CallType.REJECTED
    CallLog.Calls.BLOCKED_TYPE -> CallType.BLOCKED
    else -> CallType.UNKNOWN
}
