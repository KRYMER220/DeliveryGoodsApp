package ru.krymer.delivery.data.request


data class LogRequest(
    val id: Long? = null,
    val idFactory: Long,
    val date: Long,
    val log: String,
)
