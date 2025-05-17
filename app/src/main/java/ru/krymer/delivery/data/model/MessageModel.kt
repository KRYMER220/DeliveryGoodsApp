package ru.krymer.delivery.data.model

data class MessageModel(
    val id: Long,
    val idClient: Long,
    val text: String,
    val date: Long
)
