package com.example.plantpal.screens.chat_interface

import androidx.compose.runtime.getValue
import com.example.plantpal.screens.PlantPalAppViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor()
: PlantPalAppViewModel() {
    // State management: these 'remember' calls hold the state of our chat
    val messages = MutableStateFlow(
        listOf(ChatMessage("Hello! How can I help you today?", Sender.AI))
    )
    val userMessage = MutableStateFlow("")
    val isToggled = MutableStateFlow(false)

    fun updateUserMessage(newMessage: String) {
        userMessage.value = newMessage
    }

    fun queryFB() {
        addMessage(ChatMessage(userMessage.value, Sender.USER))
        addMessage(ChatMessage("Thinking...", Sender.AI))
        //TODO: Add Firebase API call here (if returns error, return appropriate error string)

        userMessage.value = ""
    }

    fun toggleSettings() {
        isToggled.value = !isToggled.value
    }

    fun resetChat() {
        // Clear previous chat logs
        messages.value = listOf(ChatMessage("Hello! How can I help you today?", Sender.AI))
    }

    private fun addMessage(message: ChatMessage) {
        messages.value += message
    }

    private fun deleteMessage(message: ChatMessage) {
        messages.value -= message
    }
}


