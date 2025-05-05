package ru.krymer.delivery.ui.screens.chat

import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.MutableStateFlow
import ru.krymer.delivery.data.model.user.MessageModel

data class ChatViewState(
    val chat: MutableStateFlow<List<MessageModel>> = MutableStateFlow(listOf())
)
