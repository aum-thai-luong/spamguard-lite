package com.spamguard.core.domain.usecase

import com.spamguard.core.domain.model.MatchType
import com.spamguard.core.domain.phone.PhoneNumberNormalizer
import com.spamguard.core.domain.repository.BlockRuleRepository
import javax.inject.Inject

sealed interface AddBlockRuleResult {
    data class Added(val ruleId: Long) : AddBlockRuleResult
    data object Duplicate : AddBlockRuleResult
    data object InvalidPattern : AddBlockRuleResult
}

/** Chuẩn hóa, kiểm tra hợp lệ và lưu một quy tắc chặn mới. */
class AddBlockRuleUseCase @Inject constructor(
    private val repository: BlockRuleRepository,
    private val normalizer: PhoneNumberNormalizer,
) {

    suspend operator fun invoke(rawPattern: String, matchType: MatchType): AddBlockRuleResult {
        val pattern = normalizer.normalize(rawPattern)
        return when {
            !BlockRulePolicy.isValid(pattern, matchType) -> AddBlockRuleResult.InvalidPattern
            repository.exists(pattern, matchType) -> AddBlockRuleResult.Duplicate
            else -> AddBlockRuleResult.Added(repository.addRule(pattern, matchType))
        }
    }
}

/** Ràng buộc độ dài của mẫu đã chuẩn hóa. Số E.164 dài tối đa 15 chữ số. */
internal object BlockRulePolicy {
    private val EXACT_LENGTH = 8..15
    private val PREFIX_LENGTH = 2..10

    fun isValid(pattern: String, matchType: MatchType): Boolean {
        val allowedLength = when (matchType) {
            MatchType.EXACT -> EXACT_LENGTH
            MatchType.PREFIX -> PREFIX_LENGTH
        }
        return pattern.length in allowedLength && pattern.all(Char::isDigit)
    }
}
