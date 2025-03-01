package ru.krymer.delivery.data.model

data class TripModel(
    val id: Long,
    val idFactory: Long,
    val date: Long,
    val salary: Double,
    val percentCourier: Double,
    val salaryCourier: Double,
    val priceMillage: Double,
    val millage: Double,
    val idCourier: Long,
    val nameCourier: String,
    val nameRoute: String,
    val idRoute: Long
)