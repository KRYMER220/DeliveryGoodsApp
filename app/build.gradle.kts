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
        versionCode = 28
        versionName = "1.3.1.5"

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

    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.tink)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.common)
    implementation(libs.material)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.core)
    implementation(libs.androidx.datastore)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.androidx.ui.tooling)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation (libs.compose.charts)


    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation (libs.androidx.room.ktx)
    implementation(libs.kotlin.reflect)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.animation)
    implementation(libs.androidx.foundation)

    implementation(libs.reorderable)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.sonner)

    implementation(platform("ru.ok.tracer:tracer-platform:1.1.0"))
    implementation("ru.ok.tracer:tracer-crash-report")
    implementation("ru.ok.tracer:tracer-crash-report-native")
    implementation("ru.ok.tracer:tracer-heap-dumps")
    implementation("ru.ok.tracer:tracer-disk-usage")
    implementation("ru.ok.tracer:tracer-profiler-sampling")
    implementation("ru.ok.tracer:tracer-profiler-systrace")
}