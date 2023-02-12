plugins {
    id("com.android.library")
    kotlin("android")
    id("kotlin-parcelize")
    id("kotlinx-serialization")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "com.zinc.berrybucket.ui_more"

    defaultConfig {
        minSdk = Versions.minSdk
    }

    compileSdk = Versions.compileSdk
    buildToolsVersion = Versions.buildTools

    composeOptions {
        kotlinCompilerExtensionVersion = Dep.AndroidX.Compose.compilerVersion
    }
    buildFeatures {
        compose = true
    }
    kapt {
        correctErrorTypes = true
    }
}


dependencies {

    implementation(project(":common"))
    implementation(project(":datastore"))
    implementation(project(":feature:ui-common"))

    val composeBom = platform("androidx.compose:compose-bom:2022.10.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // compose
    implementation(Dep.AndroidX.Compose.Bom.ui)
    implementation(Dep.AndroidX.Compose.Bom.material)
    implementation(Dep.AndroidX.Compose.Bom.livedata)
    implementation(Dep.AndroidX.Compose.Bom.runtime)
    implementation(Dep.AndroidX.Compose.tooling)
    implementation(Dep.AndroidX.Compose.constraintLayout)
    implementation(Dep.AndroidX.Compose.accompanist)

    // Hilt
    implementation(Dep.Dagger.Hilt.android)
    implementation(Dep.Dagger.Hilt.navigation)
    kapt(Dep.Dagger.Hilt.compiler)

    // Coil
    implementation(Dep.coil)
}