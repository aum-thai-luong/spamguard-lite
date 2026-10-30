package com.spamguard.core.data.calllog

/** Một dòng đọc thô từ CallLog content provider, chưa đổi sang model domain. */
internal data class CallLogEntry(
    val id: Long,
    val number: String,
    val cachedName: String?,
    /** Giá trị `CallLog.Calls.TYPE`. */
    val type: Int,
    val dateEpochMillis: Long,
    val durationSeconds: Long,
)

internal interface CallLogDataSource {
    /** @return `null` nếu content provider không trả về cursor. */
    fun queryRecentCalls(limit: Int): List<CallLogEntry>?
}
