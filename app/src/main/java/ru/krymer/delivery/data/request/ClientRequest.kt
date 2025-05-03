package ru.krymer.delivery.data.request

data class ClientRequest(
    val id: Long? = null,
    val idRoute: Long,
    val idFactory: Long,
    val name: String,
    val phone: String,
    val cord: String,
    val counter: Int,
    val arrears: Double,
    val date: Long
)
