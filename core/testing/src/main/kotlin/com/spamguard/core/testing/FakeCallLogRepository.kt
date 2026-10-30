package com.spamguard.core.testing

import com.spamguard.core.domain.model.CallRecord
import com.spamguard.core.domain.repository.CallLogRepository

class FakeCallLogRepository : CallLogRepository {

    var calls: List<CallRecord> = emptyList()

    /** Nếu khác `null`, mọi lần đọc sẽ ném lỗi này. */
    var failure: Throwable? = null

    override suspend fun getRecentCalls(limit: Int): List<CallRecord> {
        failure?.let { throw it }
        return calls.take(limit)
    }
}
