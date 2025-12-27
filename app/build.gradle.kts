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
        versionCode = 93
        versionName = "1.8.8"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        buildConfigField("String", "BASE_URL", project.properties["PRODUCTION_BASE_URL"] as String? ?: "\"\"")
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
            pluginToken = project.properties["PLUGIN_TOKEN"]?.toString() ?: ""
            appToken = project.properties["APP_TOKEN"]?.toString() ?: ""
            uploadMapping = true
            uploadNativeSymbols = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
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
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.animation)
    implementation(libs.androidx.foundation)
    debugImplementation(libs.androidx.ui.tooling)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    // --- Hilt / DI ---
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.common)
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
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
    // --- Navigation3 (alpha) ---
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
    implementation(platform(libs.tracer.platform))
    implementation(libs.tracer.crash.report)
    implementation(libs.tracer.crash.report.native)
    implementation(libs.tracer.heap.dumps)
    implementation(libs.tracer.disk.usage)
    implementation(libs.tracer.profiler.sampling)
    implementation(libs.tracer.profiler.systrace)

    implementation(platform(libs.bom))
    implementation(libs.appupdate)
}