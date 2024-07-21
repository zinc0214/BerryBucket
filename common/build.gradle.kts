plugins {
    kotlin("plugin.serialization")
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.zinc.waver.common"

    defaultConfig {
        minSdk = Versions.minSdk
    }

    compileSdk = Versions.compileSdk
    buildToolsVersion = Versions.buildTools

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    // serialization
    implementation(libs.kotlinx.serialization)

    // Gson
    implementation(libs.google.gson)
    implementation(libs.retrofit.converter.gson)
}