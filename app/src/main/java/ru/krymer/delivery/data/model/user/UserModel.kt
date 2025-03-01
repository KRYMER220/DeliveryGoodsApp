package ru.krymer.delivery.data.model.user

data class UserModel(
    val id: Long,
    val email: String,
    val login: String,
    val password: String,
    val idFactory: Long,
    val name: String,
    val phone: String,
    val status: StatusModel,
    val isBanned: Boolean,
    val role: RoleModel,
    val percentSalary: Double,
)
