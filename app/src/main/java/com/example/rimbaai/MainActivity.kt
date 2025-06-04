package com.example.rimbaai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.rimbaai.ui.theme.RimbaAITheme
// Import NavHostController dan rememberNavController jika Anda ingin mengontrolnya dari sini,
// tapi AppNavHost sudah menanganinya secara internal.

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RimbaAITheme {
                // Panggil AppNavHost di sini
                AppNavHost()
            }
        }
    }
}

// Preview untuk MainActivity sekarang bisa menampilkan AppNavHost jika diperlukan,
// atau Anda bisa mengandalkan preview per layar.
@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun DefaultPreview() {
    RimbaAITheme {
        // Untuk preview, Anda mungkin ingin menampilkan layar tertentu
        // atau AppNavHost dengan startDestination yang spesifik.
        // Namun, AppNavHost akan memulai dari HomeScreen secara default.
        AppNavHost()
    }
}
