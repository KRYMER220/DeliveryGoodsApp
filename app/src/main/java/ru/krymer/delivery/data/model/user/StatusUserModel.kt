package ru.krymer.delivery.data.model.user

import ru.krymer.delivery.utills.Constants

enum class StatusModel {
    OFFLINE, ONLINE
}

fun StatusModel.getStringByStatus(): String {
    return when (this) {
        StatusModel.ONLINE -> Constants.UserStatus.ONLINE
        StatusModel.OFFLINE -> Constants.UserStatus.OFFLINE
    }
}