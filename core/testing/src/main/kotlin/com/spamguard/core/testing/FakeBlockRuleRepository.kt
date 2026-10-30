package com.spamguard.core.testing

import com.spamguard.core.domain.model.BlockRule
import com.spamguard.core.domain.model.MatchType
import com.spamguard.core.domain.repository.BlockRuleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.time.Instant

class FakeBlockRuleRepository : BlockRuleRepository {

    private val rules = MutableStateFlow<List<BlockRule>>(emptyList())
    private var nextId = 1L

    override fun observeRules(): Flow<List<BlockRule>> = rules

    override suspend fun addRule(pattern: String, matchType: MatchType): Long {
        val rule = BlockRule(id = nextId++, pattern = pattern, matchType = matchType, createdAt = Instant.EPOCH)
        rules.update { current -> current + rule }
        return rule.id
    }

    override suspend fun removeRule(ruleId: Long) {
        rules.update { current -> current.filterNot { it.id == ruleId } }
    }

    override suspend fun exists(pattern: String, matchType: MatchType): Boolean =
        rules.value.any { it.pattern == pattern && it.matchType == matchType }
}
