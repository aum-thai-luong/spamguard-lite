package com.spamguard.core.domain.error

/** Danh sách quy tắc chặn tạm thời không đọc được (ví dụ: cơ sở dữ liệu lỗi hoặc đầy bộ nhớ). */
class BlockRulesUnavailableException(message: String, cause: Throwable? = null) : Exception(message, cause)
