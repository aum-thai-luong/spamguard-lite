package com.spamguard.feature.calllog.detail

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/** Dữ liệu đã sẵn sàng để hiển thị màn hình chi tiết một cuộc gọi. */
data class CallDetailUiModel(
    /** Tên danh bạ hoặc số điện thoại; `null` với số ẩn. */
    val title: String?,
    /** Số điện thoại, chỉ có khi [title] là tên danh bạ. */
    val subtitle: String?,
    /** Số dùng cho gọi lại, sao chép, chặn; `null` với số ẩn. */
    val number: String?,
    @DrawableRes val typeIconRes: Int,
    @StringRes val typeLabelRes: Int,
    val timeText: String,
    /** `null` khi cuộc gọi không kết nối (0 giây). */
    val durationText: String?,
    /** Quy tắc đang chặn số này, `null` nếu không bị chặn. */
    val blockingRule: BlockingRuleUi?,
) {
    val canCall: Boolean get() = number != null
    val canBlock: Boolean get() = number != null && blockingRule == null
}

data class BlockingRuleUi(
    val ruleId: Long,
    val pattern: String,
    val isPrefix: Boolean,
    /** Chuỗi có một tham số là [pattern]. */
    @StringRes val labelRes: Int,
)
