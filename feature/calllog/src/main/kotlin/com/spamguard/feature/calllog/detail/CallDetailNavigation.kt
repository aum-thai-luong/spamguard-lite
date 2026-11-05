package com.spamguard.feature.calllog.detail

import androidx.core.net.toUri
import androidx.navigation.NavDeepLinkRequest

/** Tên argument của destination; trùng `{callId}` trong deep link và `<argument>` của nav_graph. */
internal const val CALL_ID_ARG = "callId"

/**
 * Phải trùng `<deepLink app:uri>` của `callDetailFragment` trong `app/src/main/res/navigation/nav_graph.xml`.
 * Manifest không khai báo `<nav-graph>`, nên chỉ app tự điều hướng được tới URI này.
 */
private const val CALL_DETAIL_URI_PREFIX = "android-app://com.spamguard.lite/call-detail/"

/** Feature module không thấy `R` của `:app`, nên điều hướng sang chi tiết bằng deep link nội bộ. */
internal fun callDetailRequest(callId: Long): NavDeepLinkRequest =
    NavDeepLinkRequest.Builder.fromUri("$CALL_DETAIL_URI_PREFIX$callId".toUri()).build()
