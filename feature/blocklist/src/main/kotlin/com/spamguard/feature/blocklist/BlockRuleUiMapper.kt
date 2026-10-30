package com.spamguard.feature.blocklist

import androidx.annotation.StringRes
import com.spamguard.core.domain.model.BlockRule
import com.spamguard.core.domain.model.MatchType
import com.spamguard.feature.blocklist.model.BlockRuleUiModel
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class BlockRuleUiMapper @Inject constructor(
    private val zoneId: ZoneId,
) {
    private val dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN)

    fun toUiModel(rule: BlockRule) = BlockRuleUiModel(
        id = rule.id,
        pattern = rule.pattern,
        matchTypeLabelRes = rule.matchType.labelRes(),
        createdText = dateFormatter.format(rule.createdAt.atZone(zoneId)),
    )

    @StringRes
    private fun MatchType.labelRes(): Int = when (this) {
        MatchType.EXACT -> R.string.blocklist_match_exact
        MatchType.PREFIX -> R.string.blocklist_match_prefix
    }

    private companion object {
        const val DATE_PATTERN = "dd/MM/yyyy"
    }
}
