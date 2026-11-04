package com.spamguard.core.domain.usecase

import com.spamguard.core.domain.model.BlockRule
import com.spamguard.core.domain.model.MatchType
import com.spamguard.core.domain.model.SpamVerdict
import com.spamguard.core.domain.phone.PhoneNumberNormalizer
import com.spamguard.core.domain.repository.BlockRuleRepository
import com.spamguard.core.domain.spam.SpamClassifier
import com.spamguard.core.testing.FakeBlockRuleRepository
import com.spamguard.core.testing.FakeCallLogRepository
import com.spamguard.core.testing.aBlockRule
import com.spamguard.core.testing.aCallRecord
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetClassifiedCallsUseCaseTest {

    private val callLogRepository = FakeCallLogRepository()
    private val blockRuleRepository = FakeBlockRuleRepository()
    private val getClassifiedCalls = GetClassifiedCallsUseCase(
        callLogRepository = callLogRepository,
        blockRuleRepository = blockRuleRepository,
        classifier = SpamClassifier(PhoneNumberNormalizer()),
    )

    @Test
    fun `respects the limit`() = runTest {
        callLogRepository.calls = (1L..5L).map { aCallRecord(id = it) }

        val result = getClassifiedCalls(limit = 3).first()

        assertEquals(listOf(1L, 2L, 3L), result.map { it.call.id })
    }

    @Test
    fun `re-classifies calls when block rules change`() = runTest {
        callLogRepository.calls = listOf(aCallRecord(number = "0900123456"))
        val emissions = async { getClassifiedCalls(limit = 10).take(2).toList() }
        yield()

        blockRuleRepository.addRule("0900", MatchType.PREFIX)

        val (beforeRule, afterRule) = emissions.await()
        assertEquals(SpamVerdict.Clean, beforeRule.single().verdict)
        assertTrue(afterRule.single().verdict is SpamVerdict.Blocked)
    }

    @Test
    fun `does not re-classify when the same rules are emitted again`() = runTest {
        callLogRepository.calls = listOf(aCallRecord(number = "0900123456"))
        val rules = listOf(aBlockRule(pattern = "0900", matchType = MatchType.PREFIX))
        // Room phát lại sau mỗi lần ghi vào bảng, kể cả khi kết quả không đổi.
        val repeatingRepository = object : BlockRuleRepository {
            override fun observeRules(): Flow<List<BlockRule>> = flowOf(rules, rules.toList())
            override suspend fun addRule(pattern: String, matchType: MatchType): Long = error("not used")
            override suspend fun removeRule(ruleId: Long) = error("not used")
            override suspend fun exists(pattern: String, matchType: MatchType): Boolean = error("not used")
        }
        val useCase = GetClassifiedCallsUseCase(
            callLogRepository = callLogRepository,
            blockRuleRepository = repeatingRepository,
            classifier = SpamClassifier(PhoneNumberNormalizer()),
        )

        val emissions = useCase(limit = 10).toList()

        assertEquals(1, emissions.size)
    }
}
