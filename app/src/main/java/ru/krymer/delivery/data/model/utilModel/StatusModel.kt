package ru.krymer.delivery.data.model.utilModel

enum class StatusModel {
    NOT_CHANGE, SYNC, UN_SYNC
}

fun StatusModel.toStr(): String = when(this) {
    StatusModel.NOT_CHANGE -> "notChange"
    StatusModel.SYNC -> "sync"
    StatusModel.UN_SYNC -> "unSync"
}

fun String.toStatusModel(): StatusModel = when(this) {
    "notChange" -> StatusModel.NOT_CHANGE
    "sync" -> StatusModel.SYNC
    "unSync" -> StatusModel.UN_SYNC
    else -> StatusModel.NOT_CHANGE
}
