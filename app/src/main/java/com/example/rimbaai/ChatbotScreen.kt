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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rimbaai.ui.theme.RimbaAITheme
import kotlinx.coroutines.launch

// enum class Sender dan data class ChatMessage tetap sama
enum class Sender {
    USER, BOT
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val sender: Sender,
    val timestamp: Long = System.currentTimeMillis()
)

// Fungsi helper untuk mem-parsing Markdown sederhana (bold)
fun parseMarkdownText(markdownText: String): AnnotatedString {
    val boldPattern = Regex("""\*\*(.*?)\*\*""") // Mencocokkan teks di antara **
    // Pola lain bisa ditambahkan di sini jika perlu (misal: *italic*)

    return buildAnnotatedString {
        var currentIndex = 0
        boldPattern.findAll(markdownText).forEach { matchResult ->
            val (startIndex, endIndex) = matchResult.range.first to matchResult.range.last
            val textInsideBold = matchResult.groupValues[1] // Teks di dalam **...**

            // Tambahkan teks sebelum bagian bold
            if (startIndex > currentIndex) {
                append(markdownText.substring(currentIndex, startIndex))
            }

            // Tambahkan teks bold
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append(textInsideBold)
            }
            currentIndex = endIndex + 1
        }

        // Tambahkan sisa teks setelah bagian bold terakhir
        if (currentIndex < markdownText.length) {
            append(markdownText.substring(currentIndex))
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChatbotScreen(
    onNavigateBack: () -> Unit,
    chatbotViewModel: ChatbotViewModel = viewModel()
) {
    val lightBackgroundColor = Color(0xFFF7F9FC)
    val accentColor = Color(0xFFFF9800)
    val textPrimaryColor = Color(0xFF273240)
    val userMessageColor = accentColor.copy(alpha = 0.15f)
    val botMessageColor = Color.White

    var inputText by remember { mutableStateOf("") }
    val messages = chatbotViewModel.messages
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val handleSendMessage = {
        if (inputText.isNotBlank()) {
            val currentMessageText = inputText
            chatbotViewModel.sendMessage(currentMessageText)
            inputText = ""
            keyboardController?.hide()
            focusManager.clearFocus()
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
                onSendMessage = handleSendMessage,
                isLoading = messages.lastOrNull()?.id == "typing_indicator",
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
            items(messages, key = { it.id }) { message ->
                ChatMessageBubble(
                    message = message,
                    userMessageColor = userMessageColor,
                    botMessageColor = botMessageColor,
                    textPrimaryColor = textPrimaryColor
                )
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
            // Gunakan fungsi parseMarkdownText di sini
            Text(
                text = parseMarkdownText(message.text), // <--- PERUBAHAN DI SINI
                color = textPrimaryColor, // Warna teks mungkin perlu disesuaikan jika teks bold memiliki warna berbeda
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

@Preview(showBackground = true)
@Composable
fun ChatMessageBubblePreview_BotBold() {
    RimbaAITheme {
        ChatMessageBubble(
            message = ChatMessage(text = "Ini adalah **teks tebal** dari bot.", sender = Sender.BOT),
            userMessageColor = Color.LightGray,
            botMessageColor = Color.White,
            textPrimaryColor = Color.Black
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ChatMessageBubblePreview_UserNoBold() {
    RimbaAITheme {
        ChatMessageBubble(
            message = ChatMessage(text = "Ini teks biasa dari user.", sender = Sender.USER),
            userMessageColor = Color.Cyan.copy(alpha=0.2f),
            botMessageColor = Color.White,
            textPrimaryColor = Color.Black
        )
    }
}