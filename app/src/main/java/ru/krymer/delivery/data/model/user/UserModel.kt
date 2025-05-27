package ru.krymer.delivery.data.model.user

import androidx.room.Entity
import androidx.room.PrimaryKey

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
    val isBan: Boolean,
    val role: RoleModel,
    val percentSalary: Double,
    val salary: Double
) {
    fun isSysOrAdmin(): Boolean = role == RoleModel.ADMIN || role == RoleModel.SYSTEM
    fun isModOrAdminOrSys(): Boolean =
        role == RoleModel.MODERATOR || role == RoleModel.ADMIN || role == RoleModel.SYSTEM
}
