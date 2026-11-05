package com.spamguard.core.domain.usecase

import com.spamguard.core.domain.error.CallLogUnavailableException
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GetCallDetailUseCaseTest {

    private val callLogRepository = FakeCallLogRepository()
    private val blockRuleRepository = FakeBlockRuleRepository()
    private val getCallDetail = GetCallDetailUseCase(
        callLogRepository = callLogRepository,
        blockRuleRepository = blockRuleRepository,
        classifier = SpamClassifier(PhoneNumberNormalizer()),
    )

    @Test
    fun `missing call emits null`() = runTest {
        callLogRepository.calls = listOf(aCallRecord(id = 1))

        assertNull(getCallDetail(callId = 99).first())
    }

    @Test
    fun `call matching a rule is blocked by that rule`() = runTest {
        callLogRepository.calls = listOf(aCallRecord(id = 7, number = "+84 900 123 456"))
        val ruleId = blockRuleRepository.addRule("0900", MatchType.PREFIX)

        val detail = getCallDetail(callId = 7).first()

        assertEquals(7L, detail?.call?.id)
        assertEquals(ruleId, (detail?.verdict as SpamVerdict.Blocked).rule.id)
    }

    @Test
    fun `adding a rule re-classifies the call`() = runTest {
        callLogRepository.calls = listOf(aCallRecord(id = 7, number = "0900123456"))

        val emissions = async { getCallDetail(callId = 7).take(2).toList() }
        testScheduler.runCurrent()
        blockRuleRepository.addRule("0900123456", MatchType.EXACT)

        val (before, after) = emissions.await()
        assertEquals(SpamVerdict.Clean, before?.verdict)
        assertTrue(after?.verdict is SpamVerdict.Blocked)
    }

    @Test
    fun `same rules emitted again do not re-emit`() = runTest {
        callLogRepository.calls = listOf(aCallRecord(id = 7))
        val rules = listOf(aBlockRule())
        val repeating = object : BlockRuleRepository {
            override fun observeRules(): Flow<List<BlockRule>> = flowOf(rules, rules.toList())
            override suspend fun addRule(pattern: String, matchType: MatchType): Long = error("not used")
            override suspend fun removeRule(ruleId: Long) = error("not used")
            override suspend fun exists(pattern: String, matchType: MatchType): Boolean = error("not used")
        }
        val useCase = GetCallDetailUseCase(callLogRepository, repeating, SpamClassifier(PhoneNumberNormalizer()))

        assertEquals(1, useCase(callId = 7).toList().size)
    }

    @Test
    fun `unreadable call log surfaces as CallLogUnavailableException`() = runTest {
        callLogRepository.failure = CallLogUnavailableException("no cursor")

        val error = runCatching { getCallDetail(callId = 1).first() }.exceptionOrNull()

        assertTrue(error is CallLogUnavailableException)
    }
}
