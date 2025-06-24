package com.example.rimbaai

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ImageBitmap as ComposeImageBitmap // Alias untuk ImageBitmap Compose
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
import coil.compose.rememberAsyncImagePainter
import com.example.rimbaai.data.Prediction // Pastikan path ini benar
import com.example.rimbaai.ui.theme.RimbaAITheme
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// Composable DetectionResultDialog (kode dari atas)

// Fungsi helper uriToAndroidBitmap (Tidak ada perubahan)
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

// Fungsi untuk menyimpan Bitmap ke Galeri (MediaStore)
fun saveBitmapToGallery(context: Context, bitmap: Bitmap, displayName: String): Uri? {
    val imageOutStream: OutputStream?
    val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    } else {
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }

    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "$displayName.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
    }

    val imageUri = context.contentResolver.insert(imageCollection, contentValues)
    imageUri?.let {
        try {
            imageOutStream = context.contentResolver.openOutputStream(it)
            imageOutStream?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                context.contentResolver.update(it, contentValues, null, null)
            }
            return it
        } catch (e: Exception) {
            Log.e("SaveToGallery", "Error saving bitmap: ${e.message}", e)
            // Jika error, coba hapus entri yang mungkin sudah dibuat
            try { context.contentResolver.delete(it, null, null) } catch (e2: Exception) { /* Abaikan error saat menghapus */ }
        }
    }
    return null
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScanScreen(
    onNavigateBack: () -> Unit,
    cameraScanViewModel: CameraScanViewModel = viewModel()
) {
    val lightBackgroundColor = Color(0xFFF7F9FC)
    val textPrimaryColor = Color(0xFF273240)
    val accentColor = Color(0xFFFF9800)

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) }
    // Tambahkan izin WRITE_EXTERNAL_STORAGE jika menargetkan API < 29 untuk penyimpanan galeri
    // Untuk API >= 29, MediaStore API tidak memerlukan izin eksplisit untuk menyimpan ke koleksi media aplikasi sendiri.
    // Namun, jika menyimpan ke direktori bersama (yang tidak direkomendasikan), izin mungkin diperlukan.
    // Untuk MediaStore.Images.Media.VOLUME_EXTERNAL_PRIMARY, izin tidak diperlukan di Q+.

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
            if (!granted) {
                Toast.makeText(context, "Izin kamera ditolak.", Toast.LENGTH_SHORT).show()
            }
        }
    )

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var capturedBitmapForDisplay by remember { mutableStateOf<ComposeImageBitmap?>(null) } // ComposeImageBitmap untuk ditampilkan
    var lastProcessedBitmap by remember { mutableStateOf<Bitmap?>(null) } // android.graphics.Bitmap yang terakhir diproses

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            capturedBitmapForDisplay = null // Reset gambar dari kamera jika ada
            cameraScanViewModel.resetResults()
            uriToAndroidBitmap(context, uri)?.let { bitmap ->
                lastProcessedBitmap = bitmap // Simpan bitmap yang akan diproses
                capturedBitmapForDisplay = bitmap.asImageBitmap() // Tampilkan di UI
                // Tidak langsung identifikasi, tunggu tombol "Identifikasi"
            } ?: Toast.makeText(context, "Gagal memuat gambar dari galeri.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Tidak ada gambar dipilih", Toast.LENGTH_SHORT).show()
        }
    }

    val isLoading = cameraScanViewModel.isLoading
    val detectionResult = cameraScanViewModel.detectionResult
    val detectionError = cameraScanViewModel.detectionError
    var showResultDialog by remember { mutableStateOf(false) }

    LaunchedEffect(detectionResult) {
        if (detectionResult != null) {
            showResultDialog = true
        }
    }
    LaunchedEffect(detectionError) {
        if (detectionError != null) {
            Toast.makeText(context, "Error Deteksi: $detectionError", Toast.LENGTH_LONG).show()
            cameraScanViewModel.resetResults()
        }
    }
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val imageCapture = remember { ImageCapture.Builder().build() }
    lateinit var cameraExecutor: ExecutorService

    DisposableEffect(Unit) {
        cameraExecutor = Executors.newSingleThreadExecutor()
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Identifikasi Satwa", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = textPrimaryColor) }, navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Kembali", tint = textPrimaryColor) } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = lightBackgroundColor)) },
        containerColor = lightBackgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .aspectRatio(3f / 4f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = accentColor)
                } else if (capturedBitmapForDisplay != null) {
                    Image(bitmap = capturedBitmapForDisplay!!, contentDescription = "Gambar ditangkap/dipilih", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                } else if (selectedImageUri != null) { // Ini seharusnya sudah dihandle oleh capturedBitmapForDisplay
                    Image(painter = rememberAsyncImagePainter(model = selectedImageUri), contentDescription = "Gambar yang dipilih", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                } else if (hasCameraPermission) {
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx).apply {
                                layoutParams = android.view.ViewGroup.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT)
                            }
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()
                                val previewUseCase = Preview.Builder().build().also { it.setSurfaceProvider(previewView.surfaceProvider) }
                                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                try {
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, previewUseCase, imageCapture)
                                } catch (exc: Exception) { Log.e("CameraScanScreen", "Gagal binding use cases kamera", exc) }
                            }, ContextCompat.getMainExecutor(ctx))
                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
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
                    capturedBitmapForDisplay != null -> "Gambar siap untuk diidentifikasi."
                    else -> "Arahkan kamera atau unggah gambar."
                },
                fontSize = 14.sp, textAlign = TextAlign.Center, color = textPrimaryColor.copy(alpha = 0.8f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        if (lastProcessedBitmap != null) { // Jika ada gambar dari galeri yang sudah di-load
                            cameraScanViewModel.detectAnimalFromBitmap(lastProcessedBitmap!!)
                        } else if (hasCameraPermission) { // Jika mode kamera aktif, tangkap gambar
                            val photoFile = File(context.externalCacheDir ?: context.cacheDir, "RimbaAICapture_${System.currentTimeMillis()}.jpg")
                            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                            imageCapture.takePicture(
                                outputOptions,
                                cameraExecutor,
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                        val savedUri = output.savedUri ?: Uri.fromFile(photoFile)
                                        uriToAndroidBitmap(context, savedUri)?.let { bitmap ->
                                            (context as? Activity)?.runOnUiThread {
                                                lastProcessedBitmap = bitmap // Simpan untuk dialog
                                                capturedBitmapForDisplay = bitmap.asImageBitmap()
                                                selectedImageUri = null
                                            }
                                            cameraScanViewModel.detectAnimalFromBitmap(bitmap)
                                        } ?: (context as? Activity)?.runOnUiThread {
                                            Toast.makeText(context, "Gagal memuat gambar yang ditangkap.", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    override fun onError(exc: ImageCaptureException) {
                                        Log.e("CameraScanScreen", "Gagal mengambil foto: ${exc.message}", exc)
                                        (context as? Activity)?.runOnUiThread {
                                            Toast.makeText(context, "Gagal mengambil foto: ${exc.localizedMessage}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            )
                        } else if (selectedImageUri != null && lastProcessedBitmap == null){ // Kasus gambar dari galeri belum di-load ke lastProcessedBitmap
                            uriToAndroidBitmap(context, selectedImageUri!!)?.let { bitmap ->
                                lastProcessedBitmap = bitmap
                                capturedBitmapForDisplay = bitmap.asImageBitmap()
                                cameraScanViewModel.detectAnimalFromBitmap(bitmap)
                            } ?: Toast.makeText(context, "Gagal memproses gambar dari galeri.", Toast.LENGTH_SHORT).show()
                        }
                        else {
                            Toast.makeText(context, "Tidak ada gambar untuk diidentifikasi atau izin kamera diperlukan.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    enabled = (lastProcessedBitmap != null || selectedImageUri != null || hasCameraPermission) && !isLoading
                ) {
                    Text(
                        if (lastProcessedBitmap != null || selectedImageUri != null) "Identifikasi Gambar" else "Tangkap & Identifikasi",
                        color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { if (!isLoading) {
                        cameraScanViewModel.resetResults()
                        selectedImageUri = null
                        capturedBitmapForDisplay = null
                        lastProcessedBitmap = null
                        imagePickerLauncher.launch("image/*")
                    }},
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(accentColor)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = accentColor),
                    enabled = !isLoading
                ) {
                    Text(
                        "Unggah Gambar dari Galeri",
                        fontWeight = FontWeight.SemiBold, fontSize = 16.sp
                    )
                }
            }
        }

        if (showResultDialog && detectionResult != null) {
            DetectionResultDialog(
                prediction = detectionResult!!,
                capturedImage = lastProcessedBitmap?.asImageBitmap(), // Kirim bitmap yang diproses ke dialog
                onDismissRequest = {
                    showResultDialog = false
                    cameraScanViewModel.resetResults()
                    // Pertimbangkan untuk tidak mereset selectedImageUri/capturedBitmapForDisplay di sini
                    // agar pengguna bisa melihat gambar yang baru saja diidentifikasi
                },
                onSaveCapture = {
                    if (lastProcessedBitmap != null) {
                        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                        val displayName = "RimbaAI_${detectionResult?.`class` ?: "Capture"}_$timeStamp"
                        val savedUri = saveBitmapToGallery(context, lastProcessedBitmap!!, displayName)
                        if (savedUri != null) {
                            Toast.makeText(context, "Gambar disimpan ke galeri: $displayName", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Gagal menyimpan gambar.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Tidak ada gambar untuk disimpan.", Toast.LENGTH_SHORT).show()
                    }
                    showResultDialog = false // Tutup dialog setelah mencoba menyimpan
                    // Reset state setelah dialog ditutup jika perlu
                    // cameraScanViewModel.resetResults()
                    // selectedImageUri = null
                    // capturedBitmapForDisplay = null
                    // lastProcessedBitmap = null
                },
                onRepeatIdentification = {
                    showResultDialog = false
                    // Membersihkan hasil deteksi sebelumnya
                    cameraScanViewModel.resetResults()

                    if (lastProcessedBitmap != null) { // Jika ada gambar sebelumnya
                        // Tampilkan kembali gambar sebelumnya untuk diidentifikasi ulang
                        capturedBitmapForDisplay = lastProcessedBitmap?.asImageBitmap()
                        // Tombol "Identifikasi" akan aktif untuk gambar ini
                    } else if (selectedImageUri != null) {
                        // Jika berasal dari URI galeri dan belum di-load, load lagi
                        uriToAndroidBitmap(context, selectedImageUri!!)?.let { bitmap ->
                            lastProcessedBitmap = bitmap
                            capturedBitmapForDisplay = bitmap.asImageBitmap()
                        }
                    }
                    // Jangan langsung identifikasi, biarkan pengguna menekan tombol utama lagi
                    // jika mereka ingin identifikasi ulang gambar yang sama atau mengambil/memilih yang baru
                }
            )
        }
    }
}

// Helper function untuk menemukan Activity dari Context (Tidak ada perubahan)
fun android.content.Context.findActivity(): android.app.Activity? = when (this) {
    is android.app.Activity -> this
    is android.content.ContextWrapper -> baseContext.findActivity()
    else -> null
}

// Preview (Tidak ada perubahan)
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
        // Contoh untuk confidence tinggi
        DetectionResultDialog(
            prediction = Prediction(0.0,0.0,0.0,0.0,0.899, "Singa Test"),
            capturedImage = null, // Berikan contoh bitmap jika perlu
            onDismissRequest = {},
            onSaveCapture = {},
            onRepeatIdentification = {}
        )
        // Anda bisa menambahkan @Preview lain untuk kasus confidence rendah
    }
}