package com.example.rimbaai

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rimbaai.data.AzureChatMessage
import com.example.rimbaai.data.ChatCompletionRequest
import com.example.rimbaai.data.RetrofitClient
import kotlinx.coroutines.launch

// Data class ChatMessage untuk UI tetap sama seperti di ChatbotScreen.kt
// enum class Sender { USER, BOT }
// data class ChatMessage(
//    val id: String = java.util.UUID.randomUUID().toString(),
//    val text: String,
//    val sender: Sender,
//    val timestamp: Long = System.currentTimeMillis()
// )

class ChatbotViewModel : ViewModel() {
    private val _messages = mutableStateListOf<ChatMessage>()
    val messages: List<ChatMessage> = _messages

    private val azureOpenAIService = RetrofitClient.instance
    private val conversationHistory = mutableListOf<AzureChatMessage>()

    companion object {
        private const val TAG = "ChatbotViewModel"
    }

    init {
        // Tambahkan pesan sambutan awal dari bot
        if (_messages.isEmpty()) {
            _messages.add(
                ChatMessage(
                    text = "Halo! Saya Rimba, asisten AI Anda. Ada yang bisa saya bantu terkait dunia satwa liar?",
                    sender = Sender.BOT
                )
            )
            // Tambahkan juga ke histori untuk konteks awal jika diperlukan
            conversationHistory.add(AzureChatMessage(role = "assistant", content = _messages.first().text))
        }
    }


    fun sendMessage(userMessageText: String) {
        if (userMessageText.isBlank()) return

        // Tambahkan pesan pengguna ke UI dan histori
        val userUiMessage = ChatMessage(text = userMessageText, sender = Sender.USER)
        _messages.add(userUiMessage)
        conversationHistory.add(AzureChatMessage(role = "user", content = userMessageText))

        // Tampilkan "Typing..." dari bot
        val typingMessage = ChatMessage(text = "Rimba sedang mengetik...", sender = Sender.BOT, id = "typing_indicator")
        _messages.add(typingMessage)

        viewModelScope.launch {
            try {
                val azureDeploymentName = BuildConfig.AZURE_OPENAI_DEPLOYMENT_NAME
                // Ganti dengan versi API yang sesuai, contoh "2024-02-01" atau "2024-03-01-preview"
                // Sebaiknya ini juga dari BuildConfig atau konstanta
                val apiVersion = "2024-02-15-preview" // Pastikan versi API ini benar dan didukung endpoint Anda
                // Dari kode ChatActivity Anda sebelumnya "2024-12-01-preview"
                // saya akan pakai itu:
                // val apiVersion = "2024-12-01-preview" // Sesuaikan!

                val relativeUrl = "openai/deployments/${azureDeploymentName}/chat/completions?api-version=${apiVersion}"

                // Untuk menjaga konteks, kirim beberapa pesan terakhir dari histori
                // Batasi jumlah pesan histori untuk menghindari request body yang terlalu besar
                val contextMessages = conversationHistory.takeLast(10) // Ambil 10 pesan terakhir sebagai konteks

                val request = ChatCompletionRequest(messages = contextMessages)
                Log.d(TAG, "Mengirim request ke Azure: $request dengan URL: $relativeUrl")

                val response = azureOpenAIService.getChatCompletion(relativeUrl, request)
                Log.d(TAG, "Menerima respons dari Azure: $response")


                // Hapus indikator "Typing..."
                _messages.removeIf { it.id == "typing_indicator" }

                response.choices.firstOrNull()?.message?.content?.let { azureResponseContent ->
                    if (azureResponseContent.isNotBlank()) {
                        _messages.add(ChatMessage(text = azureResponseContent, sender = Sender.BOT))
                        conversationHistory.add(AzureChatMessage(role = "assistant", content = azureResponseContent))
                    } else {
                        Log.e(TAG, "Azure OpenAI response content is blank.")
                        _messages.add(ChatMessage(text = "Maaf, saya tidak dapat memberikan respons saat ini (konten kosong).", sender = Sender.BOT))
                    }
                } ?: run {
                    Log.e(TAG, "Azure OpenAI response content is null or choices empty. Response: $response")
                    _messages.add(ChatMessage(text = "Maaf, terjadi kesalahan saat mengambil respons (pilihan kosong).", sender = Sender.BOT))
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error sending message to Azure OpenAI: ${e.message}", e)
                // Hapus indikator "Typing..." jika ada error
                _messages.removeIf { it.id == "typing_indicator" }
                _messages.add(ChatMessage(text = "Maaf, terjadi masalah koneksi: ${e.message}", sender = Sender.BOT))
            }
        }
    }
}