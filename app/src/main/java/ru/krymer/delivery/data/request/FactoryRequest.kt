package ru.krymer.delivery.data.request

data class FactoryRequest(
    val id: Long? = null,
    val name: String,
    val dateAdd: Long,
    val salary: Double,
    val priceMillage: Double,
)
