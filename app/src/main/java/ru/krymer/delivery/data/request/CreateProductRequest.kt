package ru.krymer.delivery.data.request

data class CreateProductRequest(
    val id: Long ?= null,
    val name: String,
    val idFactory: Long,
    val counter: Int,
    val price: Double,
    val isActive: Boolean,
    val date: Long,
    val oldPrice: Double,
)
