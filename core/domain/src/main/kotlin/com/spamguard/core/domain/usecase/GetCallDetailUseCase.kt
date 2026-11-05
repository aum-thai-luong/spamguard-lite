package com.spamguard.core.domain.usecase

import com.spamguard.core.domain.model.ClassifiedCall
import com.spamguard.core.domain.repository.BlockRuleRepository
import com.spamguard.core.domain.repository.CallLogRepository
import com.spamguard.core.domain.spam.SpamClassifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Đọc một cuộc gọi theo id (một lần cho mỗi lần collect), rồi phân loại lại mỗi khi
 * danh sách chặn thay đổi. Phát `null` nếu cuộc gọi không còn trong nhật ký.
 */
class GetCallDetailUseCase @Inject constructor(
    private val callLogRepository: CallLogRepository,
    private val blockRuleRepository: BlockRuleRepository,
    private val classifier: SpamClassifier,
) {

    operator fun invoke(callId: Long): Flow<ClassifiedCall?> = flow {
        val call = callLogRepository.getCall(callId)
        if (call == null) {
            emit(null)
            return@flow
        }
        emitAll(
            blockRuleRepository.observeRules()
                // Room phát lại sau mọi lần ghi vào bảng; chỉ phân loại lại khi quy tắc thật sự đổi.
                .distinctUntilChanged()
                .map { rules -> ClassifiedCall(call, classifier.classify(call.number, rules)) },
        )
    }
}
