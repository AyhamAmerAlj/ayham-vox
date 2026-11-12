plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val voxBuild = (project.findProperty("voxBuild") as String?)?.toIntOrNull() ?: 1

android {
    namespace = "com.ayham.vox.tv"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ayham.vox.tv"
        minSdk = 24
        targetSdk = 35
        versionCode = voxBuild
        versionName = "0.1.$voxBuild"
    }

    signingConfigs {
        create("vox") {
            storeFile = rootProject.file("signing/ayham-vox.jks")
            storePassword = "ayhamvox"
            keyAlias = "ayhamvox"
            keyPassword = "ayhamvox"
        }
    }
    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("vox")
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
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(project(":shared-core"))

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
}
