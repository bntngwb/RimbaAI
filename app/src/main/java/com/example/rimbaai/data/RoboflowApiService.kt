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

// Konstanta dari MainActivity.kt
const val ROBOFLOW_API_KEY = "0IN6Y4AvxRGsjdDj2aRe"
const val ROBOFLOW_BASE_URL = "https://serverless.roboflow.com/"
const val ROBOFLOW_MODEL_ID = "animal-detection-jvsw5/1"
const val ROBOFLOW_INFERENCE_SIZE = 640.0

interface RoboflowApiService {
    @POST
    // --- PERUBAHAN DI SINI ---
    @Headers("Content-Type: application/x-www-form-urlencoded") // Dikembalikan sesuai MainActivity.kt Anda
    suspend fun detectObjects(@Url url: String, @Body imageBase64: String): RoboflowResponse
}

// Data Class untuk Response API Roboflow (tetap sama)
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

// Object RetrofitClientRoboflow (tetap sama)
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