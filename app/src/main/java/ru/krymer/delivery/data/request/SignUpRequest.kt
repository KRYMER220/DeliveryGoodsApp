package ru.krymer.delivery.data.request

data class SignUpRequest(
    val email: String,
    val password: String,
    val role: String,
    val idFactory: Long,
    val name: String,
    val status: String
)