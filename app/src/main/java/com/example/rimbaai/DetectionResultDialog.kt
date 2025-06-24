package com.example.rimbaai // Pastikan package-nya sesuai dengan struktur proyek Anda

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor // Pastikan impor ini ada
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ImageBitmap as ComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.rimbaai.data.Prediction
import com.example.rimbaai.ui.theme.RimbaAITheme

@Composable
fun DetectionResultDialog(
    prediction: Prediction,
    capturedImage: ComposeImageBitmap?,
    onDismissRequest: () -> Unit,
    onSaveCapture: () -> Unit,
    onRepeatIdentification: () -> Unit
) {
    val dialogBackgroundColor = Color.White
    val titleTextColor = Color(0xFF273240)
    val contentTextColor = Color(0xFF333333)
    val buttonAccentColor = Color(0xFF2196F3)
    val buttonSecondaryColor = Color(0xFF00BCD4) // Misal untuk tombol Ulangi
    val buttonDestructiveColor = Color(0xFF757575) // Warna abu-abu untuk tombol Tutup (Outlined)

    val confidenceThreshold = 0.75

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = dialogBackgroundColor),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(all = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (prediction.confidence >= confidenceThreshold) {
                    Text(
                        "Hasil Deteksi",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = titleTextColor,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    capturedImage?.let {
                        Image(
                            bitmap = it,
                            contentDescription = "Gambar yang dideteksi",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp) // Sedikit disesuaikan tingginya
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.LightGray),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(6.dp) // Mengurangi jarak sedikit
                    ) {
                        Text(
                            "Nama Hewan: ${prediction.`class`}",
                            fontSize = 16.sp,
                            color = contentTextColor,
                            fontWeight = FontWeight.Medium // Sedikit pertebal
                        )
                        Text(
                            "Confidence: ${String.format("%.2f", prediction.confidence * 100)}%",
                            fontSize = 16.sp,
                            color = contentTextColor,
                            fontWeight = FontWeight.Medium // Sedikit pertebal
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    // --- PERBAIKAN TOMBOL DI SINI ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp) // Jarak antar tombol
                    ) {
                        Button(
                            onClick = onSaveCapture,
                            modifier = Modifier
                                .weight(1f) // Memberikan bobot agar lebar sama
                                .height(48.dp), // Menyamakan tinggi tombol
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = buttonAccentColor)
                        ) {
                            Text("Simpan", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                        Button(
                            onClick = onRepeatIdentification,
                            modifier = Modifier
                                .weight(1f) // Memberikan bobot agar lebar sama
                                .height(48.dp), // Menyamakan tinggi tombol
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = buttonSecondaryColor)
                        ) {
                            Text("Ulangi", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp)) // Jarak sebelum tombol Tutup
                    OutlinedButton(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp), // Menyamakan tinggi tombol
                        shape = RoundedCornerShape(12.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(buttonDestructiveColor)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = buttonDestructiveColor)
                    ) {
                        Text("Tutup", fontWeight = FontWeight.SemiBold)
                    }

                } else { // Jika confidence di bawah ambang batas
                    Text(
                        "Tidak Terdeteksi",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = titleTextColor,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Objek tidak dapat diidentifikasi dengan tingkat keyakinan yang cukup. Silakan coba lagi dengan gambar yang lebih jelas atau dari sudut yang berbeda.",
                        fontSize = 16.sp,
                        color = contentTextColor,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp // Menambah jarak antar baris untuk keterbacaan
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp), // Menyamakan tinggi tombol
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = buttonAccentColor)
                    ) {
                        Text("Coba Lagi", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// Preview Functions
@Preview(showBackground = true, name = "Detection Success Dialog Preview")
@Composable
fun DetectionResultDialogSuccessPreview() {
    RimbaAITheme {
        DetectionResultDialog(
            prediction = Prediction(x = 0.0, y = 0.0, width = 0.0, height = 0.0, confidence = 0.85, `class` = "Harimau Sumatera"),
            capturedImage = null, // Anda bisa membuat dummy ComposeImageBitmap jika ingin melihat pratinjau gambar
            onDismissRequest = {},
            onSaveCapture = {},
            onRepeatIdentification = {}
        )
    }
}

@Preview(showBackground = true, name = "Detection Not Detected Dialog Preview")
@Composable
fun DetectionResultDialogNotDetectedPreview() {
    RimbaAITheme {
        DetectionResultDialog(
            prediction = Prediction(x = 0.0, y = 0.0, width = 0.0, height = 0.0, confidence = 0.50, `class` = "Tidak Diketahui"),
            capturedImage = null,
            onDismissRequest = {},
            onSaveCapture = {},
            onRepeatIdentification = {}
        )
    }
}