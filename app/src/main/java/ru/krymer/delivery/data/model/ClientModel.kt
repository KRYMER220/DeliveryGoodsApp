package ru.krymer.delivery.data.model

data class ClientModel(
    val id: Long,
    val idRoute: Long,
    val idFactory: Long,
    val name: String,
    val phone: String,
    val cord: String,
    var counter: Int,
    val arrears: Double,
    val date: Long
)
