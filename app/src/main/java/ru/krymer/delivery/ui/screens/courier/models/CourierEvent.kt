package ru.krymer.delivery.ui.screens.courier.models

import ru.krymer.delivery.data.model.user.RoleModel
import ru.krymer.delivery.data.model.user.UserModel

sealed class CourierEvent {
    data class ToggleUpdateDialog(val user: UserModel?) : CourierEvent()
    data class ToggleBanUser(val user: UserModel?) : CourierEvent()
    data class ToggleDeleteDialog(val user: UserModel?) : CourierEvent()
    data object ToggleAddDialog : CourierEvent()
    data object ToggleUpdateSettingsDialog : CourierEvent()

    data class ChangedEmailUser(val email: String) : CourierEvent()
    data class ChangedPassUser(val pass: String) : CourierEvent()
    data class ChangeUsername(val name: String) : CourierEvent()
    data class ChangeUserPercent(val percent: String) : CourierEvent()
    data class ChangeUserSalary(val salary: String) : CourierEvent()
    data class ChangedSalary(val salary: String) : CourierEvent()
    data class ChangedPrice(val price: String) : CourierEvent()

    data class SelectedItemMenu(val role: RoleModel) : CourierEvent()

    data object CreateUser : CourierEvent()
    data object BanUser : CourierEvent()
    data object DeleteUser : CourierEvent()
    data object UpdateUser : CourierEvent()
    data object UpdateSettings : CourierEvent()

    data class Error(val message: String?): CourierEvent()

    object RefreshCouriers : CourierEvent()
    data class CouriersLoaded(val couriers: List<UserModel>) : CourierEvent()
}