package com.spamguard.core.domain.usecase

import com.spamguard.core.domain.model.MatchType
import com.spamguard.core.domain.phone.PhoneNumberNormalizer
import com.spamguard.core.testing.FakeBlockRuleRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AddBlockRuleUseCaseTest {

    private val repository = FakeBlockRuleRepository()
    private val addBlockRule = AddBlockRuleUseCase(repository, PhoneNumberNormalizer())

    @Test
    fun `stores the normalized pattern`() = runTest {
        val result = addBlockRule("+84 900", MatchType.PREFIX)

        assertTrue(result is AddBlockRuleResult.Added)
        assertEquals("0900", repository.observeRules().first().single().pattern)
    }

    @Test
    fun `rejects a prefix that is too short`() = runTest {
        val result = addBlockRule("0", MatchType.PREFIX)

        assertEquals(AddBlockRuleResult.InvalidPattern, result)
    }

    @Test
    fun `rejects an exact number that is too short`() = runTest {
        val result = addBlockRule("0900", MatchType.EXACT)

        assertEquals(AddBlockRuleResult.InvalidPattern, result)
    }

    @Test
    fun `rejects a rule that already exists`() = runTest {
        addBlockRule("0900", MatchType.PREFIX)

        val result = addBlockRule("0900", MatchType.PREFIX)

        assertEquals(AddBlockRuleResult.Duplicate, result)
    }

    @Test
    fun `same pattern with another match type is not a duplicate`() = runTest {
        addBlockRule("0900123456", MatchType.PREFIX)

        val result = addBlockRule("0900123456", MatchType.EXACT)

        assertTrue(result is AddBlockRuleResult.Added)
    }
}
