package com.spamguard.feature.blocklist.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

// Key phải @Serializable: back stack được lưu lại khi xoay màn hình hoặc hệ thống dừng process.

/** Tab Danh sách chặn (gốc của tab). */
@Serializable
data object BlocklistKey : NavKey
