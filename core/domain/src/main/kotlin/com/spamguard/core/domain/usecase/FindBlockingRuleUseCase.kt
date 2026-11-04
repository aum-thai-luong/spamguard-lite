package com.spamguard.core.domain.usecase

import com.spamguard.core.domain.model.BlockRule
import com.spamguard.core.domain.model.SpamVerdict
import com.spamguard.core.domain.repository.BlockRuleRepository
import com.spamguard.core.domain.spam.SpamClassifier
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Tìm quy tắc chặn khớp với một số gọi đến, dùng khi sàng lọc cuộc gọi lúc đang đổ chuông.
 *
 * @return quy tắc khớp (ưu tiên quy tắc chính xác), hoặc `null` nếu số không bị chặn.
 */
class FindBlockingRuleUseCase @Inject constructor(
    private val repository: BlockRuleRepository,
    private val classifier: SpamClassifier,
) {

    suspend operator fun invoke(rawNumber: String): BlockRule? {
        val rules = repository.observeRules().first()
        return (classifier.classify(rawNumber, rules) as? SpamVerdict.Blocked)?.rule
    }
}
