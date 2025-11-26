package com.example.plantpal.screens.chat_interface

import android.util.Log
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


@HiltViewModel
class ChatViewModel @Inject constructor(
    val accountService: AccountService
)
: PlantPalAppViewModel() {
    // State management: these 'remember' calls hold the state of our chat
    val messages = MutableStateFlow(
        listOf(ChatMessage("Hello! How can I help you today?", Sender.AI))
    )
    val userMessage = MutableStateFlow("")
    val isToggled = MutableStateFlow(false)

    val chatThreadID = MutableStateFlow("")

    init {
        chatThreadID.value = UUID.randomUUID().toString()
    }

    fun updateUserMessage(newMessage: String) {
        userMessage.value = newMessage
    }

    suspend fun queryFB() {
        val waitMessage = ChatMessage("Thinking...", Sender.AI)
        val incomingMessage = userMessage.value

        // Clear input box when message is sent
        updateUserMessage("")

        // Add user message to chat
        addMessage(ChatMessage(incomingMessage, Sender.USER))
        addMessage(waitMessage)

        // Add AI message to chat
        val outputMessage: String = chat(incomingMessage, chatThreadID.value)

        // Delete wait message and add outputMessage
        deleteMessage(waitMessage)
        addMessage(ChatMessage(outputMessage, Sender.AI))
    }

    fun toggleSettings() {
        isToggled.value = !isToggled.value
    }

    fun resetChat() {
        // Clear previous chat logs
        messages.value = listOf(ChatMessage("Hello! How can I help you today?", Sender.AI))
        userMessage.value = ""
        chatThreadID.value = UUID.randomUUID().toString()
    }

    fun logout() {
        isToggled.value = false
        resetChat()
        accountService.signOut()
    }

    private fun addMessage(message: ChatMessage) {
        messages.value += message
    }

    private fun deleteMessage(message: ChatMessage) {
        messages.value -= message
    }

    private suspend fun chat(message: String, chatThreadId: String? = "abc_123"): String {
        val data = hashMapOf(
            "message" to message,
            "thread_id" to chatThreadId
        )

        if (Firebase.auth.currentUser != null) {
            Log.d("ChatViewModel", "User ID: ${Firebase.auth.currentUser?.uid}")
            Log.d("ChatViewModel", "User email: ${Firebase.auth.currentUser?.email}")
            Log.d("ChatViewModel", "User details: ${Firebase.auth.currentUser?.metadata}")
        } else {
            Log.d("ChatViewModel", "User is not signed in")
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



