package ru.krymer.delivery.data.model

data class RouteModel(
    val id: Long,
    var name: String,
    val date: Long,
    val idFactory: Long,
)
