plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "ro.student.smartparking"
    compileSdk = 35

    defaultConfig {
        applicationId = "ro.student.smartparking"
        minSdk = 23
        targetSdk = 34
        versionCode = 5
        versionName = "1.1"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

