package ru.krymer.delivery.data.response

data class Analitic(
    val millage: Double = 0.0,
    val salary: Double = 0.0,
    val cash: Double = 0.0,
    val noCash: Double = 0.0,
    val money: Double = 0.0,
    val percentSalary: Double = 0.0,
    val otherProductMoney: Double = 0.0,
    val productPrice: Double = 0.0,
    val productExchangePrice: Double = 0.0,
    val requests: List<AggregatedRequest> = listOf(),
    val bars: List<BarsModel> = listOf(),
    var perSumExchange: Double = 0.0,
    var exchange: Int = 0
)

data class BarsModel(
    val title: String,
    var value: Double,
    val day: String
)


data class AggregatedRequest(
    val id: Long,
    val name: String,
    val totalCount: Int,
    val totalBonus: Int,
    val totalExchange: Int
)