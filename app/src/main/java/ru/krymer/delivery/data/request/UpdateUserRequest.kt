package ru.krymer.delivery.data.request

data class UpdateUserRequest(
    val id: Long,
    val login: String,
    val name: String,
    val phone: String,
    val status: String,
    val role: String,
    val isBanned: Boolean,
    val percentSalary: Double,
    val salary: Double
)