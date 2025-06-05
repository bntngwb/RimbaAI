package com.example.rimbaai

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview // Impor untuk CameraX Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
// import androidx.compose.ui.tooling.preview.Preview // Impor lama yang menyebabkan konflik
import androidx.compose.ui.tooling.preview.Preview as ComposePreview // <-- GUNAKAN ALIAS IMPORT
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.rimbaai.ui.theme.RimbaAITheme
import androidx.compose.ui.draw.clip // <-- TAMBAHKAN IMPORT INI

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
            if (hasCameraPermission) {
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
                            // Sekarang 'Preview' di sini akan merujuk ke androidx.camera.core.Preview
                            val previewUseCase = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                            try {
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    previewUseCase // Menggunakan variabel dengan nama yang jelas
                                )
                            } catch (exc: Exception) {
                                Log.e("CameraScanScreen", "Gagal melakukan binding use case kamera", exc)
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .aspectRatio(3f / 4f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .aspectRatio(3f / 4f)
                        .background(Color.Black.copy(alpha = 0.8f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
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
                        // Cek apakah kita harus menampilkan UI untuk mengarahkan ke pengaturan
                        val activity = context.findActivity()
                        if (activity != null && !hasCameraPermission &&
                            !androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA) &&
                            // Tambahkan cek agar tidak muncul saat pertama kali dialog izin sistem muncul
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
                "Arahkan bingkai kamera ke satwa untuk identifikasi.",
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = textPrimaryColor.copy(alpha = 0.8f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { /* TODO: Logika untuk tangkap gambar */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                    enabled = hasCameraPermission
                ) {
                    Text("Tangkap Gambar", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { /* TODO: Logika untuk unggah gambar */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(accentColor)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = accentColor
                    )
                ) {
                    Text("Unggah Gambar", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
            }
        }
    }
}

fun android.content.Context.findActivity(): android.app.Activity? = when (this) {
    is android.app.Activity -> this
    is android.content.ContextWrapper -> baseContext.findActivity()
    else -> null
}

// Anotasi @Preview sekarang menggunakan alias ComposePreview
@ComposePreview(showBackground = true, device = "id:pixel_5")
@Composable
fun CameraScanScreenPreview() {
    RimbaAITheme {
        CameraScanScreen(onNavigateBack = {})
    }
}