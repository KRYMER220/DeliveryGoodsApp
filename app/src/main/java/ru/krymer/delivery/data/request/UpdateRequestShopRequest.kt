package ru.krymer.delivery.data.request

data class UpdateRequestShopRequest(
    val id: Long,
    val idShop: Long,
    val idTrip: Long,
    val idFactory: Long,
    val count: Int,
    val exchange: Int,
    val bonus: Int,
    val status: Boolean,
    val price: Double,
    val oldPrice: Double,
    val name: String,
    val counter: Int,
)
