package com.spamguard.core.domain.error

/** Nhật ký cuộc gọi tạm thời không đọc được (ví dụ: content provider không phản hồi). */
class CallLogUnavailableException(message: String) : Exception(message)
