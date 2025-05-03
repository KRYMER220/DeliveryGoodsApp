package ru.krymer.delivery.data.request

import ru.krymer.delivery.data.model.utilModel.TypePayModel
import ru.krymer.delivery.data.model.utilModel.getStringByTypePay

data class CreateShopRequest(
    val id: Long,
    val idTrip: Long,
    val idFactory: Long,
    val arrears: Double,
    val addSum: Double = 0.0,
    val status: Boolean = false,
    val date: Long,
    val typePay: String = TypePayModel.CASH.getStringByTypePay(),
    val cash: Double = 0.0,
    val counter: Int,
    val noCash: Double = 0.0,
    val isOldPrice: Boolean,
    val nameShop: String,
    val cord: String
)
