plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.travelscolombia"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.travelscolombia"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Jetpack Compose básico
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.foundation)

    implementation(libs.coil.compose)

    // Google Maps
    implementation(libs.play.services.maps)
    implementation(libs.maps.compose)

    // Navigation Compose
    implementation(libs.androidx.navigation.compose)

    // Firebase Auth
    implementation(libs.firebase.auth.ktx)
// Para usar Google Maps Compose
    implementation(libs.maps.compose)

// Para permisos (Accompanist)
    implementation(libs.accompanist.permissions)

// Para obtener ubicación
    implementation(libs.play.services.location)
// Google Maps Compose
    implementation(libs.maps.compose)

// Google Play Services Location
    implementation(libs.play.services.location.v2101)

// Retrofit para llamadas HTTP
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

// Gson para deserialización JSON
    implementation(libs.gson)

//Para notificaciones
    implementation("com.google.firebase:firebase-messaging:24.0.0")

// Google Maps Android Utils para decodificar polilíneas
    implementation(libs.android.maps.utils)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.google.firebase.storage.ktx)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.biometric.ktx)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.security.crypto)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
