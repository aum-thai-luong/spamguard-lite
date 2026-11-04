package com.spamguard.core.domain.spam

import com.spamguard.core.domain.model.MatchType
import com.spamguard.core.domain.model.SpamVerdict
import com.spamguard.core.domain.phone.PhoneNumberNormalizer
import com.spamguard.core.testing.aBlockRule
import com.spamguard.core.testing.aCallRecord
import org.junit.Assert.assertEquals
import org.junit.Test

class SpamClassifierTest {

    private val classifier = SpamClassifier(PhoneNumberNormalizer())

    @Test
    fun `call without matching rule is clean`() {
        val result = classifier.classify(listOf(aCallRecord(number = "0987654321")), rules = emptyList())

        assertEquals(SpamVerdict.Clean, result.single().verdict)
    }

    @Test
    fun `exact rule blocks the same number written in international format`() {
        val rule = aBlockRule(pattern = "0900123456", matchType = MatchType.EXACT)

        val result = classifier.classify(listOf(aCallRecord(number = "+84 900 123 456")), listOf(rule))

        assertEquals(SpamVerdict.Blocked(rule), result.single().verdict)
    }

    @Test
    fun `prefix rule blocks numbers starting with the prefix`() {
        val rule = aBlockRule(pattern = "0900", matchType = MatchType.PREFIX)

        val result = classifier.classify(listOf(aCallRecord(number = "0900123456")), listOf(rule))

        assertEquals(SpamVerdict.Blocked(rule), result.single().verdict)
    }

    @Test
    fun `prefix rule ignores numbers with another prefix`() {
        val rule = aBlockRule(pattern = "0900", matchType = MatchType.PREFIX)

        val result = classifier.classify(listOf(aCallRecord(number = "0987654321")), listOf(rule))

        assertEquals(SpamVerdict.Clean, result.single().verdict)
    }

    @Test
    fun `prefix rule ignores numbers that only contain the prefix in the middle`() {
        val rule = aBlockRule(pattern = "0900", matchType = MatchType.PREFIX)

        val result = classifier.classify(listOf(aCallRecord(number = "0919 009 001")), listOf(rule))

        assertEquals(SpamVerdict.Clean, result.single().verdict)
    }

    @Test
    fun `prefix rule blocks international numbers starting with the prefix`() {
        val rule = aBlockRule(pattern = "0900", matchType = MatchType.PREFIX)

        val result = classifier.classify(listOf(aCallRecord(number = "+84900123456")), listOf(rule))

        assertEquals(SpamVerdict.Blocked(rule), result.single().verdict)
    }

    @Test
    fun `exact rule takes priority over prefix rule`() {
        val prefixRule = aBlockRule(id = 1, pattern = "0900", matchType = MatchType.PREFIX)
        val exactRule = aBlockRule(id = 2, pattern = "0900123456", matchType = MatchType.EXACT)

        val result = classifier.classify(listOf(aCallRecord(number = "0900123456")), listOf(prefixRule, exactRule))

        assertEquals(SpamVerdict.Blocked(exactRule), result.single().verdict)
    }
}
