package ru.krymer.delivery.ui.screens.chat

import android.os.Message

sealed class ChatEvent {
    data class SendMessage(val message: String) : ChatEvent()
}