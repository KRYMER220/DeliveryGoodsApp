package ru.krymer.delivery.data.request


data class DateRequest(
    val dateStart: Long,
    val dateEnd: Long,
    val id:  Long?= null
)
