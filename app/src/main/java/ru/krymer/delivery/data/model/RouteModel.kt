package ru.krymer.delivery.data.model

import kotlinx.serialization.Serializable

@Serializable
data class RouteModel(
    val id: Long,
    var name: String,
    val date: Long,
    val idFactory: Long
)
