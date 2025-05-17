package ru.krymer.delivery.data.request

data class CreateMessage(
    val idClient: Long,
    val text: String,
    val date: Long
)
