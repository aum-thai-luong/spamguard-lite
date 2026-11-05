package com.spamguard.core.data.repository

import android.database.sqlite.SQLiteException
import com.spamguard.core.data.db.BlockRuleDao
import com.spamguard.core.data.db.BlockRuleEntity
import com.spamguard.core.data.mapper.toDomain
import com.spamguard.core.domain.error.BlockRulesUnavailableException
import com.spamguard.core.domain.model.BlockRule
import com.spamguard.core.domain.model.MatchType
import com.spamguard.core.domain.repository.BlockRuleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.time.Clock
import javax.inject.Inject

internal class BlockRuleRepositoryImpl @Inject constructor(
    private val dao: BlockRuleDao,
    private val clock: Clock,
) : BlockRuleRepository {

    override fun observeRules(): Flow<List<BlockRule>> =
        dao.observeAll()
            .map { entities -> entities.map(BlockRuleEntity::toDomain) }
            // Chỉ đổi lỗi hạ tầng của SQLite; lỗi lập trình vẫn lộ ra nguyên dạng.
            .catch { error ->
                if (error is SQLiteException) {
                    throw BlockRulesUnavailableException("Block rules could not be read", error)
                }
                throw error
            }

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
