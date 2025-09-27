package ru.krymer.delivery.data.model.user

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.krymer.delivery.utills.Constants

@Entity(tableName = "users")
data class UserModel(
    @PrimaryKey
    val id: Long,
    val email: String,
    val login: String,
    val password: String,
    val idFactory: Long,
    val name: String,
    val phone: String,
    val status: StatusModel,
    var isBan: Boolean,
    val role: RoleModel,
    val percentSalary: Double,
    val salary: Double
) {
    fun isMod(): Boolean = role == RoleModel.MODERATOR
    fun isSysOrAdmin(): Boolean = role == RoleModel.ADMIN || role == RoleModel.SYSTEM
    fun isSys(): Boolean = role == RoleModel.SYSTEM
    fun isModOrAdminOrSys(): Boolean =
        role == RoleModel.MODERATOR || role == RoleModel.ADMIN || role == RoleModel.SYSTEM
}

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