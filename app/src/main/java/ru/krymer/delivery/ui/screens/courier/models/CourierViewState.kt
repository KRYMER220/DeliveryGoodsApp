package ru.krymer.delivery.ui.screens.courier.models

import kotlinx.coroutines.flow.MutableStateFlow
import ru.krymer.delivery.data.model.FactoryModel
import ru.krymer.delivery.data.model.user.RoleModel
import ru.krymer.delivery.data.model.user.UserModel

data class CourierViewState(
    val isError: Boolean = false,
    val errorValue: String = "",
    val listUser: MutableStateFlow<List<UserModel>> = MutableStateFlow(listOf()),
    val showAddSheetDialog: Boolean = false,
    val showUpdateSheetDialog: Boolean = false,
    val showUpdateSettingsSheetDialog: Boolean = false,
    val userBan: UserModel? = null,
    val showBanDialog: Boolean = false,
    val userSalary: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val userPass: String = "",
    val userPhone: String = "",
    val isUserBanned: Boolean = false,
    val userRole: RoleModel? = null,
    val userPercent: String = "",
    val updatedUser: UserModel? = null,
    val userDelete: UserModel? = null,
    val isDeleteDialog: Boolean = false,
    val factory: FactoryModel? = null,
    val salaryChange: String = "",
    val priceMillageChange: String = "",
    val stateDropMenu: Boolean = false
)