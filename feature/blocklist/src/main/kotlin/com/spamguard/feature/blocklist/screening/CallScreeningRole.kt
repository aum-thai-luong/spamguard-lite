package com.spamguard.feature.blocklist.screening

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.spamguard.feature.blocklist.ScreeningStatus

/** Đọc và xin vai trò sàng lọc cuộc gọi; vai trò này chỉ có từ Android 10. */
internal object CallScreeningRole {

    fun status(context: Context): ScreeningStatus {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return ScreeningStatus.Unsupported
        val roleManager = context.getSystemService(RoleManager::class.java)
            ?: return ScreeningStatus.Unsupported
        return when {
            !roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING) -> ScreeningStatus.Unsupported
            roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) -> ScreeningStatus.Enabled
            else -> ScreeningStatus.Disabled
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun requestIntent(context: Context): Intent =
        context.getSystemService(RoleManager::class.java)
            .createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
}
