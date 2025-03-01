package ru.krymer.delivery.data.request

data class CreateFactoryRequest(
    val name: String,
    val dateAdd: Long?,
    val salary: Double,
    val priceMillage: Double,
)