package com.spamguard.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Sinh Baseline Profile cho hành trình chính: mở app, cuộn nhật ký cuộc gọi, mở danh sách chặn.
 *
 *     ./gradlew :app:generateBaselineProfile
 *
 * Kết quả ghi vào app/src/release/generated/baselineProfiles/ và cần được commit.
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() = rule.collect(
        packageName = TARGET_PACKAGE,
        includeInStartupProfile = true,
    ) {
        pressHome()
        grantCallLogPermission()
        startActivityAndWait()
        scrollCallLog()
        openBlocklist()
    }
}
