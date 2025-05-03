package ru.krymer.delivery.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class TripModel(
    @PrimaryKey
    val id: Long,
    val idFactory: Long,
    val date: Long,
    val salary: Double,
    val percentCourier: Double,
    val salaryCourier: Double,
    val priceMillage: Double,
    var millage: Double,
    val idCourier: Long,
    val nameCourier: String,
    val nameRoute: String,
    val idRoute: Long
)