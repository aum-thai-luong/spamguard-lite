package com.spamguard.core.domain.model

import java.time.Duration
import java.time.Instant

/**
 * Một cuộc gọi trong nhật ký cuộc gọi của thiết bị.
 *
 * @property number số điện thoại đúng như hệ thống lưu (chưa chuẩn hóa).
 * @property contactName tên trong danh bạ, `null` nếu số không có trong danh bạ.
 */
data class CallRecord(
    val id: Long,
    val number: String,
    val contactName: String?,
    val type: CallType,
    val timestamp: Instant,
    val duration: Duration,
)

enum class CallType {
    INCOMING,
    OUTGOING,
    MISSED,
    REJECTED,
    BLOCKED,
    UNKNOWN,
}
