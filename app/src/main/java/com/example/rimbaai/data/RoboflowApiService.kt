package com.example.rimbaai.data

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Url
import java.util.concurrent.TimeUnit

// --- PERUBAHAN PADA KONSTANTA ---
// Konstanta dari Roboflow API yang BARU
const val ROBOFLOW_API_KEY = "WbLiZbHUlbYq0d9joo3t"
const val ROBOFLOW_BASE_URL = "https://serverless.roboflow.com/"
const val ROBOFLOW_MODEL_ID = "animal-object-detection-ik1kk-o3k4a/2"
const val ROBOFLOW_INFERENCE_SIZE = 640.0 // Ini mungkin tidak lagi digunakan secara eksplisit,
// tapi kita biarkan saja jika ada logika lain yang bergantung padanya.

interface RoboflowApiService {
    @POST
    // Menggunakan application/x-www-form-urlencoded seperti sebelumnya.
    @Headers("Content-Type: application/x-www-form-urlencoded")
    suspend fun detectObjects(@Url url: String, @Body imageBase64: String): RoboflowResponse
}

// Data Class untuk Response API Roboflow (Struktur ini seharusnya tetap sama)
data class RoboflowResponse(
    @SerializedName("predictions") val predictions: List<Prediction>
)

data class Prediction(
    @SerializedName("x") val x: Double,
    @SerializedName("y") val y: Double,
    @SerializedName("width") val width: Double,
    @SerializedName("height") val height: Double,
    @SerializedName("confidence") val confidence: Double,
    @SerializedName("class") val `class`: String
)

// Object RetrofitClientRoboflow (Tidak ada perubahan di sini)
object RetrofitClientRoboflow {
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val instance: RoboflowApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(ROBOFLOW_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
        retrofit.create(RoboflowApiService::class.java)
    }
}