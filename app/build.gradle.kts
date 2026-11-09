import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.androidx.baselineprofile)
}

android {
    namespace = "com.spamguard.lite"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.spamguard.lite"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0.0"
        // Instrumented test chạy với HiltTestApplication để thay module dữ liệu bằng fake.
        testInstrumentationRunner = "com.spamguard.lite.HiltTestRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            // Bỏ resource không còn được tham chiếu sau khi R8 lược code.
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    buildFeatures {
        compose = true
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

baselineProfile {
    // Build release dùng profile đã commit; chỉ sinh lại khi chạy :app:generateBaselineProfile
    // (cần emulator), để assembleRelease không đòi thiết bị.
    automaticGenerationDuringBuild = false
}

dependencies {
    implementation(project(":core:data"))
    implementation(project(":core:ui"))
    implementation(project(":feature:calllog"))
    implementation(project(":feature:calllog-navigation"))
    implementation(project(":feature:blocklist"))
    implementation(project(":feature:blocklist-navigation"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)

    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)

    implementation(libs.androidx.profileinstaller)
    baselineProfile(project(":baselineprofile"))

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    androidTestImplementation(project(":core:testing"))
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
}
