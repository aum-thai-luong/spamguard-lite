package com.spamguard.core.domain.usecase

import com.spamguard.core.domain.model.BlockRule
import com.spamguard.core.domain.repository.BlockRuleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveBlockRulesUseCase @Inject constructor(
    private val repository: BlockRuleRepository,
) {
    operator fun invoke(): Flow<List<BlockRule>> = repository.observeRules()
}
