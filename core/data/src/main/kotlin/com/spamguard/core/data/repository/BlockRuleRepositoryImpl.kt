package com.spamguard.core.data.repository

import com.spamguard.core.data.db.BlockRuleDao
import com.spamguard.core.data.db.BlockRuleEntity
import com.spamguard.core.data.mapper.toDomain
import com.spamguard.core.domain.model.BlockRule
import com.spamguard.core.domain.model.MatchType
import com.spamguard.core.domain.repository.BlockRuleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import javax.inject.Inject

internal class BlockRuleRepositoryImpl @Inject constructor(
    private val dao: BlockRuleDao,
    private val clock: Clock,
) : BlockRuleRepository {

    override fun observeRules(): Flow<List<BlockRule>> =
        dao.observeAll().map { entities -> entities.map(BlockRuleEntity::toDomain) }

    override suspend fun addRule(pattern: String, matchType: MatchType): Long =
        dao.insert(
            BlockRuleEntity(
                pattern = pattern,
                matchType = matchType.name,
                createdAtEpochMillis = clock.millis(),
            ),
        )

    override suspend fun removeRule(ruleId: Long) = dao.deleteById(ruleId)

    override suspend fun exists(pattern: String, matchType: MatchType): Boolean =
        dao.exists(pattern, matchType.name)
}
