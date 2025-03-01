package ru.krymer.delivery.data.response

data class BaseResponse<T>(
    val message: String,
    val success: Boolean,
    val obj: T? = null
)
