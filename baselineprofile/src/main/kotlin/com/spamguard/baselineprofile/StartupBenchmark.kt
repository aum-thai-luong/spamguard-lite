package com.spamguard.baselineprofile

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * So sánh thời gian khởi động lạnh khi không biên dịch trước ([CompilationMode.None]) và khi dùng Baseline Profile.
 *
 *     ./gradlew :baselineprofile:connectedBenchmarkReleaseAndroidTest
 *
 * Số đo trên emulator dao động nhiều; chỉ dùng để so tương đối, đo thật trên thiết bị.
 * Chưa có benchmark cuộn: trên emulator API 36, FrameTimingMetric không đọc được frame nào
 * dù cử chỉ cuộn vẫn chạy; thêm lại khi đo được trên thiết bị thật.
 */
@RunWith(AndroidJUnit4::class)
class StartupBenchmark {

    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun startupNoCompilation() = startup(CompilationMode.None())

    @Test
    fun startupBaselineProfile() = startup(CompilationMode.Partial(BaselineProfileMode.Require))

    private fun startup(compilationMode: CompilationMode) = rule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(StartupTimingMetric()),
        compilationMode = compilationMode,
        startupMode = StartupMode.COLD,
        iterations = ITERATIONS,
        setupBlock = {
            pressHome()
            grantCallLogPermission()
        },
    ) {
        startActivityAndWait()
    }

    private companion object {
        const val ITERATIONS = 10
    }
}
