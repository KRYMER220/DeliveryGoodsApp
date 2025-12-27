package ru.krymer.delivery.data.model.utilModel

enum class StatusModel {
    NOT_CHANGE, SYNC, UN_SYNC, SYNC_FAILED
}

fun StatusModel.toStr(): String = when(this) {
    StatusModel.NOT_CHANGE -> "notChange"
    StatusModel.SYNC -> "sync"
    StatusModel.UN_SYNC -> "unSync"
    StatusModel.SYNC_FAILED -> "failed"
}

fun String.toStatusModel(): StatusModel = when(this) {
    "notChange" -> StatusModel.NOT_CHANGE
    "sync" -> StatusModel.SYNC
    "unSync" -> StatusModel.UN_SYNC
    "failed" -> StatusModel.SYNC_FAILED
    else -> StatusModel.SYNC_FAILED
}
