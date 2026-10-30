package com.spamguard.core.domain.usecase

import com.spamguard.core.domain.repository.BlockRuleRepository
import javax.inject.Inject

class RemoveBlockRuleUseCase @Inject constructor(
    private val repository: BlockRuleRepository,
) {
    suspend operator fun invoke(ruleId: Long) = repository.removeRule(ruleId)
}
