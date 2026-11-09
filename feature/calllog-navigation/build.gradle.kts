import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Chỉ chứa NavKey của :feature:calllog. Feature khác (và :app) điều hướng tới màn hình của
// :feature:calllog qua module này mà không phụ thuộc vào chính feature đó.
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.spamguard.feature.calllog.navigation"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    api(libs.androidx.navigation3.runtime)
    implementation(libs.kotlinx.serialization.core)
}
