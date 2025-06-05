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
    const val QUIZ_GAME_ROUTE = "quiz_game"
    const val CAMERASCAN_ROUTE = "camerascan" // <-- RUTE BARU
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
                    navController.navigate(AppDestinations.CAMERASCAN_ROUTE) // <-- NAVIGASI KE CAMERASCAN
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
            EncyclopediaScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(AppDestinations.CHATBOT_ROUTE) {
            ChatbotScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(AppDestinations.QUIZ_LANDING_ROUTE) {
            QuizLandingScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onStartQuiz = {
                    navController.navigate(AppDestinations.QUIZ_GAME_ROUTE)
                },
                onShowHowToPlay = {
                    println("Tombol Cara Bermain diklik!")
                }
            )
        }
        composable(AppDestinations.QUIZ_GAME_ROUTE) {
            QuizGameScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onQuizComplete = { score, totalQuestions ->
                    println("Kuis Selesai! Skor: $score/$totalQuestions")
                    navController.popBackStack(AppDestinations.QUIZ_LANDING_ROUTE, inclusive = false)
                }
            )
        }
        // Composable baru untuk CameraScanScreen
        composable(AppDestinations.CAMERASCAN_ROUTE) {
            CameraScanScreen( // Pastikan CameraScanScreen.kt sudah ada dan bisa diimpor
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}