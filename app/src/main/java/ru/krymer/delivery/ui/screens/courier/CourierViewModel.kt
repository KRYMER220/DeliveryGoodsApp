package ru.krymer.delivery.ui.screens.courier

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.krymer.delivery.common.EventHandler
import ru.krymer.delivery.data.api.FactoryApi
import ru.krymer.delivery.data.api.UserApi
import ru.krymer.delivery.data.model.user.RoleModel
import ru.krymer.delivery.data.model.user.StatusModel
import ru.krymer.delivery.data.model.user.UserModel
import ru.krymer.delivery.data.model.user.getStringByRole
import ru.krymer.delivery.data.model.user.getStringByStatus
import ru.krymer.delivery.data.model.utilModel.TypeMessageModel
import ru.krymer.delivery.data.request.SignUpRequest
import ru.krymer.delivery.data.request.UpdateFactoryRequest
import ru.krymer.delivery.data.request.UpdateUserRequest
import ru.krymer.delivery.ui.screens.courier.models.CourierEvent
import ru.krymer.delivery.ui.screens.courier.models.CourierViewState
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import ru.krymer.delivery.utills.Constants
import ru.krymer.delivery.utills.startsWithDigit
import javax.inject.Inject

@HiltViewModel
class CourierViewModel @Inject constructor(
    private val userApi: UserApi,
    private val sharedViewModel: SharedViewModel,
    private val factoryApi: FactoryApi
) : ViewModel(), EventHandler<CourierEvent> {

    private val _viewState = MutableStateFlow(CourierViewState())
    val viewState = _viewState.asStateFlow()

    private fun updateViewState(update: (CourierViewState) -> CourierViewState) {
        _viewState.update { update(it) }
    }

    private fun launchCoroutine(block: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                block()
            } catch (e: Exception) {
                sharedViewModel.message(e.message)
            }
        }
    }

    override fun obtainEvent(event: CourierEvent) {
        when (event) {
            is CourierEvent.ShowAddDialog -> showAddDialog()
            is CourierEvent.ShowBanDialog -> showBanDialog(event.user)
            is CourierEvent.UserItemClicked -> showDialogUpdate(event.user)
            is CourierEvent.UserSaveAction -> saveUser()
            is CourierEvent.ChangedEmailUser -> emailChanged(event.email)
            is CourierEvent.ChangedPassUser -> passChanged(event.pass)
            is CourierEvent.ChangeUsername -> changeName(event.name)
            is CourierEvent.ChangeUserPercent -> percentUpdateChanged(event.percent)
            is CourierEvent.UserUpdateAction -> updateUser()
            is CourierEvent.ChangedPrice -> priceUpdateChanged(event.price)
            is CourierEvent.ChangedSalary -> salaryUpdateChanged(event.salary)
            is CourierEvent.SettingsUpdateAction -> updateSettings()
            is CourierEvent.ShowUpdateSettingsDialog -> showSettings()
            is CourierEvent.DismissBanDialog -> dismissBanDialog()
            is CourierEvent.DismissUpdateSettingsDataDialog -> dismissUpdateSettingsDialog()
            is CourierEvent.DismissUpdateUserDataDialog -> dismissUpdateUserDialog()
            is CourierEvent.DismissAddDialog -> dismissAddDialog()
            is CourierEvent.BanUser -> banUser()
            is CourierEvent.ShowDeleteDialog -> showDeleteDialog(event.user)
            is CourierEvent.DismissDeleteDialog -> dismissDeleteDialog()
            is CourierEvent.DeleteUser -> deleteUser()
            is CourierEvent.DropDownMenuState -> changeStateDropMenu(event.state)
            is CourierEvent.SelectedItemMenu -> changeRole(event.role)
            is CourierEvent.ChangeUserSalary -> changeUserSalary(event.salary)
        }
    }

    init {
        getDataUsers()
    }

    private fun changeUserSalary(salary: String) {
        updateViewState { it.copy(userSalary = salary) }
    }

    private fun getDataUsers() {
        launchCoroutine {
            val user = sharedViewModel.viewState.value.user.value
            if (user != null) {
                val response = userApi.getUsers(idFactory = user.idFactory)
                if (response.success) {
                    val users = response.obj
                    if (users != null) {
                        updateViewState {
                            it.copy(
                                listUser = MutableStateFlow(users)
                            )
                        }
                    } else {
                        delay(5000)
                        getDataUsers()
                    }
                } else {
                    sharedViewModel.message(response.message)
                }
            }
        }
    }

    private fun changeRole(role: RoleModel) {
        updateViewState { it.copy(userRole = role) }
    }

    private fun changeStateDropMenu(state: Boolean) {
        updateViewState { it.copy(stateDropMenu = state) }
    }

    private fun showDeleteDialog(user: UserModel) {
        updateViewState {
            it.copy(
                isDeleteDialog = true, userDelete = user
            )
        }
    }

    private fun deleteUser() {
        launchCoroutine {
            val user = viewState.value.userDelete
            if (user != null && sharedViewModel.initSysAdm()) {
                if (user.id != sharedViewModel.viewState.value.user.value?.id) {
                    val response = userApi.delete(id = user.id)
                    if (response.success) {
                        val list =
                            viewState.value.listUser.value.map { it.copy() }.toMutableList()
                        val item = list.first { it.id == user.id }
                        val listNew = list - item
                        updateViewState { it.copy(listUser = MutableStateFlow(listNew.sortedBy { r -> r.name })) }
                        dismissDeleteDialog()
                    } else {
                        sharedViewModel.message(response.message)
                    }
                } else {
                    sharedViewModel.message(Constants.ERROR.RESRTRAINT)
                }
            } else {
                sharedViewModel.message(Constants.ERROR.ERROR)
            }
        }
    }

    private fun dismissDeleteDialog() {
        updateViewState { it.copy(isDeleteDialog = false) }
    }

    private fun showSettings() {
        val factory = sharedViewModel.viewState.value.factory
        if (factory != null) {
            updateViewState {
                it.copy(
                    showUpdateSettingsSheetDialog = true,
                    factory = factory
                )
            }
        }
    }

    private fun updateSettings() {
        launchCoroutine {
            val factory = viewState.value.factory
            if (factory != null) {
                val newFactory = UpdateFactoryRequest(
                    id = factory.id,
                    name = factory.name,
                    dateAdd = factory.dateAdd,
                    salary = factory.salary,
                    priceMillage = factory.priceMillage
                )
                val response = factoryApi.update(factory = newFactory)
                if (response.success) {
                    sharedViewModel.updateFactory(factoryModel = factory)
                    dismissUpdateSettingsDialog()
                } else {
                    sharedViewModel.message(response.message)
                }
            } else {
                sharedViewModel.message(Constants.EMPTY.EMPTY_DATA)
            }
        }
    }

    private fun showDialogUpdate(user: UserModel) {
        updateViewState {
            it.copy(
                showUpdateSheetDialog = true,
                updatedUser = user,
                userName = user.name,
                userPercent = "${user.percentSalary}",
                userSalary = "${user.salary}",
                isUserBanned = user.isBan,
                userPhone = user.phone,
                userRole = user.role,
            )
        }
    }

    private fun updateUser() {
        launchCoroutine {
            val name = viewState.value.userName
            val percent = if (viewState.value.userPercent == "") 0.0 else viewState.value.userPercent.toDouble()
            val salary = if (viewState.value.userSalary == "") 0.0 else viewState.value.userSalary.toDouble()
            val phone = viewState.value.userPhone
            val isBanned = viewState.value.isUserBanned
            val userUpdated = viewState.value.updatedUser
            val userRole = viewState.value.userRole
            if (userUpdated != null && userRole != null) {
                val userRequest = UpdateUserRequest(
                    id = userUpdated.id,
                    login = userUpdated.login,
                    name = name,
                    phone = phone,
                    role = userRole.getStringByRole(),
                    isBanned = isBanned,
                    percentSalary = percent.toDouble(),
                    status = userUpdated.status.getStringByStatus(),
                    salary = salary.toDouble()
                )
                val response = userApi.update(userRequest)
                if (response.success) {
                    val list = viewState.value.listUser.value.map { it.copy() }.toMutableList()
                    val index = list.indexOfFirst { it.id == userUpdated.id }
                    list[index] = userUpdated.copy(
                        name = name,
                        isBan = isBanned,
                        role = userRole,
                        phone = phone,
                        percentSalary = percent.toDouble(),
                        salary = salary.toDouble()
                    )
                    updateViewState { it.copy(listUser = MutableStateFlow(list)) }
                    sharedViewModel.message(
                        response.message,
                        typeMessageModel = TypeMessageModel.SUCCEED
                    )
                    dismissUpdateUserDialog()
                } else {
                    sharedViewModel.message(response.message)
                }
            }
        }
    }


    private fun saveUser() {
       launchCoroutine {
           val email = viewState.value.userEmail
           val pass = viewState.value.userPass
           val name = viewState.value.userName
           val user = sharedViewModel.viewState.value.user.value
           if (user != null) {
               if (sharedViewModel.initSysAdm()) {
                   val registerRequest = SignUpRequest(
                       email = email,
                       password = pass,
                       role = Constants.Role.USER,
                       idFactory = user.idFactory,
                       name = name,
                       status = StatusModel.OFFLINE.getStringByStatus()
                   )
                   val response = userApi.signUp(registerRequest)
                   if (response.success) {
                       val userAdded = response.obj
                       if (userAdded != null) {
                           val list =
                               viewState.value.listUser.value.map { it.copy() }.toMutableList()
                           list.add(userAdded)
                           updateViewState { it.copy(listUser = MutableStateFlow(list.sortedBy { u -> u.name })) }
                           sharedViewModel.message(
                               response.message,
                               typeMessageModel = TypeMessageModel.SUCCEED
                           )
                           dismissAddDialog()
                       }
                   } else {
                       sharedViewModel.message(response.message)
                   }
               } else {
                   sharedViewModel.message(Constants.ERROR.RESRTRAINT)
               }
           }
       }
    }

    private fun changeName(name: String) {
        updateViewState { it.copy(userName = name) }
    }

    private fun salaryUpdateChanged(salary: String) {
        val factory = viewState.value.factory
        if (salary.isNotEmpty() && factory != null && startsWithDigit(salary)) {
            updateViewState { it.copy(factory = factory.copy(salary = salary.toDouble())) }
        }
    }

    private fun priceUpdateChanged(price: String) {
        val factory = viewState.value.factory
        if (price.isNotEmpty() && factory != null && startsWithDigit(price)) {
            updateViewState { it.copy(factory = factory.copy(priceMillage = price.toDouble())) }
        }
    }

    private fun percentUpdateChanged(percent: String) {
        updateViewState { it.copy(userPercent = percent) }
    }

    private fun emailChanged(email: String = Constants.EMPTY.EMPTY_STRING) {
        updateViewState { it.copy(userEmail = email) }
    }

    private fun passChanged(pass: String = Constants.EMPTY.EMPTY_STRING) {
        updateViewState { it.copy(userPass = pass) }
    }

    private fun showBanDialog(user: UserModel) {
        updateViewState {
            it.copy(
                showBanDialog = true, userBan = user
            )
        }
    }

    private fun showAddDialog() {
        updateViewState {
            it.copy(
                showAddSheetDialog = true
            )
        }
    }

    private fun banUser() {
        launchCoroutine {
            val user = viewState.value.userBan
            if (user != null) {
                if (user.id != sharedViewModel.viewState.value.user.value?.id && user.role != RoleModel.SYSTEM) {
                    val userRequest = UpdateUserRequest(
                        id = user.id,
                        login = user.login,
                        name = user.name,
                        phone = user.phone,
                        role = user.role.getStringByRole(),
                        isBanned = !user.isBan,
                        percentSalary = user.percentSalary,
                        status = user.status.getStringByStatus(),
                        salary = user.salary
                    )
                    val response = userApi.update(userRequest)
                    if (response.success) {
                        val list =
                            viewState.value.listUser.value.map { it.copy() }.toMutableList()
                        val index = list.indexOfFirst { it.id == user.id }
                        list[index] = user.copy(
                            isBan = !user.isBan,
                        )
                        updateViewState { it.copy(listUser = MutableStateFlow(list)) }
                        dismissBanDialog()
                    } else {
                        sharedViewModel.message(response.message)
                    }
                } else {
                    sharedViewModel.message(Constants.ERROR.RESRTRAINT)
                }
            } else {
                sharedViewModel.message(Constants.ERROR.GENERAL_ERROR)
            }
        }
    }

    private fun dismissBanDialog() {
        updateViewState {
            it.copy(
                showBanDialog = false, userBan = null
            )
        }
    }

    private fun dismissAddDialog() {
        updateViewState {
            it.copy(
                showAddSheetDialog = false, userName = ""
            )
        }
    }

    private fun dismissUpdateUserDialog() {
        updateViewState {
            it.copy(
                showUpdateSheetDialog = false,
                userName = "",
                userPercent = "",
            )
        }
    }

    private fun dismissUpdateSettingsDialog() {
        updateViewState {
            it.copy(
                showUpdateSettingsSheetDialog = false
            )
        }
    }
}