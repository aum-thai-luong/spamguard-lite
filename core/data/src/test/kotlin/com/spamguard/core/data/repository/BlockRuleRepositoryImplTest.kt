package com.spamguard.core.data.repository

import android.database.sqlite.SQLiteException
import com.spamguard.core.data.db.BlockRuleDao
import com.spamguard.core.data.db.BlockRuleEntity
import com.spamguard.core.domain.error.BlockRulesUnavailableException
import com.spamguard.core.domain.model.MatchType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class BlockRuleRepositoryImplTest {

    private val dao = StubBlockRuleDao()
    private val repository = BlockRuleRepositoryImpl(dao, Clock.fixed(Instant.EPOCH, ZoneOffset.UTC))

    @Test
    fun `maps entities to domain rules`() = runTest {
        dao.rules = flowOf(listOf(BlockRuleEntity(id = 3, pattern = "0900", matchType = "PREFIX", createdAtEpochMillis = 0)))

        val rule = repository.observeRules().first().single()

        assertEquals(3L, rule.id)
        assertEquals(MatchType.PREFIX, rule.matchType)
    }

    @Test
    fun `database failure surfaces as BlockRulesUnavailableException`() = runTest {
        dao.rules = flow { throw SQLiteException("disk I/O error") }

        val error = runCatching { repository.observeRules().first() }.exceptionOrNull()

        assertTrue("got $error", error is BlockRulesUnavailableException)
    }

    @Test
    fun `programming errors are not converted`() = runTest {
        dao.rules = flow { throw IllegalStateException("bug") }

        val error = runCatching { repository.observeRules().first() }.exceptionOrNull()

        assertTrue("got $error", error is IllegalStateException)
    }

    private class StubBlockRuleDao : BlockRuleDao {
        var rules: Flow<List<BlockRuleEntity>> = flowOf(emptyList())

        override fun observeAll(): Flow<List<BlockRuleEntity>> = rules
        override suspend fun insert(entity: BlockRuleEntity): Long = error("not used")
        override suspend fun deleteById(ruleId: Long) = error("not used")
        override suspend fun exists(pattern: String, matchType: String): Boolean = error("not used")
    }
}
