package ru.krymer.delivery.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.krymer.delivery.common.EventHandler
import ru.krymer.delivery.data.TokenManager
import ru.krymer.delivery.data.model.user.MessageModel
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sharedViewModel: SharedViewModel,
    private val webSocketManager: WebSocketManager,
    private val tokenManager: TokenManager
) : ViewModel(), EventHandler<ChatEvent> {

    private val _viewState = MutableStateFlow(ChatViewState())
    val viewState: StateFlow<ChatViewState> = _viewState
    private val _chatMessages = MutableStateFlow<List<MessageModel>>(emptyList())
    val chatMessages: StateFlow<List<MessageModel>> = _chatMessages

    private fun updateViewState(update: (ChatViewState) -> ChatViewState) {
        _viewState.update { update(it) }
    }

    private fun launchCoroutine(block: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                block()
            } catch (e: Exception) {
                sharedViewModel.message(e.message)
            }
        }

    }

    init {
        observeMessages()
        launchCoroutine {
            connectToChat()
        }
    }

    private fun connectToChat() {
        val currentUser = sharedViewModel.viewState.value.user.value
        val token = tokenManager.getAccessToken()
        if (currentUser != null && token != null) webSocketManager.connect(currentUser.id, token)
    }

    private fun observeMessages() {
        viewModelScope.launch {
            webSocketManager.messages.collect { message ->
                _chatMessages.update { currentMessages ->
                    (currentMessages + message).sortedByDescending { it.date }
                }
            }
        }
    }

    fun sendMessage(text: String) {
        viewModelScope.launch {
            val currentUser = sharedViewModel.viewState.value.user.value
            if (currentUser != null) {
                val message = MessageModel(
                    date = System.currentTimeMillis(),
                    nameUser = currentUser.name,
                    message = text,
                    id = currentUser.id
                )
                webSocketManager.sendMessage(message)
            }
        }
    }

    override fun onCleared() {
        webSocketManager.disconnect()
        super.onCleared()
    }

    override fun obtainEvent(event: ChatEvent) {
        when (event) {
            is ChatEvent.SendMessage -> sendMessage(event.message)
        }
    }
}