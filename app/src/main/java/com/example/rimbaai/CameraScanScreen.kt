package com.example.rimbaai

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri // <-- TAMBAHKAN IMPORT UNTUK URI
import android.util.Log
import android.widget.Toast // <-- TAMBAHKAN IMPORT UNTUK TOAST
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview as ComposePreview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter // <-- TAMBAHKAN IMPORT UNTUK COIL
import com.example.rimbaai.ui.theme.RimbaAITheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScanScreen(
    onNavigateBack: () -> Unit
) {
    val lightBackgroundColor = Color(0xFFF7F9FC)
    val textPrimaryColor = Color(0xFF273240)
    val accentColor = Color(0xFFFF9800)

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    // State untuk menyimpan URI gambar yang dipilih dari galeri
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher untuk memilih gambar dari galeri
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        if (uri != null) {
            Toast.makeText(context, "Gambar dipilih: $uri", Toast.LENGTH_SHORT).show()
            Log.d("CameraScanScreen", "Image URI: $uri")
            // Di sini Anda bisa memproses URI gambar, misalnya mengirimnya untuk identifikasi
        } else {
            Toast.makeText(context, "Tidak ada gambar dipilih", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(key1 = true) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Identifikasi Satwa",
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
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Area Pratinjau (Kamera atau Gambar yang Dipilih)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .aspectRatio(3f / 4f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    // Tampilkan gambar yang dipilih jika ada
                    Image(
                        painter = rememberAsyncImagePainter(model = selectedImageUri),
                        contentDescription = "Gambar yang dipilih",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit // Atau Crop, sesuai kebutuhan
                    )
                } else if (hasCameraPermission) {
                    // Tampilkan pratinjau kamera jika izin ada dan tidak ada gambar dipilih
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx).apply {
                                layoutParams = android.view.ViewGroup.LayoutParams(
                                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                                )
                            }
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()
                                val previewUseCase = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }
                                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                try {
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        cameraSelector,
                                        previewUseCase
                                    )
                                } catch (exc: Exception) {
                                    Log.e("CameraScanScreen", "Gagal melakukan binding use case kamera", exc)
                                }
                            }, ContextCompat.getMainExecutor(ctx))
                            previewView
                        },
                        modifier = Modifier.fillMaxSize() // AndroidView mengisi Box
                    )
                } else {
                    // Tampilan jika izin kamera ditolak atau belum diberikan
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Izin kamera diperlukan untuk fitur ini.",
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                        ) {
                            Text("Berikan Izin", color = Color.White)
                        }
                        val activity = context.findActivity()
                        if (activity != null && !hasCameraPermission &&
                            !androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA) &&
                            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                        ) {
                            Text(
                                "Anda telah menolak izin kamera. Aktifkan melalui pengaturan aplikasi.",
                                color = Color.White.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                if (selectedImageUri != null) "Gambar dipilih. Siap untuk diidentifikasi?"
                else "Arahkan bingkai kamera ke satwa atau unggah gambar.",
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = textPrimaryColor.copy(alpha = 0.8f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        if (selectedImageUri != null) {
                            // TODO: Logika untuk identifikasi gambar yang diunggah
                            Toast.makeText(context, "Identifikasi gambar dari galeri...", Toast.LENGTH_SHORT).show()
                        } else {
                            // TODO: Logika untuk tangkap gambar dari kamera
                            Toast.makeText(context, "Tangkap gambar dari kamera...", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    // Tombol utama sekarang bisa "Identifikasi" atau "Tangkap Gambar"
                    enabled = hasCameraPermission || selectedImageUri != null
                ) {
                    Text(
                        if (selectedImageUri != null) "Identifikasi Gambar Ini" else "Tangkap Gambar",
                        color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {
                        // Selalu luncurkan image picker saat tombol ini ditekan
                        imagePickerLauncher.launch("image/*")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(accentColor)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = accentColor
                    )
                ) {
                    Text(
                        if (selectedImageUri != null) "Ganti Gambar" else "Unggah Gambar",
                        fontWeight = FontWeight.SemiBold, fontSize = 16.sp
                    )
                }
            }
        }
    }
}

// Fungsi helper findActivity() tetap sama
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