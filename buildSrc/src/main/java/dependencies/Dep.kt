@file:Suppress("ClassName")

object Versions {
    const val compileSdk = 30
    const val buildTools = "30.0.3"

    const val minSdk = 23
    const val targetSdk = 30
    const val versionCode = 1
    const val versionName = "1.0.0"
}

object Dep {
    object GradlePlugin {
        const val androidStudioGradlePluginVersion = "7.1.0-alpha12"
        const val android = "com.android.tools.build:gradle:$androidStudioGradlePluginVersion"
        const val kotlin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Kotlin.version}"
        const val kotlinSerialization =
            "org.jetbrains.kotlin:kotlin-serialization:${Kotlin.version}"
        const val hilt = "com.google.dagger:hilt-android-gradle-plugin:${Dagger.version}"
    }

    object AndroidX {
        object activity {
            const val activityVersion = "1.3.1"
            const val activity = "androidx.activity:activity:$activityVersion"
            const val ktx = "androidx.activity:activity-ktx:$activityVersion"
        }

        const val appcompat = "androidx.appcompat:appcompat:1.3.1"
        const val coreKtx = "androidx.core:core-ktx:1.6.0"

        object fragment {
            private const val fragmentVersion = "1.3.5"
            const val fragment = "androidx.fragment:fragment:$fragmentVersion"
            const val ktx = "androidx.fragment:fragment-ktx:$fragmentVersion"
        }

        object Lifecycle {
            private const val lifecycleVersion = "2.3.1"
            const val viewModel = "androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion"
            const val viewModelCompose =
                "androidx.lifecycle:lifecycle-viewmodel-compose:1.0.0-alpha07"
            const val livedata = "androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion"
            const val runTime = "androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion"
        }

        object UI {
            const val material = "com.google.android.material:material:1.4.0"
        }

        object Compose {
            const val version = "1.0.1"

            const val runtime = "androidx.compose.runtime:runtime:$version"
            const val ui = "androidx.compose.ui:ui:${version}"
            const val material = "androidx.compose.material:material:${version}"
            const val materialAdapter =
                "com.google.android.material:compose-theme-adapter:${version}"
            const val tooling = "androidx.compose.ui:ui-tooling:${version}"
            const val livedata = "androidx.compose.runtime:runtime-livedata:$version"
            const val animation = "androidx.compose.animation:animation:$version"

            const val activity =
                "androidx.activity:activity-compose:${AndroidX.activity.activityVersion}"
            const val constraintLayout =
                "androidx.constraintlayout:constraintlayout-compose:1.0.0-beta02"
            const val viewModel = "androidx.lifecycle:lifecycle-viewmodel-compose:1.0.0-alpha07"
        }

        const val viewBinding =
            "androidx.databinding:viewbinding:${GradlePlugin.androidStudioGradlePluginVersion}"

        object Hilt {
            private const val version = "1.0.0"

            const val compiler = "androidx.hilt:hilt-compiler:${version}"
            const val work = "androidx.hilt:hilt-work:${version}"
        }

        object RecyclerView {
            const val core = "androidx.recyclerview:recyclerview:1.2.1"
            const val selection = "androidx.recyclerview:recyclerview-selection:1.1.0"
        }
    }

    object Dagger {
        const val version = "2.38.1"
        const val hiltAndroid = "com.google.dagger:hilt-android:$version"
        const val hiltCompiler = "com.google.dagger:hilt-compiler:$version"
        const val hiltAndroidCompiler = "com.google.dagger:hilt-android-compiler:version" // kapt
    }

    object Kotlin {
        const val version = "1.5.21"
        const val stdlibJvm = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$version"

        object coroutines {
            private const val coroutinesVersion = "1.5.1"
            const val core = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion"
            const val android =
                "org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion"
            const val test = "org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion"
        }

        const val serialization = "org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2"
    }

    object OkHttp {
        private const val version = "4.9.1"
        const val core = "com.squareup.okhttp3:okhttp:$version"
        const val loggingInterceptor = "com.squareup.okhttp3:logging-interceptor:$version"
    }

    object Retrofit {
        private const val version = "2.9.0"
        const val core = "com.squareup.retrofit2:retrofit:$version"
        const val serialization =
            "com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.8.0"
        const val converter = "com.squareup.retrofit2:converter-gson:$version"
    }

    const val glide = "com.github.skydoves:landscapist-glide:1.1.7"
    const val cardView = "androidx.cardview:cardview:1.0.0"
    const val timber = "com.jakewharton.timber:timber:4.7.1"

    object Test {
        const val junit = "junit:junit:4.13.2"
        const val assertJ = "org.assertj:assertj-core:3.20.2"
        const val mockito = "org.mockito:mockito-core:3.11.2"
        const val androidJunit = "androidx.test.ext:junit:1.1.3"
        const val espressoCore = "androidx.test.espresso:espresso-core:3.4.0"
        const val compose = "androidx.compose.ui:ui-test-junit4:${AndroidX.Compose.version}"
    }
}
