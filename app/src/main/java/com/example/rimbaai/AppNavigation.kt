package com.example.rimbaai

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// Definisikan rute sebagai konstanta agar mudah dikelola
object AppDestinations {
    const val HOME_ROUTE = "home"
    const val ENCYCLOPEDIA_ROUTE = "encyclopedia"
    const val CHATBOT_ROUTE = "chatbot"
    const val QUIZ_LANDING_ROUTE = "quiz_landing"
    const val QUIZ_GAME_ROUTE = "quiz_game" // Rute BARU untuk layar permainan kuis
    // const val HOW_TO_PLAY_ROUTE = "how_to_play" // Siapkan rute untuk cara bermain nanti
}

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = AppDestinations.HOME_ROUTE
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(AppDestinations.HOME_ROUTE) {
            HomeScreen(
                onNavigateToIdentification = {
                    // TODO: Navigasi ke layar identifikasi
                    println("Tombol Identifikasi diklik dari NavHost!")
                },
                onNavigateToEncyclopedia = {
                    navController.navigate(AppDestinations.ENCYCLOPEDIA_ROUTE)
                },
                onNavigateToChatbot = {
                    navController.navigate(AppDestinations.CHATBOT_ROUTE)
                },
                onNavigateToQuiz = {
                    navController.navigate(AppDestinations.QUIZ_LANDING_ROUTE)
                }
            )
        }
        composable(AppDestinations.ENCYCLOPEDIA_ROUTE) {
            EncyclopediaScreen( // Pastikan EncyclopediaScreen.kt sudah ada dan bisa diimpor
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(AppDestinations.CHATBOT_ROUTE) {
            ChatbotScreen( // Pastikan ChatbotScreen.kt sudah ada dan bisa diimpor
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(AppDestinations.QUIZ_LANDING_ROUTE) {
            QuizLandingScreen( // Pastikan QuizLandingScreen.kt sudah ada dan bisa diimpor
                onNavigateBack = {
                    navController.popBackStack()
                },
                onStartQuiz = {
                    // Navigasi ke layar permainan kuis yang sebenarnya
                    navController.navigate(AppDestinations.QUIZ_GAME_ROUTE)
                },
                onShowHowToPlay = {
                    // TODO: Navigasi ke layar cara bermain atau tampilkan dialog/bottom sheet
                    // navController.navigate(AppDestinations.HOW_TO_PLAY_ROUTE)
                    println("Tombol Cara Bermain diklik!")
                }
            )
        }
        // Composable baru untuk QuizGameScreen
        composable(AppDestinations.QUIZ_GAME_ROUTE) {
            QuizGameScreen( // Pastikan QuizGameScreen.kt sudah ada dan bisa diimpor
                onNavigateBack = {
                    navController.popBackStack()
                },
                onQuizComplete = { score, totalQuestions ->
                    // Setelah kuis selesai, kembali ke QuizLandingScreen
                    // Anda bisa juga menampilkan skor di sini atau meneruskannya ke layar lain jika perlu
                    println("Kuis Selesai! Skor: $score/$totalQuestions")
                    // Kembali ke QuizLandingScreen. inclusive = false berarti QuizLandingScreen tidak di-pop.
                    navController.popBackStack(AppDestinations.QUIZ_LANDING_ROUTE, inclusive = false)
                    // Atau jika ingin kembali ke HomeScreen setelah kuis:
                    // navController.popBackStack(AppDestinations.HOME_ROUTE, inclusive = false)
                    // Atau navigasi ke layar skor khusus jika ada:
                    // navController.navigate("score_screen/$score/$totalQuestions") {
                    //     popUpTo(AppDestinations.QUIZ_LANDING_ROUTE) { inclusive = true }
                    // }
                }
            )
        }
    }
}
