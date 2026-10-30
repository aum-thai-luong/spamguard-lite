package com.spamguard.core.data.calllog

import android.content.ContentResolver
import android.database.Cursor
import android.provider.CallLog
import javax.inject.Inject

internal class ContentResolverCallLogDataSource @Inject constructor(
    private val contentResolver: ContentResolver,
) : CallLogDataSource {

    override fun queryRecentCalls(limit: Int): List<CallLogEntry>? =
        contentResolver
            .query(CallLog.Calls.CONTENT_URI, PROJECTION, null, null, SORT_NEWEST_FIRST)
            ?.use { cursor -> cursor.readEntries(limit) }

    private fun Cursor.readEntries(limit: Int): List<CallLogEntry> {
        val idColumn = getColumnIndexOrThrow(CallLog.Calls._ID)
        val numberColumn = getColumnIndexOrThrow(CallLog.Calls.NUMBER)
        val nameColumn = getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME)
        val typeColumn = getColumnIndexOrThrow(CallLog.Calls.TYPE)
        val dateColumn = getColumnIndexOrThrow(CallLog.Calls.DATE)
        val durationColumn = getColumnIndexOrThrow(CallLog.Calls.DURATION)

        return buildList {
            while (size < limit && moveToNext()) {
                add(
                    CallLogEntry(
                        id = getLong(idColumn),
                        number = getString(numberColumn).orEmpty(),
                        cachedName = getString(nameColumn)?.takeIf { it.isNotBlank() },
                        type = getInt(typeColumn),
                        dateEpochMillis = getLong(dateColumn),
                        durationSeconds = getLong(durationColumn),
                    ),
                )
            }
        }
    }

    private companion object {
        val PROJECTION = arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.NUMBER,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.TYPE,
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
        )
        const val SORT_NEWEST_FIRST = "${CallLog.Calls.DATE} DESC"
    }
}
