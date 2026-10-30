package com.spamguard.core.domain.spam

import com.spamguard.core.domain.model.MatchType

/**
 * Chiến lược so khớp một số điện thoại với mẫu của quy tắc chặn.
 * Thêm kiểu so khớp mới = thêm một [MatchType] và một matcher, không sửa [SpamClassifier].
 */
fun interface BlockRuleMatcher {
    /**
     * @param normalizedNumber số đã chuẩn hóa bằng [com.spamguard.core.domain.phone.PhoneNumberNormalizer].
     * @param pattern mẫu của quy tắc, cũng đã chuẩn hóa.
     */
    fun matches(normalizedNumber: String, pattern: String): Boolean
}

internal object ExactMatcher : BlockRuleMatcher {
    override fun matches(normalizedNumber: String, pattern: String): Boolean =
        normalizedNumber == pattern
}

internal object PrefixMatcher : BlockRuleMatcher {
    override fun matches(normalizedNumber: String, pattern: String): Boolean =
        normalizedNumber.contains(pattern)
}

internal fun MatchType.matcher(): BlockRuleMatcher = when (this) {
    MatchType.EXACT -> ExactMatcher
    MatchType.PREFIX -> PrefixMatcher
}

/** Quy tắc cụ thể hơn được xét trước: chặn đúng số ưu tiên hơn chặn theo đầu số. */
internal val MatchType.priority: Int
    get() = when (this) {
        MatchType.EXACT -> 0
        MatchType.PREFIX -> 1
    }
