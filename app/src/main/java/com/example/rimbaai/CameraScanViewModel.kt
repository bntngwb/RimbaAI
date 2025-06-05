package com.example.rimbaai

import android.content.Context
import android.graphics.Bitmap // Import Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rimbaai.data.Prediction
import com.example.rimbaai.data.ROBOFLOW_API_KEY
import com.example.rimbaai.data.ROBOFLOW_BASE_URL
import com.example.rimbaai.data.ROBOFLOW_MODEL_ID
import com.example.rimbaai.data.RetrofitClientRoboflow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class CameraScanViewModel : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var detectionResult by mutableStateOf<Prediction?>(null)
        private set

    var detectionError by mutableStateOf<String?>(null)
        private set

    private val roboflowApiService = RetrofitClientRoboflow.instance

    companion object {
        private const val TAG = "CameraScanViewModel"
    }

    fun resetResults() {
        detectionResult = null
        detectionError = null
        isLoading = false // Pastikan isLoading juga direset
    }

    // Metode untuk deteksi dari URI (tetap ada jika diperlukan, misal sebelum cropping)
    fun detectAnimalFromUri(context: Context, imageUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val bitmap = uriToBitmap(context, imageUri)
            if (bitmap != null) {
                performDetection(bitmap)
            } else {
                withContext(Dispatchers.Main) {
                    detectionError = "Gagal memuat gambar dari URI."
                    isLoading = false
                }
            }
        }
    }

    // Metode baru untuk deteksi dari Bitmap (setelah cropping)
    fun detectAnimalFromBitmap(imageBitmap: Bitmap) {
        performDetection(imageBitmap)
    }

    private fun performDetection(bitmap: Bitmap) {
        isLoading = true
        detectionError = null
        detectionResult = null

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val base64Image = bitmapToBase64(bitmap)
                if (base64Image.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        detectionError = "Gagal mengonversi gambar ke Base64."
                        isLoading = false
                    }
                    return@launch
                }
                Log.d(TAG, "Base64 image string length: ${base64Image.length}")

                val requestUrl = "${ROBOFLOW_BASE_URL}${ROBOFLOW_MODEL_ID}?api_key=${ROBOFLOW_API_KEY}"
                Log.d(TAG, "Request URL: $requestUrl")

                val response = roboflowApiService.detectObjects(requestUrl, base64Image)
                Log.d(TAG, "Roboflow Response: $response")

                withContext(Dispatchers.Main) {
                    if (response.predictions.isNotEmpty()) {
                        detectionResult = response.predictions.maxByOrNull { it.confidence }
                        if (detectionResult == null) {
                            detectionError = "Tidak ada prediksi yang valid ditemukan."
                        }
                    } else {
                        detectionError = "Tidak ada hewan terdeteksi."
                    }
                    isLoading = false
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error during detection: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    detectionError = "Error: ${e.message}"
                    isLoading = false
                }
            }
        }
    }


    private fun uriToBitmap(context: Context, imageUri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, imageUri))
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error converting URI to Bitmap", e)
            null
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        // Kompresi gambar, kualitas 80-90 biasanya cukup baik
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
}