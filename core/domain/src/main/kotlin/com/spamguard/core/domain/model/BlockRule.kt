package com.spamguard.core.domain.model

import java.time.Instant

/**
 * Quy tắc chặn do người dùng tạo.
 *
 * @property pattern số hoặc đầu số đã được chuẩn hóa (chỉ gồm chữ số, bắt đầu bằng 0 với số Việt Nam).
 */
data class BlockRule(
    val id: Long,
    val pattern: String,
    val matchType: MatchType,
    val createdAt: Instant,
)

enum class MatchType {
    /** Chặn đúng một số. */
    EXACT,

    /** Chặn mọi số bắt đầu bằng [BlockRule.pattern]. */
    PREFIX,
}
