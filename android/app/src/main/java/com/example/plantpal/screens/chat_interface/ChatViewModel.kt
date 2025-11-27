package com.example.plantpal.screens.chat_interface

import android.util.Log
import androidx.compose.animation.core.copy
import androidx.compose.ui.semantics.text
import com.example.plantpal.Screen
import com.example.plantpal.model.service.AccountService
import com.example.plantpal.screens.PlantPalAppViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import com.google.firebase.Firebase
import com.google.firebase.functions.functions
import com.google.firebase.auth.auth
import kotlinx.coroutines.delay


@HiltViewModel
class ChatViewModel @Inject constructor(
    val accountService: AccountService
): PlantPalAppViewModel() {
    private val _messages = MutableStateFlow(
        listOf(ChatMessage("Hello! How can I help you today?", Sender.AI))
    )
    val messages = _messages

    val userMessage = MutableStateFlow("")
    val isToggled = MutableStateFlow(false)
    val isChatting = MutableStateFlow(false)

    val currentMessageLength = MutableStateFlow(0)

    val chatThreadID = MutableStateFlow("")

    init {
        chatThreadID.value = UUID.randomUUID().toString()
    }

    fun updateUserMessage(newMessage: String) {
        userMessage.value = newMessage
    }

    suspend fun queryFB() {
        isChatting.value = true
        val incomingMessage = userMessage.value

        // Clear input box immediately
        updateUserMessage("")

        // 1. Add user message to chat. This will appear instantly.
        _messages.value += ChatMessage(incomingMessage, Sender.USER)

        delay(1500)

        // 2. Add a placeholder for the AI's response.
        val aiPlaceholder = ChatMessage("Thinking...", Sender.AI)
        _messages.value += aiPlaceholder

        // 3. Get the full response from the backend.
        val fullResponse: String = chat(incomingMessage, chatThreadID.value)

        // 4. Remove current text in placeholder
        val currentList = _messages.value
        val lastMessage = currentList.last()
        _messages.value = currentList.dropLast(1) + lastMessage.copy(text = "")

        // 5. Animate the full response into the placeholder and enable chat after animation is done.
        animateMessage(fullResponse)
        isChatting.value = false
    }

    /**
     * Replaces the last message (the AI placeholder) with a character-by-character animation.
     */
    private suspend fun animateMessage(fullText: String) {
        val characterDelay = 15L // milliseconds between each character

        // Build the text one character at a time
        fullText.forEach { char ->
            // Get the current list, drop the last message, and add an updated version
            val currentList = _messages.value
            val lastMessage = currentList.last()
            val updatedMessage = lastMessage.copy(text = lastMessage.text + char)

            currentMessageLength.value = updatedMessage.text.length

            _messages.value = currentList.dropLast(1) + updatedMessage
            delay(characterDelay)
        }
    }

    fun toggleSettings() {
        isToggled.value = !isToggled.value
    }

    fun resetChat() {
        // Clear previous chat logs
        _messages.value = listOf(ChatMessage("Hello! How can I help you today?", Sender.AI))
        userMessage.value = ""
        chatThreadID.value = UUID.randomUUID().toString()
    }

    fun logout(openAndPopUp: (String, String) -> Unit) {
        isToggled.value = false
        resetChat()
        accountService.signOut()
        openAndPopUp(Screen.SignInRoute.route, Screen.ChatRoute.route)
    }

    private suspend fun chat(message: String, chatThreadId: String? = "abc_123"): String {
        val data = hashMapOf(
            "message" to message,
            "thread_id" to chatThreadId
        )

        if (Firebase.auth.currentUser == null) {
            Log.d("ChatViewModel", "User is not signed in, using simulated response.")
            delay(1000) // Simulate network delay
            return "This is a simulated response because you are not signed in. The text will animate character by character."
        }

        try {
            // Call the function and await the result
            val result = Firebase.functions
                .getHttpsCallable("plantpal_chat")
                .call(data)
                .await() // This suspends the coroutine until the task is complete

            val resultMap = result.data as? Map<*, *>
            val success = resultMap?.get("success") as? Boolean ?: false

            return if (success) {
                Log.d("ChatViewModel", "Response from PlantPal: ${resultMap["response"]}")
                resultMap["response"] as? String ?: "No response received"
            } else {
                Log.d("ChatViewModel", "Error: Failed to get response from PlantPal")
                "Error: Failed to get response from PlantPal"
            }

        } catch (e: Exception) {
            return "Error: ${e.message}"
        }
    }
}
