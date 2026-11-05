package com.spamguard.core.domain.repository

import com.spamguard.core.domain.model.CallRecord

interface CallLogRepository {
    /** Trả về tối đa [limit] cuộc gọi gần nhất, mới nhất trước. */
    suspend fun getRecentCalls(limit: Int): List<CallRecord>

    /**
     * Trả về cuộc gọi có [id], hoặc `null` nếu không còn trong nhật ký (ví dụ người dùng đã xóa).
     *
     * @throws com.spamguard.core.domain.error.CallLogUnavailableException khi không đọc được nhật ký.
     */
    suspend fun getCall(id: Long): CallRecord?
}
