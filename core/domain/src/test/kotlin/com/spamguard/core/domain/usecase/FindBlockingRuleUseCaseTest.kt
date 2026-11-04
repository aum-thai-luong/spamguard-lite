package com.spamguard.core.domain.usecase

import com.spamguard.core.domain.model.MatchType
import com.spamguard.core.domain.phone.PhoneNumberNormalizer
import com.spamguard.core.domain.spam.SpamClassifier
import com.spamguard.core.testing.FakeBlockRuleRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FindBlockingRuleUseCaseTest {

    private val repository = FakeBlockRuleRepository()
    private val findBlockingRule = FindBlockingRuleUseCase(repository, SpamClassifier(PhoneNumberNormalizer()))

    @Test
    fun `no rules means the call is not blocked`() = runTest {
        assertNull(findBlockingRule("0900123456"))
    }

    @Test
    fun `prefix rule blocks an incoming international number`() = runTest {
        repository.addRule("0900", MatchType.PREFIX)

        val rule = findBlockingRule("+84 900 123 456")

        assertEquals("0900", rule?.pattern)
    }

    @Test
    fun `prefix rule does not block a number containing the prefix elsewhere`() = runTest {
        repository.addRule("0900", MatchType.PREFIX)

        assertNull(findBlockingRule("0919009001"))
    }

    @Test
    fun `exact rule is reported before a matching prefix rule`() = runTest {
        repository.addRule("0900", MatchType.PREFIX)
        repository.addRule("0900123456", MatchType.EXACT)

        val rule = findBlockingRule("0900123456")

        assertEquals(MatchType.EXACT, rule?.matchType)
    }

    @Test
    fun `uses the rules as they are at the time of the call`() = runTest {
        val ruleId = repository.addRule("0900", MatchType.PREFIX)
        repository.removeRule(ruleId)

        assertNull(findBlockingRule("0900123456"))
    }
}
