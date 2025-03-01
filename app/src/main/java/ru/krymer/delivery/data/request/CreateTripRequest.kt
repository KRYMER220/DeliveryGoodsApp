package ru.krymer.delivery.data.request


data class CreateTripRequest(
    val id: Long? = null,
    val factoryId: Long,
    val date: Long,
    val courierId: Long,
    val routeId: Long,
    val salary: Double,
    val percentCourier: Double,
    val priceMillage: Double,
    val nameCourier: String,
    val nameRoute: String,
    val salaryCourier: Double ?= null,
    val millage: Double ?= null,
)
