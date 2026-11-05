package com.spamguard.feature.calllog.detail

import com.spamguard.core.domain.model.CallType
import com.spamguard.core.domain.model.ClassifiedCall
import com.spamguard.core.domain.model.MatchType
import com.spamguard.core.domain.model.SpamVerdict
import com.spamguard.core.testing.aBlockRule
import com.spamguard.core.testing.aCallRecord
import com.spamguard.feature.calllog.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

class CallDetailUiMapperTest {

    private val mapper = CallDetailUiMapper(ZoneOffset.ofHours(7))

    @Test
    fun `contact call shows name with number below and local time`() {
        val detail = mapper.toUiModel(
            clean(aCallRecord(number = "0919009001", contactName = "Chị Lan", timestamp = Instant.parse("2026-01-15T01:30:00Z"))),
        )

        assertEquals("Chị Lan", detail.title)
        assertEquals("0919009001", detail.subtitle)
        assertEquals("0919009001", detail.number)
        assertEquals("08:30 · 15/01/2026", detail.timeText)
    }

    @Test
    fun `duration uses minutes and seconds, or hours when long`() {
        assertEquals("2:05", mapper.toUiModel(clean(aCallRecord(duration = Duration.ofSeconds(125)))).durationText)
        assertEquals("1:01:01", mapper.toUiModel(clean(aCallRecord(duration = Duration.ofSeconds(3_661)))).durationText)
    }

    @Test
    fun `call that never connected has no duration`() {
        assertNull(mapper.toUiModel(clean(aCallRecord(duration = Duration.ZERO))).durationText)
    }

    @Test
    fun `hidden number cannot be called or blocked`() {
        val detail = mapper.toUiModel(clean(aCallRecord(number = "", type = CallType.MISSED)))

        assertNull(detail.title)
        assertNull(detail.number)
        assertFalse(detail.canCall)
        assertFalse(detail.canBlock)
        assertEquals(R.string.call_log_type_missed, detail.typeLabelRes)
    }

    @Test
    fun `clean number can be called and blocked`() {
        val detail = mapper.toUiModel(clean(aCallRecord(number = "0987654321")))

        assertTrue(detail.canCall)
        assertTrue(detail.canBlock)
        assertNull(detail.blockingRule)
    }

    @Test
    fun `prefix rule is labelled as prefix and hides the block action`() {
        val rule = aBlockRule(id = 5, pattern = "0900", matchType = MatchType.PREFIX)
        val detail = mapper.toUiModel(ClassifiedCall(aCallRecord(number = "0900123456"), SpamVerdict.Blocked(rule)))

        assertEquals(BlockingRuleUi(5, "0900", isPrefix = true, R.string.call_log_detail_blocked_by_prefix), detail.blockingRule)
        assertFalse(detail.canBlock)
        assertTrue(detail.canCall)
    }

    @Test
    fun `exact rule is labelled as exact`() {
        val rule = aBlockRule(pattern = "0900123456", matchType = MatchType.EXACT)
        val detail = mapper.toUiModel(ClassifiedCall(aCallRecord(number = "0900123456"), SpamVerdict.Blocked(rule)))

        assertEquals(R.string.call_log_detail_blocked_by_exact, detail.blockingRule?.labelRes)
        assertFalse(detail.blockingRule!!.isPrefix)
    }

    private fun clean(call: com.spamguard.core.domain.model.CallRecord) = ClassifiedCall(call, SpamVerdict.Clean)
}
