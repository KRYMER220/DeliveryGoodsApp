package ru.krymer.delivery.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "request", primaryKeys = ["id", "idTrip", "idShop"])
data class RequestModel(
    val id: Long,
    val idShop: Long,
    val idTrip: Long,
    val idFactory: Long,
    val count: Int,
    val exchange: Int,
    val bonus: Int,
    val status: Boolean,
    val price: Double,
    val oldPrice: Double,
    val name: String,
    val counter: Int
)
