package ru.krymer.delivery.data.model.user

import ru.krymer.delivery.utills.Constants

enum class RoleModel {
    ADMIN, MODERATOR, USER, SYSTEM
}

fun RoleModel.getStringByRole(): String {
    return when (this) {
        RoleModel.ADMIN -> Constants.Role.ADMIN
        RoleModel.MODERATOR -> Constants.Role.MODERATOR
        RoleModel.SYSTEM -> Constants.Role.SYSTEM
        else -> Constants.Role.USER
    }
}