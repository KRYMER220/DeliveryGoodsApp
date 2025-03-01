package ru.krymer.delivery.data.response

import ru.krymer.delivery.data.model.RequestModel

data class Factory(
    val millage: Double = 0.0,
    val salary: Double = 0.0,
    val cash: Double = 0.0,
    val noCash: Double = 0.0,
    val money: Double = 0.0,
    val percentSalary: Double = 0.0,
    val otherProductMoney: Double = 0.0,
    val productPrice: Double = 0.0,
    val productExchangePrice: Double = 0.0,
    val requests: List<RequestModel> = listOf(),
    val bars: List<BarsModel> = listOf(),
    var perSumExchange: Double = 0.0,
    var exchange: Int = 0
)

data class BarsModel(
    val title: String,
    var value: Double,
    val day: String
)

data class Client(
    var cash: Double = 0.0,
    var noCash: Double = 0.0,
    var money: Double = 0.0,
    var perSumExchange: Double = 0.0,
    var exchange: Int = 0,
    var otherProductMoney: Double = 0.0,
    var requests: List<RequestModel> = listOf(),
    var bars: List<BarsModel> = listOf(),
)

data class Trip(
    var cash: Double = 0.0,
    var noCash: Double = 0.0,
    var money: Double = 0.0,
    var perSumExchange: Double = 0.0,
    var exchange: Int = 0,
    var otherProductMoney: Double = 0.0,
    var requests: List<RequestModel> = listOf(),
    var bars: List<BarsModel> = listOf(),
)