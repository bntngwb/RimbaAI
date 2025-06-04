package com.example.rimbaai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue // Import untuk delegate
import androidx.compose.runtime.mutableStateOf // Import untuk mutableStateOf
import androidx.compose.runtime.remember // Import untuk remember
import androidx.compose.runtime.setValue // Import untuk delegate
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog // Import untuk Dialog
import com.example.rimbaai.ui.theme.RimbaAITheme

// Composable untuk kartu utama di halaman kuis, meniru gaya MainHeroCard
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizHeroCard(
    title: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = accentColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                lineHeight = 30.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun HowToPlayDialog(
    onDismissRequest: () -> Unit
) {
    val dialogBackgroundColor = Color.White
    val titleTextColor = Color(0xFF273240)
    val contentTextColor = Color(0xFF333333)
    val buttonAccentColor = Color(0xFF2196F3) // Biru untuk tombol "Mengerti"

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = dialogBackgroundColor),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(all = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Panduan Bermain",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleTextColor,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start, // Teks instruksi rata kiri
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("1. Akan terdapat soal dengan pertanyaan acak.", fontSize = 15.sp, color = contentTextColor)
                    Text("2. Pilih jawaban yang benar dari pilihan jawaban yang disediakan.", fontSize = 15.sp, color = contentTextColor)
                    Text("3. Hati-hati terdapat waktu disetiap pertanyaan yang dapat mengurangi skor.", fontSize = 15.sp, color = contentTextColor)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismissRequest,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = buttonAccentColor)
                ) {
                    Text("Mengerti", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizLandingScreen(
    onNavigateBack: () -> Unit,
    onStartQuiz: () -> Unit,
    onShowHowToPlay: () -> Unit // Tetap ada, tapi akan dikelola internal dialognya
) {
    val lightBackgroundColor = Color(0xFFF7F9FC)
    val heroCardAccentColor = Color(0xFFFF9800)
    val quizButtonAccentColor = Color(0xFF2196F3)
    val textPrimaryColor = Color(0xFF273240)
    val buttonTextColor = Color.White
    val increasedButtonFontSize = 16.sp

    // State untuk mengontrol visibilitas dialog "Cara Bermain"
    var showHowToPlayDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Kuis Edukasi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = textPrimaryColor
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.wrapContentHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                QuizHeroCard(
                    title = "Apakah kamu siap\nuntuk menguji pengetahuanmu\ntentang satwa liar?",
                    accentColor = heroCardAccentColor,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onStartQuiz, // Aksi ini akan diteruskan ke NavHost
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = quizButtonAccentColor,
                            contentColor = buttonTextColor
                        )
                    ) {
                        Icon(
                            Icons.Filled.PlayArrow,
                            contentDescription = "Mulai",
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(
                            "Mulai Permainan",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = increasedButtonFontSize
                        )
                    }

                    OutlinedButton(
                        onClick = { showHowToPlayDialog = true }, // Mengubah state untuk menampilkan dialog
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(quizButtonAccentColor)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = quizButtonAccentColor
                        )
                    ) {
                        Icon(
                            Icons.Filled.HelpOutline,
                            contentDescription = "Cara Bermain",
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(
                            "Cara Bermain",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = increasedButtonFontSize
                        )
                    }
                }
            }
        }
        // Tampilkan dialog jika showHowToPlayDialog adalah true
        if (showHowToPlayDialog) {
            HowToPlayDialog(onDismissRequest = { showHowToPlayDialog = false })
        }
    }
}

@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun QuizLandingScreenPreview() {
    RimbaAITheme {
        QuizLandingScreen(
            onNavigateBack = {},
            onStartQuiz = {},
            onShowHowToPlay = {} // Lambda onShowHowToPlay di preview tidak akan melakukan apa-apa
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HowToPlayDialogPreview() {
    RimbaAITheme {
        HowToPlayDialog(onDismissRequest = {})
    }
}
