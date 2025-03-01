package ru.krymer.delivery.data.request

data class SignInRequest(
    val email: String,
    val password: String,
)