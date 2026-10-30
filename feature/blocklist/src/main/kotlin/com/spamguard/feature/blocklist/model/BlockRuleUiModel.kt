package com.spamguard.feature.blocklist.model

import androidx.annotation.StringRes

data class BlockRuleUiModel(
    val id: Long,
    val pattern: String,
    @StringRes val matchTypeLabelRes: Int,
    val createdText: String,
)
