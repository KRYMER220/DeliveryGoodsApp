package ru.krymer.delivery.data.model

data class CourierInfoModel(
    val cash: Double,
    val noCash: Double,
    val percentSalary: Double,
    val allMoney: Double,
    val remainCash: Double,
    val salary: Double
)