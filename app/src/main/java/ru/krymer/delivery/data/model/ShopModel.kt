package ru.krymer.delivery.data.model

import ru.krymer.delivery.data.model.utilModel.TypePayModel

data class ShopModel(
    val id: Long,
    val idTrip: Long,
    val idFactory: Long,
    val nameShop: String,
    val arrears: Double,
    val addSum: Double,
    val status: Boolean,
    val date: Long,
    val typePay: TypePayModel,
    val cash: Double,
    val counter: Int,
    val noCash: Double,
    val listRequest: List<RequestModel>? = null,
    val isOldPrice: Boolean,
    val cord: String,
)