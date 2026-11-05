package com.spamguard.feature.calllog.detail

import com.spamguard.core.domain.model.ClassifiedCall
import com.spamguard.core.domain.model.MatchType
import com.spamguard.core.domain.model.SpamVerdict
import com.spamguard.feature.calllog.CallUiMapper
import com.spamguard.feature.calllog.R
import com.spamguard.feature.calllog.iconRes
import com.spamguard.feature.calllog.labelRes
import java.time.Duration
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

/** Chuyển một cuộc gọi đã phân loại thành model hiển thị của màn hình chi tiết. */
class CallDetailUiMapper @Inject constructor(
    private val zoneId: ZoneId,
) {
    private val timeFormatter = DateTimeFormatter.ofPattern(CallUiMapper.TIME_PATTERN)

    fun toUiModel(classifiedCall: ClassifiedCall): CallDetailUiModel {
        val call = classifiedCall.call
        val contactName = call.contactName?.takeIf { it.isNotBlank() }
        val number = call.number.takeIf { it.isNotBlank() }
        val rule = (classifiedCall.verdict as? SpamVerdict.Blocked)?.rule
        return CallDetailUiModel(
            title = contactName ?: number,
            subtitle = number.takeIf { contactName != null },
            number = number,
            typeIconRes = call.type.iconRes(),
            typeLabelRes = call.type.labelRes(),
            timeText = timeFormatter.format(call.timestamp.atZone(zoneId)),
            durationText = formatDuration(call.duration),
            blockingRule = rule?.let {
                val isPrefix = it.matchType == MatchType.PREFIX
                BlockingRuleUi(
                    ruleId = it.id,
                    pattern = it.pattern,
                    isPrefix = isPrefix,
                    labelRes = if (isPrefix) {
                        R.string.call_log_detail_blocked_by_prefix
                    } else {
                        R.string.call_log_detail_blocked_by_exact
                    },
                )
            },
        )
    }

    /** `m:ss`, hoặc `h:mm:ss` khi từ một giờ trở lên; `null` khi không kết nối. */
    private fun formatDuration(duration: Duration): String? {
        if (duration.isZero || duration.isNegative) return null
        // Không dùng toMinutesPart()/toSecondsPart(): chỉ có từ API 31, minSdk là 26.
        val totalSeconds = duration.seconds
        val hours = totalSeconds / SECONDS_PER_HOUR
        val minutes = totalSeconds % SECONDS_PER_HOUR / SECONDS_PER_MINUTE
        val seconds = totalSeconds % SECONDS_PER_MINUTE
        return if (hours > 0) {
            String.format(Locale.ROOT, "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.ROOT, "%d:%02d", minutes, seconds)
        }
    }

    private companion object {
        const val SECONDS_PER_MINUTE = 60L
        const val SECONDS_PER_HOUR = 3_600L
    }
}
