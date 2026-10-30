package com.spamguard.core.data.repository

import android.provider.CallLog
import com.spamguard.core.data.calllog.CallLogDataSource
import com.spamguard.core.data.calllog.CallLogEntry
import com.spamguard.core.domain.error.CallLogUnavailableException
import com.spamguard.core.domain.model.CallType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class CallLogRepositoryImplTest {

    private val dataSource = StubCallLogDataSource()
    private val repository = CallLogRepositoryImpl(dataSource, UnconfinedTestDispatcher())

    @Test
    fun `maps provider rows to domain calls`() = runTest {
        dataSource.entries = listOf(
            CallLogEntry(
                id = 7,
                number = "0900123456",
                cachedName = "Chị Lan",
                type = CallLog.Calls.MISSED_TYPE,
                dateEpochMillis = 1_700_000_000_000,
                durationSeconds = 0,
            ),
        )

        val call = repository.getRecentCalls(limit = 10).single()

        assertEquals(7L, call.id)
        assertEquals("Chị Lan", call.contactName)
        assertEquals(CallType.MISSED, call.type)
        assertEquals(Instant.ofEpochMilli(1_700_000_000_000), call.timestamp)
        assertEquals(Duration.ZERO, call.duration)
    }

    @Test
    fun `unknown provider call type maps to UNKNOWN`() = runTest {
        dataSource.entries = listOf(
            CallLogEntry(1, "0987654321", null, type = 99, dateEpochMillis = 0, durationSeconds = 0),
        )

        assertEquals(CallType.UNKNOWN, repository.getRecentCalls(limit = 10).single().type)
    }

    @Test
    fun `missing cursor surfaces as CallLogUnavailableException`() = runTest {
        dataSource.entries = null

        val error = runCatching { repository.getRecentCalls(limit = 10) }.exceptionOrNull()

        assertTrue(error is CallLogUnavailableException)
    }

    private class StubCallLogDataSource : CallLogDataSource {
        var entries: List<CallLogEntry>? = emptyList()

        override fun queryRecentCalls(limit: Int): List<CallLogEntry>? = entries?.take(limit)
    }
}
