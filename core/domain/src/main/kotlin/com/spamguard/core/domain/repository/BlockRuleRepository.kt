package com.spamguard.core.domain.repository

import com.spamguard.core.domain.model.BlockRule
import com.spamguard.core.domain.model.MatchType
import kotlinx.coroutines.flow.Flow

interface BlockRuleRepository {
    fun observeRules(): Flow<List<BlockRule>>

    /** @return id của quy tắc vừa tạo. */
    suspend fun addRule(pattern: String, matchType: MatchType): Long

    suspend fun removeRule(ruleId: Long)

    suspend fun exists(pattern: String, matchType: MatchType): Boolean
}
