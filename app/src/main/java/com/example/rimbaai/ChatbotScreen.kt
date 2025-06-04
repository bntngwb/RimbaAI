package com.example.rimbaai

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rimbaai.ui.theme.RimbaAITheme
import kotlinx.coroutines.delay // Import delay untuk simulasi
import kotlinx.coroutines.launch

enum class Sender {
    USER, BOT
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val sender: Sender,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChatbotScreen(
    onNavigateBack: () -> Unit
) {
    val lightBackgroundColor = Color(0xFFF7F9FC)
    val accentColor = Color(0xFFFF9800) // Oranye aksen
    val textPrimaryColor = Color(0xFF273240)
    val userMessageColor = accentColor.copy(alpha = 0.15f)
    val botMessageColor = Color.White // Atau Color(0xFFE8EAF6) untuk sedikit beda

    var inputText by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<ChatMessage>() } // Nama variabel kembali ke 'messages'
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var isLoadingResponse by remember { mutableStateOf(false) }

    // Tambahkan pesan sambutan dari bot jika messages kosong
    LaunchedEffect(Unit) {
        if (messages.isEmpty()) {
            messages.add(
                ChatMessage(
                    text = "Halo! Saya Rimba, asisten AI Anda. Ada yang bisa saya bantu terkait dunia satwa liar?",
                    sender = Sender.BOT
                )
            )
        }
    }

    val sendMessage = {
        if (inputText.isNotBlank()) {
            val userMessageText = inputText // Simpan teks sebelum dikosongkan
            val userMessage = ChatMessage(text = userMessageText, sender = Sender.USER)
            messages.add(userMessage)
            inputText = ""
            keyboardController?.hide()
            focusManager.clearFocus()

            isLoadingResponse = true // Tampilkan indikator loading

            coroutineScope.launch {
                if(messages.isNotEmpty()){
                    listState.animateScrollToItem(messages.size -1)
                }
            }

            // Kembali ke Simulasi respons dari Bot
            coroutineScope.launch {
                delay(1500) // Tunda untuk simulasi waktu respons jaringan
                val botResponseText = when {
                    userMessageText.lowercase().contains("harimau") -> "Harimau adalah kucing terbesar di dunia! Harimau Sumatera adalah subspesies yang hanya ditemukan di Pulau Sumatera, Indonesia, dan statusnya kritis."
                    userMessageText.lowercase().contains("makanan komodo") -> "Komodo adalah karnivora. Makanan utamanya adalah rusa, babi hutan, dan kerbau air. Mereka juga bisa memakan bangkai."
                    userMessageText.lowercase().contains("hello") || userMessageText.lowercase().contains("hai") -> "Hai juga! Ada yang ingin kamu ketahui tentang satwa?"
                    else -> "Maaf, saya belum bisa menjawab pertanyaan itu. Mungkin coba pertanyaan lain tentang satwa liar?"
                }
                messages.add(ChatMessage(text = botResponseText, sender = Sender.BOT))
                isLoadingResponse = false // Sembunyikan indikator loading
                if(messages.isNotEmpty()){
                    listState.animateScrollToItem(messages.size -1)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Tanya Rimba",
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
        containerColor = lightBackgroundColor,
        bottomBar = {
            MessageInputBar(
                inputText = inputText,
                onTextChange = { inputText = it },
                onSendMessage = sendMessage,
                isLoading = isLoadingResponse,
                accentColor = accentColor
            )
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages, key = { it.id }) { message -> // Menggunakan 'messages'
                ChatMessageBubble(
                    message = message,
                    userMessageColor = userMessageColor,
                    botMessageColor = botMessageColor,
                    textPrimaryColor = textPrimaryColor
                )
            }
            if (isLoadingResponse) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = accentColor, strokeWidth = 3.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageBubble(
    message: ChatMessage,
    userMessageColor: Color,
    botMessageColor: Color,
    textPrimaryColor: Color
) {
    val alignment = if (message.sender == Sender.USER) Alignment.CenterEnd else Alignment.CenterStart
    val backgroundColor = if (message.sender == Sender.USER) userMessageColor else botMessageColor
    val shape = if (message.sender == Sender.USER) {
        RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .clip(shape)
                .background(backgroundColor)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = message.text,
                color = textPrimaryColor,
                fontSize = 15.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageInputBar(
    inputText: String,
    onTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    isLoading: Boolean,
    accentColor: Color
) {
    Surface(
        tonalElevation = 4.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ketik pesanmu...") },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor.copy(alpha = 0.7f),
                    unfocusedBorderColor = Color.LightGray,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = { if (!isLoading) onSendMessage() }
                ),
                maxLines = 4
            )
            IconButton(
                onClick = { if (!isLoading) onSendMessage() },
                enabled = inputText.isNotBlank() && !isLoading,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (inputText.isNotBlank() && !isLoading) accentColor else Color.Gray.copy(alpha = 0.5f),
                    contentColor = Color.White
                ),
                modifier = Modifier.size(48.dp)
            ) {
                AnimatedVisibility(
                    visible = !isLoading,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Kirim Pesan"
                    )
                }
                AnimatedVisibility(
                    visible = isLoading,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true, device = "id:pixel_5")
@Composable
fun ChatbotScreenPreview() {
    RimbaAITheme {
        ChatbotScreen(onNavigateBack = {})
    }
}

// Preview lainnya bisa Anda tambahkan kembali jika diperlukan
