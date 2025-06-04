package com.example.rimbaai

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.rimbaai.ui.theme.RimbaAITheme
import androidx.compose.foundation.lazy.grid.GridCells // Import yang mungkin terlewat
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid // Import yang mungkin terlewat
import androidx.compose.foundation.lazy.grid.items // Import yang mungkin terlewat


// PASTIKAN DEFINISI DATA CLASS DAN SAMPLE DATA ADA DI SINI (TOP LEVEL)
data class QuizQuestion(
    val id: String,
    val questionText: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val animalImageUrl: String? = null
)

val sampleQuizQuestions = listOf(
    QuizQuestion("q1", "Apa makanan utama Harimau Sumatera?", listOf("Buah-buahan", "Rusa dan Babi Hutan", "Ikan", "Serangga"), 1, "rimba_placeholder_tiger"),
    QuizQuestion("q2", "Di pulau mana Komodo dapat ditemukan secara alami?", listOf("Jawa", "Sumatera", "Kalimantan", "Flores dan sekitarnya"), 3, "rimba_placeholder_komodo"),
    QuizQuestion("q3", "Apa status konservasi Badak Jawa menurut IUCN?", listOf("Aman", "Rentan", "Terancam Punah", "Kritis"), 3, "rimba_placeholder_rhino"),
    QuizQuestion("q4", "Manakah dari hewan berikut yang merupakan primata endemik Kalimantan dengan hidung besar?", listOf("Orangutan", "Bekantan", "Lutung", "Monyet Ekor Panjang"), 1, "rimba_placeholder_bekantan"),
    QuizQuestion("q5", "Elang Jawa sering diidentikkan dengan lambang negara Indonesia, yaitu?", listOf("Pohon Beringin", "Garuda", "Banteng", "Padi dan Kapas"), 1, "rimba_placeholder_eagle")
)

@Composable
fun AnswerFeedbackDialog(
    isCorrect: Boolean,
    onDismiss: () -> Unit
) {
    val dialogBackgroundColor = Color.White
    val titleTextColor = Color(0xFF273240)
    val correctColor = Color(0xFF4CAF50)
    val incorrectColor = Color(0xFFF44336)
    val buttonAccentColor = if (isCorrect) correctColor else Color(0xFF2196F3)

    Dialog(onDismissRequest = { /* Biarkan kosong, tutup hanya via tombol */ }) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = dialogBackgroundColor),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(all = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = if (isCorrect) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                    contentDescription = if (isCorrect) "Jawaban Benar" else "Jawaban Salah",
                    tint = if (isCorrect) correctColor else incorrectColor,
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = if (isCorrect) "Jawaban Benar!" else "Jawaban Salah!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleTextColor,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = buttonAccentColor)
                ) {
                    Text(
                        if (isCorrect) "Lanjut" else "Coba Lagi Nanti",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizGameScreen(
    questions: List<QuizQuestion> = sampleQuizQuestions,
    onNavigateBack: () -> Unit,
    onQuizComplete: (Int, Int) -> Unit
) {
    val lightBackgroundColor = Color(0xFFF7F9FC)
    // val accentColor = Color(0xFF2196F3) // Tidak digunakan langsung untuk tombol pilihan lagi
    val textPrimaryColor = Color(0xFF273240)
    val defaultOptionButtonColor = Color.White
    val selectedOptionBorderColor = Color(0xFF2196F3)

    var currentQuestionIndex by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var selectedAnswerIndex by remember { mutableStateOf<Int?>(null) }
    var answerSubmitted by remember { mutableStateOf(false) }

    var showAnswerFeedbackDialog by remember { mutableStateOf(false) }
    var isAnswerCorrectForFeedback by remember { mutableStateOf(false) }

    var showScoreDialog by remember { mutableStateOf(false) }

    // Pastikan questions tidak kosong sebelum mengakses elemennya
    if (questions.isEmpty()) {
        // Tampilkan pesan atau kembali jika tidak ada pertanyaan
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Tidak ada pertanyaan kuis tersedia.")
        }
        return // Keluar dari Composable jika tidak ada pertanyaan
    }
    val currentQuestion = questions[currentQuestionIndex]


    val handleSubmitAnswer = { optionIndex: Int ->
        if (!answerSubmitted) {
            selectedAnswerIndex = optionIndex
            answerSubmitted = true

            val isCorrect = optionIndex == currentQuestion.correctAnswerIndex
            isAnswerCorrectForFeedback = isCorrect
            if (isCorrect) {
                score++
            }
            showAnswerFeedbackDialog = true
        }
    }

    val proceedToNextStep = {
        showAnswerFeedbackDialog = false
        if (currentQuestionIndex < questions.size - 1) {
            currentQuestionIndex++
            answerSubmitted = false
            selectedAnswerIndex = null
        } else {
            showScoreDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Kuis Satwa (${currentQuestionIndex + 1}/${questions.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = textPrimaryColor
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        onNavigateBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = textPrimaryColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = lightBackgroundColor
                )
            )
        },
        containerColor = lightBackgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = currentQuestion.questionText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimaryColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                )

                currentQuestion.options.forEachIndexed { index, optionText ->
                    val isSelected = selectedAnswerIndex == index && answerSubmitted

                    OutlinedButton(
                        onClick = { handleSubmitAnswer(index) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = defaultOptionButtonColor,
                            contentColor = textPrimaryColor
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) selectedOptionBorderColor else Color.LightGray
                        ),
                        enabled = !answerSubmitted
                    ) {
                        Text(text = optionText, fontSize = 16.sp, textAlign = TextAlign.Start, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }

        if (showAnswerFeedbackDialog) {
            AnswerFeedbackDialog(
                isCorrect = isAnswerCorrectForFeedback,
                onDismiss = proceedToNextStep
            )
        }

        if (showScoreDialog) {
            ScoreDialog(
                score = score,
                totalQuestions = questions.size,
                onDismiss = {
                    showScoreDialog = false
                    onQuizComplete(score, questions.size)
                }
            )
        }
    }
}

@Composable
fun ScoreDialog(
    score: Int,
    totalQuestions: Int,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Kuis Selesai!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF273240)
                )
                Text(
                    "Skor Kamu:",
                    fontSize = 18.sp,
                    color = Color(0xFF333333)
                )
                Text(
                    "$score / $totalQuestions",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (score >= totalQuestions / 2) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                ) {
                    Text("Kembali ke Menu", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun QuizGameScreenPreview() {
    RimbaAITheme {
        QuizGameScreen(onNavigateBack = {}, onQuizComplete = { _, _ -> })
    }
}

@Preview(showBackground = true)
@Composable
fun AnswerFeedbackDialogCorrectPreview() {
    RimbaAITheme {
        AnswerFeedbackDialog(isCorrect = true, onDismiss = {})
    }
}

@Preview(showBackground = true)
@Composable
fun AnswerFeedbackDialogIncorrectPreview() {
    RimbaAITheme {
        AnswerFeedbackDialog(isCorrect = false, onDismiss = {})
    }
}
