package com.example.rimbaai.data // atau package network Anda

import com.example.rimbaai.BuildConfig // Untuk mengakses API KEY
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

// Retrofit Interface untuk komunikasi dengan Azure OpenAI Service
interface AzureOpenAIService {
    @Headers(
        "Content-Type: application/json",
        // Mengambil API Key dari BuildConfig yang sudah diset di build.gradle
        "api-key: ${BuildConfig.AZURE_OPENAI_API_KEY}"
    )
    @POST
    suspend fun getChatCompletion(@Url url: String, @Body request: ChatCompletionRequest): ChatCompletionResponse
}

// Data Class untuk Request Body Azure OpenAI (Chat Completions)
// Menggunakan AzureChatMessage untuk menghindari konflik nama dengan ChatMessage di UI
data class ChatCompletionRequest(
    @SerializedName("messages") val messages: List<AzureChatMessage>,
    @SerializedName("max_tokens") val maxTokens: Int = 800,
    @SerializedName("temperature") val temperature: Double = 0.7,
    @SerializedName("frequency_penalty") val frequencyPenalty: Double = 0.0,
    @SerializedName("presence_penalty") val presencePenalty: Double = 0.0
    // Anda bisa menambahkan parameter lain yang didukung Azure seperti "dataSources", dll. jika perlu
)

data class AzureChatMessage( // Diubah dari ChatMessage di ChatActivity.kt
    @SerializedName("role") val role: String, // "user", "system", "assistant"
    @SerializedName("content") val content: String
)

// Data Class untuk Response Body Azure OpenAI (Chat Completions)
data class ChatCompletionResponse(
    @SerializedName("id") val id: String?,
    @SerializedName("object") val obj: String?,
    @SerializedName("created") val created: Long?,
    @SerializedName("model") val model: String?,
    @SerializedName("prompt_filter_results") val promptFilterResults: List<PromptFilterResult>?,
    @SerializedName("choices") val choices: List<Choice>,
    @SerializedName("usage") val usage: Usage?,
    @SerializedName("system_fingerprint") val systemFingerprint: String?
)

data class Choice(
    @SerializedName("index") val index: Int?,
    @SerializedName("finish_reason") val finishReason: String?,
    @SerializedName("message") val message: AzureChatMessage?, // Menggunakan AzureChatMessage
    @SerializedName("content_filter_results") val contentFilterResults: ContentFilterResults?
)

data class Usage(
    @SerializedName("completion_tokens") val completionTokens: Int?,
    @SerializedName("prompt_tokens") val promptTokens: Int?,
    @SerializedName("total_tokens") val totalTokens: Int?
)

data class ContentFilterResults(
    @SerializedName("hate") val hate: FilterResult?,
    @SerializedName("self_harm") val selfHarm: FilterResult?,
    @SerializedName("sexual") val sexual: FilterResult?,
    @SerializedName("violence") val violence: FilterResult?
)

data class FilterResult(
    @SerializedName("filtered") val filtered: Boolean?,
    @SerializedName("severity") val severity: String?
)

data class PromptFilterResult(
    @SerializedName("prompt_index") val promptIndex: Int?,
    @SerializedName("content_filter_results") val contentFilterResults: ContentFilterResults?
)

// Object untuk membuat instance Retrofit (Singleton direkomendasikan)
object RetrofitClient {
    private val azureBaseUrl = BuildConfig.AZURE_OPENAI_BASE_URL

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Level BODY untuk debug, bisa diubah ke NONE untuk release
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val instance: AzureOpenAIService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(azureBaseUrl) // Pastikan BASE_URL diakhiri dengan "/"
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
        retrofit.create(AzureOpenAIService::class.java)
    }
}