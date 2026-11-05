package com.spamguard.feature.calllog

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.spamguard.core.domain.model.CallType

/** Icon và nhãn của từng loại cuộc gọi; dùng chung cho danh sách và màn hình chi tiết. */
@DrawableRes
internal fun CallType.iconRes(): Int = when (this) {
    CallType.INCOMING, CallType.UNKNOWN -> R.drawable.ic_call_incoming
    CallType.OUTGOING -> R.drawable.ic_call_outgoing
    CallType.MISSED, CallType.REJECTED -> R.drawable.ic_call_missed
    CallType.BLOCKED -> R.drawable.ic_call_blocked
}

@StringRes
internal fun CallType.labelRes(): Int = when (this) {
    CallType.INCOMING -> R.string.call_log_type_incoming
    CallType.OUTGOING -> R.string.call_log_type_outgoing
    CallType.MISSED -> R.string.call_log_type_missed
    CallType.REJECTED -> R.string.call_log_type_rejected
    CallType.BLOCKED -> R.string.call_log_type_blocked
    CallType.UNKNOWN -> R.string.call_log_type_unknown
}
