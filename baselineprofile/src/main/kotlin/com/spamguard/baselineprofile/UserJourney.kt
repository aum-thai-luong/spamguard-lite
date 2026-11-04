package com.spamguard.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until

internal const val TARGET_PACKAGE = "com.spamguard.lite"

private const val UI_TIMEOUT_MILLIS = 5_000L

/**
 * Cấp sẵn quyền đọc nhật ký cuộc gọi để hành trình không dừng ở hộp thoại xin quyền.
 * Gọi trước khi mở app; dữ liệu mẫu lấy từ scripts/seed-call-log.sh.
 */
internal fun MacrobenchmarkScope.grantCallLogPermission() {
    device.executeShellCommand("pm grant $packageName android.permission.READ_CALL_LOG")
}

/** Chờ danh sách cuộc gọi (Compose testTag `call_log_list`) rồi cuộn xuống và lên. */
internal fun MacrobenchmarkScope.scrollCallLog() {
    val list = device.wait(Until.findObject(By.res("call_log_list")), UI_TIMEOUT_MILLIS) ?: return
    // Chừa mép để cử chỉ không chạm thanh điều hướng của hệ thống.
    list.setGestureMargin(device.displayWidth / 5)
    list.fling(Direction.DOWN)
    device.waitForIdle()
    list.fling(Direction.UP)
    device.waitForIdle()
}

/** Mở tab Danh sách chặn qua thanh điều hướng dưới (id menu `blocklistFragment`). */
internal fun MacrobenchmarkScope.openBlocklist() {
    device.findObject(By.res(packageName, "blocklistFragment"))?.click() ?: return
    device.waitForIdle()
}
