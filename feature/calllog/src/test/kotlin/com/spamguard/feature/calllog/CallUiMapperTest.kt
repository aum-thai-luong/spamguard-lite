package com.spamguard.feature.calllog

import com.spamguard.core.domain.model.CallType
import com.spamguard.core.domain.model.ClassifiedCall
import com.spamguard.core.domain.model.SpamVerdict
import com.spamguard.core.testing.aBlockRule
import com.spamguard.core.testing.aCallRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneOffset

class CallUiMapperTest {

    private val mapper = CallUiMapper(ZoneOffset.ofHours(7))

    @Test
    fun `contact name becomes title and number becomes subtitle`() {
        val call = aCallRecord(number = "0919009001", contactName = "Chị Lan")

        val model = mapper.toUiModel(ClassifiedCall(call, SpamVerdict.Clean))

        assertEquals("Chị Lan", model.title)
        assertEquals("0919009001", model.subtitle)
    }

    @Test
    fun `unknown contact shows number as title without subtitle`() {
        val model = mapper.toUiModel(ClassifiedCall(aCallRecord(number = "0987654321"), SpamVerdict.Clean))

        assertEquals("0987654321", model.title)
        assertNull(model.subtitle)
    }

    @Test
    fun `hidden number has no title`() {
        val model = mapper.toUiModel(ClassifiedCall(aCallRecord(number = ""), SpamVerdict.Clean))

        assertNull(model.title)
    }

    @Test
    fun `time is formatted in the given zone`() {
        val call = aCallRecord(timestamp = Instant.parse("2026-01-15T01:30:00Z"))

        val model = mapper.toUiModel(ClassifiedCall(call, SpamVerdict.Clean))

        assertEquals("08:30 · 15/01/2026", model.timeText)
    }

    @Test
    fun `blocked call exposes the matching pattern`() {
        val verdict = SpamVerdict.Blocked(aBlockRule(pattern = "0900"))

        val model = mapper.toUiModel(ClassifiedCall(aCallRecord(type = CallType.MISSED), verdict))

        assertTrue(model.isBlocked)
        assertEquals("0900", model.blockedPattern)
        assertEquals(R.drawable.ic_call_missed, model.typeIconRes)
    }

    @Test
    fun `clean call is not blocked`() {
        val model = mapper.toUiModel(ClassifiedCall(aCallRecord(), SpamVerdict.Clean))

        assertFalse(model.isBlocked)
    }

    @Test
    fun `empty list maps to empty state`() {
        assertEquals(CallLogUiState.Empty, mapper.toUiState(emptyList()))
    }
}
