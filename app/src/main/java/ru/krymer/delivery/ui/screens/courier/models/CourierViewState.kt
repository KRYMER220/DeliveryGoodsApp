package ru.krymer.delivery.ui.screens.courier.models

import kotlinx.coroutines.flow.MutableStateFlow
import ru.krymer.delivery.data.model.user.RoleModel
import ru.krymer.delivery.data.model.user.UserModel

data class CourierViewState(
    val isError: Boolean = false,
    val errorValue: String = "",
    val isLoadUserData: Boolean = false,
    val listUser: MutableStateFlow<List<UserModel>> = MutableStateFlow(listOf()),
    val showAddSheetDialog: Boolean = false,
    val showUpdateSheetDialog: Boolean = false,
    val showUpdateSettingsSheetDialog: Boolean = false,
    val userBan: UserModel? = null,
    val showBanDialog: Boolean = false,
    val userName: String? = null,
    val userEmail: String? = null,
    val userPass: String? = null,
    val userPhone: String? = null,
    val isErrorEmail: Boolean = false,
    val errorEmailValue: String = "",
    val isErrorPass: Boolean = false,
    val errorPassValue: String = "",
    val isErrorName: Boolean = false,
    val errorNameValue: String = "",
    val isErrorPhone: Boolean = false,
    val errorPhoneValue: String = "",
    val isErrorPercent: Boolean = false,
    val isUserBanned: Boolean? = null,
    val userRole: RoleModel? = null,
    val userPercent: String? = null,
    val updatedUser: UserModel? = null,
    val userDelete: UserModel? = null,
    val isDeleteDialog: Boolean = false,
    val salaryChange: String? = null,
    val priceChange: String? = null,
    val isErrorSalary: Boolean = false,
    val isErrorPrice: Boolean = false,
    val stateDropMenu: Boolean = false
)