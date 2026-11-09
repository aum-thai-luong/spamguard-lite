import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.spamguard.feature.blocklist"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
        // Dialog thêm quy tắc vẫn dùng layout XML.
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        // Robolectric cần resource thật để dựng dialog Material.
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
    implementation(libs.material)
    implementation(libs.androidx.lifecycle.runtime.compose)

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
    // Activity rỗng cho test: chỉ cần ở test JVM (Robolectric). Khai báo `debugImplementation`
    // sẽ gộp Activity exported vào APK debug của app.
    testImplementation(libs.androidx.compose.ui.test.manifest)
    testImplementation(libs.androidx.fragment.testing.manifest)
}

// Robolectric mặc định tự tải jar android-all từ Maven Central lúc chạy test, bỏ qua
// cache, mirror và proxy của Gradle. Ở đây Gradle tải jar như mọi dependency khác, còn
// Robolectric chạy offline và đọc jar từ thư mục build. Thiếu jar nào, test báo
// "Path is not a file: .../android-all-instrumented-<version>.jar"; thêm version đó vào catalog.
// Mỗi jar một configuration riêng: cùng group:name nên gộp chung thì Gradle chỉ giữ version cao nhất.
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
