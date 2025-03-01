package ru.krymer.delivery.data.request


data class RouteRequest(
    val name: String,
    val idFactory: Long,
    val date: Long,
    val id: Long? = null,
)