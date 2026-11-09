import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.spamguard.feature.calllog"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        // Robolectric cần resource thật (chuỗi, drawable) để dựng màn hình Compose trong test.
        unitTests.isIncludeAndroidResources = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:ui"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    // Điều hướng sang màn chi tiết bằng deep link; ConfirmUnblockDialogFragment dùng MaterialAlertDialogBuilder.
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.material)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(project(":core:testing"))
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.androidx.fragment.testing)
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
    // Activity rỗng cho test: chỉ cần ở test JVM (Robolectric), không gộp vào APK debug.
    testImplementation(libs.androidx.compose.ui.test.manifest)
    testImplementation(libs.androidx.fragment.testing.manifest)
}

// Giống :feature:blocklist: Gradle tải jar android-all, Robolectric chạy offline và đọc jar từ
// thư mục build. Thiếu jar nào, test báo "Path is not a file: .../android-all-instrumented-<version>.jar".
val robolectricAndroidAllJars = listOf(
    libs.robolectric.android.all.sdk34,
    libs.robolectric.android.all.sdk37,
).map { jar ->
    configurations.detachedConfiguration(dependencies.create(jar.get())).apply { isTransitive = false }
}

val prepareRobolectricJars = tasks.register<Sync>("prepareRobolectricJars") {
    from(robolectricAndroidAllJars)
    into(layout.buildDirectory.dir("robolectric-jars"))
}

tasks.withType<Test>().configureEach {
    dependsOn(prepareRobolectricJars)
    val jarsDir = prepareRobolectricJars.map { it.destinationDir.absolutePath }
    systemProperty("robolectric.offline", "true")
    jvmArgumentProviders.add(CommandLineArgumentProvider { listOf("-Drobolectric.dependency.dir=${jarsDir.get()}") })
}
