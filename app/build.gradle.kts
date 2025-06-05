// File: app/build.gradle.kts (Module :app)

// Import untuk membaca local.properties
import java.util.Properties
import java.io.FileInputStream
import java.io.File // Import File

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose) // Plugin untuk Kotlin Compose Compiler
    kotlin("plugin.serialization") version libs.versions.kotlin.get() // Menggunakan versi Kotlin dari libs
}

// Fungsi untuk membaca properti dari local.properties
fun getLocalProperty(propertyName: String, projectRootDir: File): String {
    val properties = Properties()
    val localPropertiesFile = File(projectRootDir, "local.properties")
    if (localPropertiesFile.exists() && localPropertiesFile.isFile) {
        FileInputStream(localPropertiesFile).use { properties.load(it) }
        val propertyValue = properties.getProperty(propertyName)
        if (propertyValue.isNullOrBlank()) {
            println("Warning: Property '$propertyName' not found or is empty in local.properties. Defaulting to empty string.")
            return ""
        }
        return propertyValue
    }
    println("Warning: local.properties file not found in root project directory. Defaulting to empty string for '$propertyName'.")
    return "" // Kembalikan string kosong jika file tidak ditemukan atau properti tidak ada
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

        // Mengambil nilai dari local.properties dan menyediakannya sebagai BuildConfig field
        // Pastikan rootProject.rootDir digunakan untuk path yang benar ke local.properties
        buildConfigField("String", "AZURE_OPENAI_BASE_URL", "\"${getLocalProperty("AZURE_OPENAI_BASE_URL", rootProject.rootDir)}\"")
        buildConfigField("String", "AZURE_OPENAI_API_KEY", "\"${getLocalProperty("AZURE_OPENAI_API_KEY", rootProject.rootDir)}\"")
        buildConfigField("String", "AZURE_OPENAI_DEPLOYMENT_NAME", "\"${getLocalProperty("AZURE_OPENAI_DEPLOYMENT_NAME", rootProject.rootDir)}\"")
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
        buildConfig = true // DIAKTIFKAN untuk mengakses BuildConfig field
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
    implementation(libs.androidx.navigation.compose) //

    // Dependensi Inti AndroidX dan Kotlin
    implementation(libs.androidx.core.ktx) //
    implementation(libs.androidx.lifecycle.runtime.ktx) //
    implementation(libs.androidx.activity.compose) //

    // Jetpack Compose - Bill of Materials (BOM)
    implementation(platform(libs.androidx.compose.bom)) //

    // Dependensi Jetpack Compose UI
    implementation(libs.androidx.ui) //
    implementation(libs.androidx.ui.graphics) //
    implementation(libs.androidx.ui.tooling.preview) //
    implementation(libs.androidx.material3) //

    // Dependensi untuk Material Icons
    implementation(libs.androidx.material.icons.core) //
    implementation(libs.androidx.material.icons.extended) //

    // Ktor Client (jika Anda masih menggunakannya untuk hal lain, jika tidak dan hanya Azure, Retrofit sudah cukup)
    implementation("io.ktor:ktor-client-core:2.3.12") //
    implementation("io.ktor:ktor-client-cio:2.3.12") //
    implementation("io.ktor:ktor-client-content-negotiation:2.3.12") //
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.12") //
    implementation("io.ktor:ktor-client-logging:2.3.12") //

    // Kotlinx Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3") //

    // CameraX
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // Networking: Retrofit, Gson, OkHttp Logging (untuk Azure OpenAI)
    // Jika Anda sudah mendefinisikan ini di libs.versions.toml, gunakan alias libs.
    // Jika belum, Anda bisa menggunakan versi eksplisit seperti di bawah ini atau menambahkannya ke TOML.
    implementation("com.squareup.retrofit2:retrofit:2.9.0") // atau versi terbaru yang stabil
    implementation("com.squareup.retrofit2:converter-gson:2.9.0") // atau versi terbaru yang stabil
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0") // atau versi terbaru yang stabil (OkHttp BOM mungkin lebih baik)

    // ViewModel
    implementation(libs.androidx.lifecycle.runtime.ktx) // Anda sudah punya ini
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2") // atau versi dari libs jika ada, atau versi stabil terbaru
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2") // atau versi dari libs jika ada, atau versi stabil terbaru


    // Dependensi Testing
    testImplementation(libs.junit) //
    androidTestImplementation(libs.androidx.junit) //
    androidTestImplementation(libs.androidx.espresso.core) //
    androidTestImplementation(platform(libs.androidx.compose.bom)) //
    androidTestImplementation(libs.androidx.ui.test.junit4) //

    // Dependensi Debug
    debugImplementation(libs.androidx.ui.tooling) //
    debugImplementation(libs.androidx.ui.test.manifest) //
}