package com.spamguard.feature.calllog

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.content.ContextCompat

internal const val CALL_LOG_PERMISSION = Manifest.permission.READ_CALL_LOG

internal fun Context.hasCallLogPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, CALL_LOG_PERMISSION) == PackageManager.PERMISSION_GRANTED

/** Mở trang thông tin app trong Cài đặt, nơi người dùng bật lại quyền đã bị từ chối hẳn. */
internal fun Context.openAppSettings() {
    val uri = Uri.fromParts("package", packageName, null)
    startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri))
}
