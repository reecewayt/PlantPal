package com.example.plantpal.screens.chat_interface

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plantpal.ui.theme.PlantPalTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID


// 1. Data model for a single message
data class ChatMessage(
    val text: String,
    val sender: Sender,
    val id: String = UUID.randomUUID().toString()
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
    openAndPopUp: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel
) {
    val messages by viewModel.messages.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()
    val isToggled by viewModel.isToggled.collectAsState()
    val chatThreadID by viewModel.chatThreadID.collectAsState()
    val isChatting by viewModel.isChatting.collectAsState()
    val currentMessageLength by viewModel.currentMessageLength.collectAsState()



    ChatInterfaceContent(
        modifier = modifier,
        messages = messages,
        userMessage = userMessage,
        isToggled = isToggled,
        chatThreadID = chatThreadID,
        isChatting = isChatting,
        currentMessageLength = currentMessageLength,
        updateUserMessage = viewModel::updateUserMessage,
        queryFB = viewModel::queryFB,
        toggleSettings = viewModel::toggleSettings,
        resetChat = viewModel::resetChat,
        logout = { viewModel.logout(openAndPopUp) }
    )
}

// 2. The main Chat Screen Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class) // <-- Add ExperimentalFoundationApi
@Composable
fun ChatInterfaceContent(
    modifier: Modifier = Modifier,
    messages: List<ChatMessage>,
    userMessage: String,
    isToggled: Boolean,
    chatThreadID: String,
    isChatting: Boolean,
    currentMessageLength: Int,
    updateUserMessage: (String) -> Unit,
    queryFB: suspend () -> Unit,
    toggleSettings: () -> Unit,
    resetChat: () -> Unit,
    logout: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    LaunchedEffect(currentMessageLength) {
        lazyListState.scrollToItem(messages.size+1)
    }   

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
                                imageVector = if (isToggled) Icons.Filled.Settings else Icons.Outlined.Settings,
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
                    },
                    enabled = !isChatting
                )
            }
        ) { innerPadding ->
            // The list of messages
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Use items with a key for better performance and animation
                items(
                    items = messages,
                    key = { message -> message.id } // A stable key is important
                ) { message ->
                    Column(
                        horizontalAlignment = if (message.sender == Sender.USER) Alignment.End else Alignment.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp)
                    ) {
                        val density = LocalDensity.current
                        var visible by remember { mutableStateOf(false) }

                        LaunchedEffect(key1 = message.id) {
                            // This will be triggered for each new message, making it visible.
                            visible = true
                        }

                        AnimatedVisibility(
                            visible = visible,
                            enter = slideInHorizontally {
                                with(density) { if (message.sender == Sender.USER) 120.dp.roundToPx() else -120.dp.roundToPx() }
                            } + expandVertically(
                                expandFrom = Alignment.Bottom
                            ) + fadeIn(
                                initialAlpha = 0.3f
                            ),
                            exit = slideOutHorizontally() + shrinkVertically() + fadeOut()
                        ) {
                            MessageBubble(
                                // The MessageBubble no longer needs any special modifiers.
                                message = message
                            )
                        }
                    }
                }

                item(key = "anchor") {
                    Spacer(modifier = Modifier.size(1.dp))
                }
            }
        }

        if (isToggled) {
            ModalBottomSheet(
                // Intentionally removed fillMaxSize to make it a normal sheet
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
                        .padding(10.dp)
                        .fillMaxWidth(),
                ) {
                    Text(
                        text = "Reset Chat",
                        fontSize = 16.sp
                    )
                }
                // Logout Button
                Button(
                    onClick = {
                        logout()
                    },
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth(),
                ) {
                    Text(
                        text ="Logout",
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

// ... (Rest of the file remains the same)


// 3. The input field and send button at the bottom
@Composable
fun UserInputBar(
    modifier: Modifier,
    currentText: String,
    onTextChanged: (String) -> Unit,
    onSendClicked: () -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = modifier
            .padding(horizontal = 20.dp, vertical = 15.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = currentText,
            onValueChange = onTextChanged,
            label = { Text("Type a message...") },
            modifier = Modifier.weight(1f),
            enabled = enabled
        )
        IconButton(
            onClick = onSendClicked,
            enabled = enabled
        ) {
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
    message: ChatMessage
) {
    val isUserMessage = message.sender == Sender.USER

    Card(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            // ✨ 2. APPLY THE MODIFIER HERE ✨
            // This will smoothly animate the card's size as the text inside grows.
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        shape = RoundedCornerShape(16.dp),
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
            modifier = Modifier.padding(20.dp),
            fontSize = 18.sp
        )
    }
}


// 5. Preview function to see our screen in Android Studio
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ChatInterfacePreview() {
    PlantPalTheme(dynamicColor = false) {
        ChatInterfaceContent(
            messages = listOf(
                ChatMessage("Test", Sender.AI),
                ChatMessage("Hello", Sender.USER),
                ChatMessage("Test", Sender.AI),
                ChatMessage("Hello", Sender.USER),
                ChatMessage("Test", Sender.AI),
                ChatMessage("Hello", Sender.USER)
            ),
            userMessage = "",
            isToggled = false,
            chatThreadID = "Test123",
            isChatting = false,
            currentMessageLength = 0,
            updateUserMessage = {},
            queryFB = {},
            toggleSettings = {},
            resetChat = {},
            logout = {}
        )
    }
}
