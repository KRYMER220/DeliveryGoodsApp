package ru.krymer.delivery.ui.screens.courier.models

import ru.krymer.delivery.data.model.FactoryModel
import ru.krymer.delivery.data.model.user.RoleModel
import ru.krymer.delivery.data.model.user.UserModel

data class CourierViewState(
    val couriers: List<UserModel> = emptyList(),
    val isLoading: Boolean = false,
    val toggleAddDialog: Boolean = false,
    val toggleUpdateCourier: Boolean = false,
    val toggleUpdateSettings: Boolean = false,
    val toggleBanDialog: Boolean = false,
    val userSalary: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val userPass: String = "",
    val userPhone: String = "",
    val userRole: RoleModel? = null,
    val userPercent: String = "",
    val user: UserModel? = null,
    val toggleDeleteCourier: Boolean = false,
    val factory: FactoryModel? = null,
    val salaryChange: String = "",
    val priceMillage: String = "",
)