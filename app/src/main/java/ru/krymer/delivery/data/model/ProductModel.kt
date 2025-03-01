package ru.krymer.delivery.data.model

data class ProductModel(
    val id: Long,
    val name: String,
    val idFactory: Long,
    val counter: Int,
    val price: Double,
    val isActive: Boolean,
    val date: Long,
    val count: Int,
    val addCount: Int,
    val oldPrice: Double,
    val exchange: Int ?= null
)