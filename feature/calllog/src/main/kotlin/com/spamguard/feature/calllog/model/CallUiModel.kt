package com.spamguard.feature.calllog.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/** Dữ liệu đã sẵn sàng để hiển thị một dòng trong danh sách cuộc gọi. */
data class CallUiModel(
    val id: Long,
    /** Tên danh bạ hoặc số điện thoại; `null` với số ẩn. */
    val title: String?,
    /** Số điện thoại, chỉ có khi [title] là tên danh bạ. */
    val subtitle: String?,
    /** Số dùng để chặn; `null` với số ẩn. */
    val number: String?,
    val timeText: String,
    @DrawableRes val typeIconRes: Int,
    @StringRes val typeLabelRes: Int,
    /** Mẫu của quy tắc đã chặn cuộc gọi này, `null` nếu không bị chặn. */
    val blockedPattern: String?,
) {
    val isBlocked: Boolean get() = blockedPattern != null

    /** Nhấn giữ để chặn chỉ có với số không ẩn và chưa bị chặn, giống màn chi tiết. */
    val canBlock: Boolean get() = number != null && !isBlocked
}
