import java.util.Properties
import java.io.FileInputStream

plugins {
    id("kotlin-kapt")
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

// Local signing fallback: reads from keystore.properties (kept out of git)
// when it exists on disk. In CI, this file won't exist, so it's ignored
// and the environment variables set in the workflow are used instead.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
val hasLocalKeystoreProperties = keystorePropertiesFile.exists()
if (hasLocalKeystoreProperties) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    namespace = "io.github.norbertweb.bluebird"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.trebronwayne.bluebird"
        minSdk = 26
        targetSdk = 36
        versionCode = 14
        versionName = "2.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            if (hasLocalKeystoreProperties) {
                // Local build: read from keystore.properties
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            } else {
                // CI build: read from environment variables
                storeFile = System.getenv("KEYSTORE_PATH")?.let { file(it) }
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
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
configurations.all {
    resolutionStrategy {
        force("androidx.compose.material:material:1.7.8")
        force("androidx.compose.material:material-ripple:1.7.8")
    }
}


dependencies {
implementation("io.github.niyajali:fluentui-system-icons:1.0.1")

    // Already in Compose projects — confirm these are present:
    implementation("androidx.webkit:webkit:1.8.0")
// WebSettingsCompat dark mode
    implementation("androidx.activity:activity-compose:1.8.2")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
 // All material icons have been swap with fluent system UI iconsimplementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.accompanist.permissions)
    implementation(libs.coil.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.gson)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.remote.creation.core)
    implementation(libs.androidx.room.ktx)
   // implementation(libs.androidx.compose.ui)
    debugImplementation(libs.androidx.ui.tooling)
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.0")
    implementation("com.google.accompanist:accompanist-drawablepainter:0.32.0")

    // Media3 — required by PlaybackService.kt and MediaPlayerScreen.kt
    implementation("androidx.media3:media3-exoplayer:1.4.1")
    implementation("androidx.media3:media3-session:1.4.1")
    implementation("androidx.media3:media3-ui:1.4.1")
    implementation("androidx.media3:media3-common:1.4.1")

    // Dominant-color extraction from album art for the Now Playing screen
    implementation("androidx.palette:palette-ktx:1.0.0")
    // Real backdrop blur behind glass panels (Taskbar, Start Menu, Action
    // Center, etc.) — see HazeBlurSetup.kt for the shared wrapper this
    // powers. Compose has no first-party blur-behind-content API, Haze is
    // the standard solution (works down to minSdk, no API 31+ requirement).
    implementation("dev.chrisbanes.haze:haze:1.6.10")
    implementation("dev.chrisbanes.haze:haze-materials:1.6.10")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-crashlytics")
implementation("com.google.firebase:firebase-firestore-ktx")
implementation("com.google.firebase:firebase-config-ktx")
}
