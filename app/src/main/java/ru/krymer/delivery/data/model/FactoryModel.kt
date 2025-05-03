package ru.krymer.delivery.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "factory")
data class FactoryModel(
    @PrimaryKey
    val id: Long,
    val name: String,
    val dateAdd: Long,
    val salary: Double,
    val priceMillage: Double,
)