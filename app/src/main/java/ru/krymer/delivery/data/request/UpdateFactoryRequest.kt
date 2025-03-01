package ru.krymer.delivery.data.request

data class UpdateFactoryRequest(
    val id: Long,
    val name: String,
    val dateAdd: Long,
    val salary: Double,
    val priceMillage: Double,
)
