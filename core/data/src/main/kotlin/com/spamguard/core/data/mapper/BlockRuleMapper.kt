package com.spamguard.core.data.mapper

import com.spamguard.core.data.db.BlockRuleEntity
import com.spamguard.core.domain.model.BlockRule
import com.spamguard.core.domain.model.MatchType
import java.time.Instant

internal fun BlockRuleEntity.toDomain() = BlockRule(
    id = id,
    pattern = pattern,
    matchType = MatchType.valueOf(matchType),
    createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
)
