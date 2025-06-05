package com.example.rimbaai

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap // Diperlukan untuk ViewModel jika masih ada metode detectAnimalFromBitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
// import androidx.compose.ui.graphics.asImageBitmap // Tidak lagi dibutuhkan jika tidak menampilkan Bitmap mentah
// import androidx.compose.ui.graphics.ImageBitmap // Tidak lagi dibutuhkan
// import androidx.compose.ui.graphics.asAndroidBitmap // Tidak lagi dibutuhkan
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview as ComposePreview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter // Untuk menampilkan gambar dari URI
import com.example.rimbaai.data.Prediction
import com.example.rimbaai.ui.theme.RimbaAITheme
// Hapus impor untuk pustaka cropper yang tidak digunakan lagi
// import com.mr0xf00.easycrop.CropResult
// import com.mr0xf00.easycrop.ImageCropper
// import com.mr0xf00.easycrop.rememberImageCropperState
// import kotlinx.coroutines.launch


// Composable DetectionResultDialog (Tidak ada perubahan)
@Composable
fun DetectionResultDialog(
    prediction: Prediction,
    onDismissRequest: () -> Unit
) {
    val dialogBackgroundColor = Color.White
    val titleTextColor = Color(0xFF273240)
    val contentTextColor = Color(0xFF333333)
    val buttonAccentColor = Color(0xFF2196F3)

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = dialogBackgroundColor),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(all = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Hasil Deteksi", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = titleTextColor, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(24.dp))
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Nama Hewan: ${prediction.`class`}", fontSize = 16.sp, color = contentTextColor)
                    Text("Confidence: ${String.format("%.2f", prediction.confidence * 100)}%", fontSize = 16.sp, color = contentTextColor)
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onDismissRequest, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = buttonAccentColor)) {
                    Text("Tutup", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}


// Fungsi helper uriToAndroidBitmap (mungkin masih berguna jika ViewModel memerlukan Bitmap)
// Namun, jika ViewModel bisa langsung dari URI, ini mungkin tidak diperlukan di Composable ini
fun uriToAndroidBitmap(context: Context, imageUri: Uri): android.graphics.Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, imageUri))
        } else {
            @Suppress("DEPRECATION")
            MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
        }
    } catch (e: Exception) {
        Log.e("UriToBitmap", "Error converting URI to Bitmap: ${e.message}", e)
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScanScreen(
    onNavigateBack: () -> Unit,
    cameraScanViewModel: CameraScanViewModel = viewModel() // ViewModel tetap ada
) {
    val lightBackgroundColor = Color(0xFFF7F9FC)
    val textPrimaryColor = Color(0xFF273240)
    val accentColor = Color(0xFFFF9800)

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted; if (!granted) Toast.makeText(context, "Izin kamera ditolak.", Toast.LENGTH_SHORT).show() }
    )

    // State untuk menyimpan URI gambar yang dipilih dari galeri
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri // Langsung set selectedImageUri
            cameraScanViewModel.resetResults() // Reset hasil deteksi sebelumnya jika ada
            // Tidak ada lagi showCropperScreen
            Toast.makeText(context, "Gambar dipilih. Siap untuk identifikasi.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Tidak ada gambar dipilih", Toast.LENGTH_SHORT).show()
        }
    }

    val isLoading = cameraScanViewModel.isLoading
    val detectionResult = cameraScanViewModel.detectionResult
    val detectionError = cameraScanViewModel.detectionError
    var showResultDialog by remember { mutableStateOf(false) }

    LaunchedEffect(detectionResult) { if (detectionResult != null) showResultDialog = true }
    LaunchedEffect(detectionError) { if (detectionError != null) { Toast.makeText(context, "Error Deteksi: $detectionError", Toast.LENGTH_LONG).show(); cameraScanViewModel.resetResults() } }
    LaunchedEffect(Unit) { if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA) }


    Scaffold(
        topBar = { TopAppBar(title = { Text("Identifikasi Satwa", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = textPrimaryColor) }, navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali", tint = textPrimaryColor) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = lightBackgroundColor)) },
        containerColor = lightBackgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues).fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f).aspectRatio(3f / 4f).clip(RoundedCornerShape(12.dp)).background(Color.Black.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = accentColor)
                } else if (selectedImageUri != null) { // Tampilkan gambar yang dipilih dari galeri
                    Image(
                        painter = rememberAsyncImagePainter(model = selectedImageUri), // Menggunakan Coil
                        contentDescription = "Gambar yang dipilih",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else if (hasCameraPermission) { // Tampilkan pratinjau kamera
                    AndroidView(
                        factory = { ctx ->
                            PreviewView(ctx).apply {
                                layoutParams = android.view.ViewGroup.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT)
                                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                cameraProviderFuture.addListener({
                                    val cameraProvider = cameraProviderFuture.get()
                                    val previewUseCase = Preview.Builder().build().also { it.setSurfaceProvider(surfaceProvider) }
                                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                    try {
                                        cameraProvider.unbindAll(); cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, previewUseCase)
                                    } catch (exc: Exception) { Log.e("CameraScanScreen", "Gagal binding kamera", exc) }
                                }, ContextCompat.getMainExecutor(ctx))
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else { // Tampilan permintaan izin
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.padding(16.dp)) {
                        Text("Izin kamera diperlukan untuk fitur ini.", color = Color.White, textAlign = TextAlign.Center, modifier = Modifier.padding(bottom = 8.dp))
                        Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }, colors = ButtonDefaults.buttonColors(containerColor = accentColor)) { Text("Berikan Izin", color = Color.White) }
                        val activity = context.findActivity()
                        if (activity != null && !hasCameraPermission && !ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA) && ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                            Text("Aktifkan izin kamera via pengaturan aplikasi.", color = Color.White.copy(alpha = 0.7f), textAlign = TextAlign.Center, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = when {
                    isLoading -> "Sedang mengidentifikasi..."
                    selectedImageUri != null && detectionResult == null && detectionError == null -> "Gambar dipilih. Tekan 'Identifikasi' untuk memproses."
                    selectedImageUri != null && detectionResult != null -> "Hasil deteksi ditampilkan."
                    selectedImageUri != null && detectionError != null -> "Gagal mendeteksi. Coba lagi."
                    else -> "Arahkan kamera atau unggah gambar."
                },
                fontSize = 14.sp, textAlign = TextAlign.Center, color = textPrimaryColor.copy(alpha = 0.8f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        if (selectedImageUri != null) {
                            // ViewModel Anda mungkin memiliki detectAnimalFromUri atau Anda bisa
                            // mengonversi URI ke Bitmap di sini lalu memanggil detectAnimalFromBitmap
                            cameraScanViewModel.detectAnimalFromUri(context, selectedImageUri!!)
                        } else {
                            // TODO: Logika "Tangkap & Identifikasi" dari kamera
                            Toast.makeText(context, "Silakan unggah gambar atau tangkap gambar dulu.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    enabled = (selectedImageUri != null || (hasCameraPermission && selectedImageUri == null)) && !isLoading
                ) {
                    Text(
                        if (selectedImageUri != null) "Identifikasi Gambar Terpilih" else "Tangkap & Identifikasi",
                        color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { if (!isLoading) { cameraScanViewModel.resetResults(); selectedImageUri = null; imagePickerLauncher.launch("image/*") } },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(accentColor)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = accentColor),
                    enabled = !isLoading
                ) {
                    Text(
                        if (selectedImageUri !=null) "Pilih/Ganti Gambar dari Galeri" else "Unggah Gambar dari Galeri",
                        fontWeight = FontWeight.SemiBold, fontSize = 16.sp
                    )
                }
            }
        }

        if (showResultDialog && detectionResult != null) {
            DetectionResultDialog(prediction = detectionResult) { showResultDialog = false; cameraScanViewModel.resetResults(); selectedImageUri = null }
        }
    }
}

fun android.content.Context.findActivity(): android.app.Activity? = when (this) {
    is android.app.Activity -> this
    is android.content.ContextWrapper -> baseContext.findActivity()
    else -> null
}

@ComposePreview(showBackground = true, device = "id:pixel_5")
@Composable
fun CameraScanScreenPreview() {
    RimbaAITheme {
        CameraScanScreen(onNavigateBack = {})
    }
}

@ComposePreview(showBackground = true)
@Composable
fun DetectionResultDialogPreview() {
    RimbaAITheme {
        DetectionResultDialog(
            prediction = Prediction(0.0,0.0,0.0,0.0,0.899, "Singa Test"),
            onDismissRequest = {}
        )
    }
}