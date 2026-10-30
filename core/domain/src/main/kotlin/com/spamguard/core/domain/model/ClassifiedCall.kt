package com.spamguard.core.domain.model

/** Kết quả phân loại một cuộc gọi theo danh sách chặn. */
sealed interface SpamVerdict {
    data object Clean : SpamVerdict

    data class Blocked(val rule: BlockRule) : SpamVerdict
}

data class ClassifiedCall(
    val call: CallRecord,
    val verdict: SpamVerdict,
)
