package com.spamguard.core.domain.repository

import com.spamguard.core.domain.model.CallRecord

interface CallLogRepository {
    /** Trả về tối đa [limit] cuộc gọi gần nhất, mới nhất trước. */
    suspend fun getRecentCalls(limit: Int): List<CallRecord>
}
