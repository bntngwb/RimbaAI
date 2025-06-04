// File: build.gradle.kts (Module :app)

// Import untuk membaca local.properties
import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose) // Plugin untuk Kotlin Compose Compiler
    kotlin("plugin.serialization") version libs.versions.kotlin.get() // Menggunakan versi Kotlin dari libs
}

android {
    namespace = "com.example.rimbaai"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.rimbaai"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Pembacaan API Key dari local.properties dihapus karena tidak ada API Key yang akan digunakan dari BuildConfig saat ini
        // val localProperties = Properties()
        // val localPropertiesFile = rootProject.file("local.properties")
        // if (localPropertiesFile.exists()) {
        //     localProperties.load(FileInputStream(localPropertiesFile))
        // } else {
        //     println("Peringatan: File local.properties tidak ditemukan. API Keys akan menggunakan nilai default.")
        // }

        // Penyediaan API Key Gemini sebagai BuildConfig field DIHAPUS
        // val geminiApiKey = localProperties.getProperty("GEMINI_API_KEY", "MASUKKAN_KEY_GEMINI_DI_LOCAL_PROPERTIES")
        // buildConfigField("String", "GEMINI_API_KEY", "\"$geminiApiKey\"")

        // Kredensial Azure OpenAI juga sudah dihapus/dikomentari sebelumnya
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
        // buildConfig = true // Bisa disetel ke false jika tidak ada BuildConfig field yang digunakan
        // Namun, membiarkannya true tidak masalah, hanya saja BuildConfig akan kosong
        buildConfig = false // Kita set ke false karena tidak ada field yang didefinisikan
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/LICENSE-notice.md"
        }
    }
}

dependencies {
    // Coil untuk memuat gambar
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Navigasi Compose
    implementation(libs.androidx.navigation.compose)

    // Dependensi Inti AndroidX dan Kotlin
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Jetpack Compose - Bill of Materials (BOM)
    implementation(platform(libs.androidx.compose.bom))

    // Dependensi Jetpack Compose UI
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Dependensi untuk Material Icons
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.material.icons.extended)

    // Ktor Client
    implementation("io.ktor:ktor-client-core:2.3.12")
    implementation("io.ktor:ktor-client-cio:2.3.12") // Engine untuk Android
    implementation("io.ktor:ktor-client-content-negotiation:2.3.12")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.12")
    implementation("io.ktor:ktor-client-logging:2.3.12")

    // Kotlinx Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Dependensi Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Dependensi Debug
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
