package ru.krymer.delivery.data.model.oldModel

data class TripOld(
    val id: String = "",
    val date: Long = 0,
    val courier: String = "",
    val idCourier: String = "",
    val idRoute: String = "",
    val name: String = "",
    var salary: Double = 0.0,
    val percent: Double = 0.0,
    val priceSalary: Int = 0,
    val priceKM: Double = 0.0,
    val millage: Double = 0.0
)

data class RouteOld(
    val name: String = "",
    val id: String = "",
    val date: Long = 0,
)

data class ShopOld(
    val id: String = "",
    val name: String = "",
    val cords: String = "",
    var i: Long = 0,
    var arrears: Int = 0,
    val date: Long = 0,
    val phone: String = "",
    val status: Boolean = false,
    val idRoute: String = "",
    var addSum: Int = 0,
    var ready: Boolean = false,
    var cash: Int = 0,
    var nocash: Int = 0,
    var typePay: Boolean = false,
    var debt: Int = 0,
    var b: Boolean = false,
)

data class ProductOld(
    val id: String = "",
    val name: String = "",
    val price: Int = 0,
    var count: Int = 0,
    var i: Int = 0,
    var exchange: Int = 0,
    var endPrice: Int = 0,
    var status: Boolean = false,
    var priceOld: Int = 0,
    var old: Boolean = false
)