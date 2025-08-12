package ru.krymer.delivery.data.model

data class LogShopModel(
    val id: Long,
    val idFactory: Long,
    val idCourier: Long,
    val nameCourier: String,
    val idTrip: Long,
    val idShop: Long,
    val date: Long,
    val log: String
)