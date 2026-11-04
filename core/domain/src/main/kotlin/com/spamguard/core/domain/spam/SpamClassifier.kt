package com.spamguard.core.domain.spam

import com.spamguard.core.domain.model.BlockRule
import com.spamguard.core.domain.model.CallRecord
import com.spamguard.core.domain.model.ClassifiedCall
import com.spamguard.core.domain.model.SpamVerdict
import com.spamguard.core.domain.phone.PhoneNumberNormalizer
import javax.inject.Inject

/** Gắn nhãn cho từng cuộc gọi dựa trên danh sách quy tắc chặn. */
class SpamClassifier @Inject constructor(
    private val normalizer: PhoneNumberNormalizer,
) {

    fun classify(calls: List<CallRecord>, rules: List<BlockRule>): List<ClassifiedCall> {
        val rulesByPriority = rules.sortedBy { it.matchType.priority }
        return calls.map { call -> ClassifiedCall(call, verdictFor(call.number, rulesByPriority)) }
    }

    /** Phân loại một số (chưa chuẩn hóa), ví dụ số của cuộc gọi đang đổ chuông. */
    fun classify(rawNumber: String, rules: List<BlockRule>): SpamVerdict =
        verdictFor(rawNumber, rules.sortedBy { it.matchType.priority })

    private fun verdictFor(rawNumber: String, rulesByPriority: List<BlockRule>): SpamVerdict {
        val number = normalizer.normalize(rawNumber)
        val matchedRule = rulesByPriority.firstOrNull { rule ->
            rule.matchType.matcher().matches(number, rule.pattern)
        }
        return matchedRule?.let(SpamVerdict::Blocked) ?: SpamVerdict.Clean
    }
}
