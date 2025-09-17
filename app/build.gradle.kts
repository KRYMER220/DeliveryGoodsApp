plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    alias(libs.plugins.jetbrains.kotlin.serialization)
    id("ru.ok.tracer").version("1.1.0")
}

android {
    namespace = "ru.krymer.delivery"
    compileSdk = 36

    defaultConfig {
        applicationId = "ru.krymer.delivery"
        minSdk = 26
        targetSdk = 36
        versionCode = 35
        versionName = "1.3.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    tracer {
        create("defaultConfig") {
            pluginToken = "lvbCFIvUR9aQhO0qz8YmG0SYeX5LF1M5kE9V70vEvKY2"
            appToken = "fjPNzzrVeSgqHqHyOeWLWjUQE2AiuJcSNvVa1IHtUaw"
            uploadMapping = true
            uploadNativeSymbols = false
        }
    }


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }


    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(17)
        }
    }
}

dependencies {
    // --- Networking ---
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    // --- Kotlin / Serialization / Coroutines ---
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    // --- AndroidX core / lifecycle / appcompat / material ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.process)
    // --- Compose ---
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.animation)
    implementation(libs.androidx.foundation)
    debugImplementation(libs.androidx.ui.tooling)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    // --- Hilt / DI ---
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.common)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    // --- Room (DB) ---
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    // --- WorkManager ---
    implementation(libs.androidx.work.runtime.ktx)
    // --- DataStore ---
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.datastore.core)
    implementation(libs.androidx.datastore.preferences)
    // --- Navigation3 (альфа) ---
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    // --- UI / 3rd-party helpers ---
    implementation(libs.compose.charts)
    implementation(libs.reorderable)
    implementation(libs.sonner)
    // --- Security / Misc ---
    implementation(libs.tink)
    // --- Tracer (ok.tracer) ---
    implementation(platform("ru.ok.tracer:tracer-platform:1.1.0"))
    implementation("ru.ok.tracer:tracer-crash-report")
    implementation("ru.ok.tracer:tracer-crash-report-native")
    implementation("ru.ok.tracer:tracer-heap-dumps")
    implementation("ru.ok.tracer:tracer-disk-usage")
    implementation("ru.ok.tracer:tracer-profiler-sampling")
    implementation("ru.ok.tracer:tracer-profiler-systrace")
}