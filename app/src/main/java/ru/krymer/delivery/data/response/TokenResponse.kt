package ru.krymer.delivery.data.response

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String? = null,
    val tokenType: String,
)
