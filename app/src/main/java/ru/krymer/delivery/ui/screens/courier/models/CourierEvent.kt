package ru.krymer.delivery.ui.screens.courier.models

import ru.krymer.delivery.data.model.user.RoleModel
import ru.krymer.delivery.data.model.user.UserModel

sealed class CourierEvent {
    data class UserItemClicked(val user: UserModel) : CourierEvent()
    data class ShowBanDialog(val user: UserModel) : CourierEvent()
    data object ShowAddDialog : CourierEvent()
    data object ShowUpdateSettingsDialog : CourierEvent()
    data class ChangedEmailUser(val email: String) : CourierEvent()
    data class ChangedPassUser(val pass: String) : CourierEvent()
    data object UserSaveAction : CourierEvent()
    data object UserUpdateAction : CourierEvent()
    data class ChangedNameUser(val name: String) : CourierEvent()
    data class ChangedPercentUser(val percent: String) : CourierEvent()
    data class ChangedSalary(val salary: String) : CourierEvent()
    data class ChangedPrice(val price: String) : CourierEvent()
    data object SettingsUpdateAction : CourierEvent()
    data object DismissBanDialog : CourierEvent()
    data object DismissUpdateUserDataDialog : CourierEvent()
    data object DismissUpdateSettingsDataDialog : CourierEvent()
    data object DismissAddDialog : CourierEvent()
    data object BanUser : CourierEvent()
    data class ShowDeleteDialog(val user: UserModel) : CourierEvent()
    data object DismissDeleteDialog : CourierEvent()
    data object DeleteUser : CourierEvent()
    data class DropDownMenuState(val state: Boolean) : CourierEvent()
    data class SelectedItemMenu(val role: RoleModel) : CourierEvent()
}