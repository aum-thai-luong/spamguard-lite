package com.spamguard.core.testing

import com.spamguard.core.domain.model.BlockRule
import com.spamguard.core.domain.model.CallRecord
import com.spamguard.core.domain.model.CallType
import com.spamguard.core.domain.model.MatchType
import java.time.Duration
import java.time.Instant

/** Builder tạo dữ liệu mẫu cho test; chỉ ghi đè trường test cần quan tâm. */
fun aCallRecord(
    id: Long = 1L,
    number: String = "0987654321",
    contactName: String? = null,
    type: CallType = CallType.INCOMING,
    timestamp: Instant = Instant.parse("2026-01-15T08:30:00Z"),
    duration: Duration = Duration.ofSeconds(42),
) = CallRecord(id, number, contactName, type, timestamp, duration)

fun aBlockRule(
    id: Long = 1L,
    pattern: String = "0900",
    matchType: MatchType = MatchType.PREFIX,
    createdAt: Instant = Instant.parse("2026-01-01T00:00:00Z"),
) = BlockRule(id, pattern, matchType, createdAt)
