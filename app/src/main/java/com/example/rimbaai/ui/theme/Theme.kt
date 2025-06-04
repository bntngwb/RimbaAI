package com.example.rimbaai.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.Color // <-- PASTIKAN IMPORT INI ADA

// Palet warna terang kustom untuk RimbaAI
// Pastikan variabel warna ini (RimbaGreenLight, dll.) sudah didefinisikan di Color.kt
// dan Color.kt juga sudah mengimpor androidx.compose.ui.graphics.Color
private val LightColorSchemeRimba = lightColorScheme(
    primary = RimbaGreenLight,
    secondary = RimbaOrangeAccentLight,
    tertiary = Pink80, // Anda bisa ganti ini atau definisikan warna kustom
    background = RimbaBackgroundLight,
    surface = RimbaBackgroundLight,
    onPrimary = Color.White, // Menggunakan Color.White
    onSecondary = Color.White, // Menggunakan Color.White
    onTertiary = Color.White, // Menggunakan Color.White
    onBackground = RimbaTextPrimaryLight,
    onSurface = RimbaTextPrimaryLight
    // Anda bisa mendefinisikan warna lain sesuai kebutuhan
)

// Palet warna gelap kustom untuk RimbaAI (buat jika Anda mendukung tema gelap)
private val DarkColorSchemeRimba = darkColorScheme(
    primary = Purple80, // Ganti dengan warna gelap Anda, misal RimbaGreenDark
    secondary = PurpleGrey80, // Ganti dengan warna gelap Anda
    tertiary = Pink40, // Ganti dengan warna gelap Anda
    background = Color(0xFF1C1B1F), // Contoh warna latar gelap
    surface = Color(0xFF1C1B1F), // Contoh
    onPrimary = Color.Black, // Menggunakan Color.Black
    onSecondary = Color.Black, // Menggunakan Color.Black
    onTertiary = Color.Black, // Menggunakan Color.Black
    onBackground = Color(0xFFE6E1E5), // Contoh
    onSurface = Color(0xFFE6E1E5) // Contoh
    // Anda bisa mendefinisikan warna lain sesuai kebutuhan
)

@Composable
fun RimbaAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Dynamic color dinonaktifkan untuk memakai tema kustom
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorSchemeRimba
        else -> LightColorSchemeRimba
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Anda bisa mengatur warna status bar sesuai warna primary atau background tema
            window.statusBarColor = colorScheme.background.toArgb() // Menggunakan background agar lebih seamless
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Pastikan Typography sudah terdefinisi (di Type.kt)
        content = content
    )
}