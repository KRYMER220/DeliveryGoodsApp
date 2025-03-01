package ru.krymer.delivery.data.request

data class UpdateShopRequest(
    val id: Long,
    val idTrip: Long,
    val idFactory: Long,
    val arrears: Double,
    val addSum: Double,
    val status: Boolean,
    val date: Long,
    val typePay: String,
    val cash: Double,
    val counter: Int,
    val noCash: Double,
    val isOldPrice: Boolean,
    val nameShop: String,
    val cord: String,
)
