package com.example.plantpal.screens.chat_interface

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.plantpal.ui.theme.PlantPalTheme
import kotlinx.coroutines.launch


// 1. Data model for a single message
data class ChatMessage(
    val text: String,
    val sender: Sender
) {
    init {
        require(sender  == Sender.USER || sender == Sender.AI) {"Sender must be USER or AI"}
    }
}

enum class Sender {
    USER, AI
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ChatInterfaceScreen(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()
    val isToggled by viewModel.isToggled.collectAsState()
    val chatThreadID by viewModel.chatThreadID.collectAsState()

    ChatInterfaceContent(
        modifier = modifier,
        messages = messages,
        userMessage = userMessage,
        isToggled = isToggled,
        chatThreadID = chatThreadID,
        updateUserMessage = viewModel::updateUserMessage,
        queryFB = viewModel::queryFB,
        toggleSettings = viewModel::toggleSettings,
        resetChat = viewModel::resetChat,
        logout = viewModel::logout
    )
}

// 2. The main Chat Screen Composable
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ChatInterfaceContent(
    modifier: Modifier = Modifier,
    messages: List<ChatMessage>,
    userMessage: String,
    isToggled: Boolean,
    chatThreadID: String,
    updateUserMessage: (String) -> Unit,
    queryFB: suspend () -> Unit,
    toggleSettings: () -> Unit,
    resetChat: () -> Unit,
    logout: suspend () -> Unit
) {
    val scope = rememberCoroutineScope()

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Plant Pal")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    actions = {
                        IconButton(onClick = { toggleSettings() }) {
                            Icon(
                                // Suggestion 4: Provide visual feedback for the toggled state
                                imageVector = if (isToggled) {
                                    Icons.Filled.Settings
                                } else {
                                    Icons.Outlined.Settings
                                },
                                contentDescription = "Toggle settings",
                                modifier = Modifier.size(24.dp),
                                tint = if (isToggled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                )
            },
            bottomBar = {
                UserInputBar(
                    modifier = Modifier,
                    currentText = userMessage,
                    onTextChanged = { updateUserMessage(it) },
                    onSendClicked = {
                        if (userMessage.isNotBlank()) {
                            scope.launch {
                                queryFB()
                            }
                        }
                    }
                )
            }
        ) { innerPadding ->
            // The list of messages
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                reverseLayout = true // Important for chat UIs
            ) {
                items(messages.reversed()) { message ->
                    MessageBubble(
                        modifier = Modifier,
                        message = message
                    )
                }
            }
        }

        if (isToggled) {
            ModalBottomSheet(
                modifier = Modifier.fillMaxSize(),
                onDismissRequest = { toggleSettings() }
            ) {
                // Chat ID Display
                Text(
                    text = "Chat Thread ID: $chatThreadID",
                    modifier = Modifier
                        .padding(14.dp)
                        .fillMaxWidth()
                )
                // Reset Chat
                Button(
                    onClick = {
                        resetChat()
                        toggleSettings()
                    },
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                ) {
                    Text("Reset Chat")
                }
                // Logout Button
                Button(
                    onClick = {
                        scope.launch{
                            logout()
                        }
                    },
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                ) {
                    Text("Logout")
                }
            }
        }
    }
}

// 3. The input field and send button at the bottom
@Composable
fun UserInputBar(
    modifier: Modifier,
    currentText: String,
    onTextChanged: (String) -> Unit,
    onSendClicked: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = currentText,
            onValueChange = onTextChanged,
            label = { Text("Type a message...") },
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onSendClicked) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = "Send message",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// 4. The composable for a single chat bubble
@Composable
fun MessageBubble(
    modifier: Modifier,
    message: ChatMessage
) {
    val isUserMessage = message.sender == Sender.USER

    // Use a Box to align the message bubble to the start or end
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = if (isUserMessage) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.8f) // Don't let the bubble take the full width
                .clip(RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(
                containerColor = if (isUserMessage) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.secondaryContainer
                }
            )
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(16.dp),
                fontSize = 16.sp
            )
        }
    }
}


// 5. Preview function to see our screen in Android Studio
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ChatInterfacePreview() {
    PlantPalTheme(dynamicColor = false) {
        ChatInterfaceContent(
            messages = listOf(ChatMessage("Test", Sender.AI)),
            userMessage = "",
            isToggled = false,
            chatThreadID = "Test123",
            updateUserMessage = {},
            queryFB = {},
            toggleSettings = {},
            resetChat = {},
            logout = {}
        )
    }
}
