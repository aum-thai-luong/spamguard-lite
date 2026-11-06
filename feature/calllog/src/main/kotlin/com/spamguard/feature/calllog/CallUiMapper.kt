package com.spamguard.feature.calllog

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
            number = number,
            timeText = timeFormatter.format(call.timestamp.atZone(zoneId)),
            typeIconRes = call.type.iconRes(),
            typeLabelRes = call.type.labelRes(),
            blockedPattern = (classifiedCall.verdict as? SpamVerdict.Blocked)?.rule?.pattern,
        )
    }

    internal companion object {
        /** Dùng chung với màn hình chi tiết để thời gian hiển thị giống nhau. */
        const val TIME_PATTERN = "HH:mm · dd/MM/yyyy"
    }
}
