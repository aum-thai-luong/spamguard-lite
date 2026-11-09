package com.spamguard.feature.calllog.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// Key phải @Serializable: back stack được lưu lại khi xoay màn hình hoặc hệ thống dừng process.

/** Tab Cuộc gọi (gốc của tab). */
@Serializable
data object CallLogKey : NavKey

/** Chi tiết một cuộc gọi; [callId] là id trong nhật ký cuộc gọi của hệ thống. */
@Serializable
data class CallDetailKey(val callId: Long) : NavKey
