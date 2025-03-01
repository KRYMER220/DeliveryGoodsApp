package ru.krymer.delivery.ui.screens.courier

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
import ru.krymer.delivery.utills.isEmptyInput
import ru.krymer.delivery.utills.isValidEmail
import javax.inject.Inject

@HiltViewModel
class CourierViewModel @Inject constructor(
    private val userApi: UserApi,
    private val sharedViewModel: SharedViewModel,
    private val factoryApi: FactoryApi
) : ViewModel(), EventHandler<CourierEvent> {

    private val _viewState = MutableStateFlow(CourierViewState())
    val viewState: StateFlow<CourierViewState> = _viewState

    private fun updateViewState(update: (CourierViewState) -> CourierViewState) {
        _viewState.update { update(it) }
    }

    override fun obtainEvent(event: CourierEvent) {
        when (event) {
            is CourierEvent.ShowAddDialog -> showAddDialog()
            is CourierEvent.ShowBanDialog -> showBanDialog(event.user)
            is CourierEvent.UserItemClicked -> showDialogUpdate(event.user)
            is CourierEvent.UserSaveAction -> saveUser()
            is CourierEvent.ChangedEmailUser -> emailChanged(event.email)
            is CourierEvent.ChangedPassUser -> passChanged(event.pass)
            is CourierEvent.ChangedNameUser -> changeName(event.name)
            is CourierEvent.ChangedPercentUser -> percentUpdateChanged(event.percent)
            is CourierEvent.UserUpdateAction -> updateUser()
            is CourierEvent.ChangedPrice -> priceUpdateChanged(event.price)
            is CourierEvent.ChangedSalary -> salaryUpdateChanged(event.salary)
            is CourierEvent.SettingsUpdateAction -> saveSettings()
            is CourierEvent.ShowUpdateSettingsDialog -> showSettings()
            is CourierEvent.DismissBanDialog -> dismissBanDialog()
            is CourierEvent.DismissUpdateSettingsDataDialog -> dismissUpdateSettingsDataDialog()
            is CourierEvent.DismissUpdateUserDataDialog -> dismissUpdateUserDataDialog()
            is CourierEvent.DismissAddDialog -> dismissAddDialog()
            is CourierEvent.BanUser -> banUser()
            is CourierEvent.ShowDeleteDialog -> showDeleteDialog(event.user)
            is CourierEvent.DismissDeleteDialog -> dismissDeleteDialog()
            is CourierEvent.DeleteUser -> deleteUser()
            is CourierEvent.DropDownMenuState -> changeStateDropMenu(event.state)
            is CourierEvent.SelectedItemMenu -> changeRole(event.role)
        }
    }

    init {
        getDataUsers()
    }

    private fun getDataUsers() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = sharedViewModel.viewState.value.user
                if (user != null) {
                    val response = userApi.getListUser(idFactory = user.idFactory)
                    if (response.success) {
                        val users = response.obj?.sortedBy { it.name }
                        if (users != null) {
                            updateViewState {
                                it.copy(
                                    listUser = MutableStateFlow(users), isLoadUserData = true
                                )
                            }
                        } else {
                            sharedViewModel.message(Constants.ERROR.GENERAL_ERROR)
                        }
                    } else {
                        sharedViewModel.message(response.message)
                    }
                }
            } catch (e: Exception) {
                sharedViewModel.message(e.message)
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
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = viewState.value.userDelete
                if (user != null) {
                    if (user.id != sharedViewModel.viewState.value.user?.id) {
                        val response = userApi.deleteUser(idUser = user.id)
                        if (response.success) {
                            val list =
                                viewState.value.listUser.value.map { it.copy() }.toMutableList()
                            val item = list.first { it.id == user.id }
                            val listNew = list - item
                            updateViewState { it.copy(listUser = MutableStateFlow(listNew.sortedBy { r -> r.name })) }
                        } else {
                            sharedViewModel.message(response.message)
                        }
                    } else {
                        sharedViewModel.message(Constants.ERROR.RESRTRAINT)
                    }
                } else {
                    sharedViewModel.message(Constants.ERROR.ERROR)
                }
            } catch (e: Exception) {
                sharedViewModel.message(e.message)
            } finally {
                dismissDeleteDialog()
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
                    priceChange = "${factory.priceMillage}",
                    salaryChange = "${factory.salary}"
                )
            }
        }
    }

    private fun saveSettings() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val factory = sharedViewModel.viewState.value.factory
                val price = viewState.value.priceChange?.toDouble()
                val salary = viewState.value.salaryChange?.toDouble()
                if (factory != null && price != null && salary != null) {
                    val newFactory = UpdateFactoryRequest(
                        id = factory.id,
                        name = factory.name,
                        dateAdd = factory.dateAdd,
                        salary = salary,
                        priceMillage = price
                    )
                    val response = factoryApi.updateFactory(factory = newFactory)
                    if (response.success) {
                        val updatedFactory = factory.copy(salary = salary, priceMillage = price)
                        sharedViewModel.updateFactory(factoryModel = updatedFactory)
                    } else {
                        sharedViewModel.message(response.message)
                    }
                } else {
                    sharedViewModel.message(Constants.EMPTY.EMPTY_DATA)
                }
            } catch (e: Exception) {
                sharedViewModel.message(e.message)
            } finally {
                dismissUpdateSettingsDataDialog()
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
                isUserBanned = user.isBanned,
                userPhone = user.phone,
                userRole = user.role,
            )
        }
    }

    private fun updateUser() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val name = viewState.value.userName
                val percent = viewState.value.userPercent?.toDouble()
                val phone = viewState.value.userPhone
                val isBanned = viewState.value.isUserBanned
                val userUpdated = viewState.value.updatedUser
                val userRole = viewState.value.userRole
                if (!viewState.value.isErrorPercent && !viewState.value.isErrorName && userUpdated != null && userRole != null && isBanned != null && !viewState.value.isErrorPhone) {
                    val userRequest = UpdateUserRequest(
                        id = userUpdated.id,
                        login = userUpdated.login,
                        name = name ?: userUpdated.name,
                        phone = phone ?: userUpdated.phone,
                        role = userRole.getStringByRole(),
                        isBanned = isBanned,
                        percentSalary = percent ?: userUpdated.percentSalary,
                        status = userUpdated.status.getStringByStatus()
                    )
                    val response = userApi.updateUser(userRequest)
                    if (response.success) {
                        val list = viewState.value.listUser.value.map { it.copy() }.toMutableList()
                        val index = list.indexOfFirst { it.id == userUpdated.id }
                        list[index] = userUpdated.copy(
                            name = name ?: userUpdated.name,
                            isBanned = isBanned,
                            role = userRole,
                            phone = phone ?: userUpdated.phone,
                            percentSalary = percent ?: userUpdated.percentSalary
                        )
                        updateViewState { it.copy(listUser = MutableStateFlow(list)) }
                        sharedViewModel.message(
                            response.message,
                            typeMessageModel = TypeMessageModel.SUCCEED
                        )
                    } else {
                        sharedViewModel.message(response.message)
                    }
                }
            } catch (e: Exception) {
                sharedViewModel.message(e.message)
            } finally {
                dismissUpdateUserDataDialog()
            }
        }
    }


    private fun saveUser() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val email = viewState.value.userEmail
                val pass = viewState.value.userPass
                val name = viewState.value.userName
                val user = sharedViewModel.viewState.value.user
                if (!viewState.value.isErrorName && !viewState.value.isErrorPass && !viewState.value.isErrorEmail && user != null) {
                    if (sharedViewModel.initSysAdm()) {
                        val registerRequest = SignUpRequest(
                            email = email!!,
                            password = pass!!,
                            role = Constants.Role.USER,
                            idFactory = user.idFactory,
                            name = name
                                ?: (Constants.Role.USER + "${viewState.value.listUser.value.size}"),
                            status = StatusModel.OFFLINE.getStringByStatus()
                        )
                        val response = userApi.signUpUser(registerRequest)
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
                            }
                        } else {
                            sharedViewModel.message(response.message)
                        }
                    } else {
                        sharedViewModel.message(Constants.ERROR.RESRTRAINT)
                    }
                }
            } catch (e: Exception) {
                sharedViewModel.message(e.message)
            } finally {
                dismissAddDialog()
            }
        }
    }

    private fun changeName(name: String) {
        if (isEmptyInput(name)) {
            updateViewState { it.copy(userName = name, isErrorName = false) }
        } else {
            updateViewState {
                it.copy(
                    isErrorName = true,
                    errorNameValue = Constants.EMPTY.EMPTY_USER_NAME,
                    userName = name
                )
            }
        }

    }

    private fun salaryUpdateChanged(salary: String) {
        if (isEmptyInput(salary)) {
            updateViewState { it.copy(salaryChange = salary, isErrorSalary = false) }
        } else {
            updateViewState {
                it.copy(
                    isErrorSalary = true,
                    errorValue = Constants.EMPTY.EMPTY_SALARY,
                    salaryChange = salary
                )
            }
        }
    }

    private fun priceUpdateChanged(price: String) {
        if (isEmptyInput(price)) {
            updateViewState { it.copy(priceChange = price, isErrorPrice = false) }
        } else {
            updateViewState {
                it.copy(
                    isErrorPrice = true,
                    errorValue = Constants.EMPTY.EMPTY_PRICE_MILLAGE,
                    priceChange = price
                )
            }
        }
    }

    private fun percentUpdateChanged(percent: String) {
        if (isEmptyInput(percent)) {
            updateViewState { it.copy(userPercent = percent, isErrorPercent = false) }
        } else {
            updateViewState {
                it.copy(
                    isErrorPercent = true,
                    errorValue = Constants.EMPTY.EMPTY_USER_PERCENT,
                    userPercent = percent
                )
            }
        }

    }

    private fun emailChanged(email: String = Constants.EMPTY.EMPTY_STRING) {
        if (isEmptyInput(email)) {
            if (isValidEmail(email)) {
                updateViewState { it.copy(userEmail = email, isErrorEmail = false) }
            } else {
                updateViewState {
                    it.copy(
                        isErrorEmail = true,
                        errorEmailValue = Constants.ERROR.EMAIL_INVALID,
                        userEmail = email
                    )
                }
            }
        } else {
            updateViewState {
                it.copy(
                    isErrorEmail = true,
                    errorEmailValue = "${Constants.EMPTY.FIELD_IMPORTANT} ${Constants.EMPTY.EMPTY_FIELD}",
                    userEmail = email
                )
            }
        }
    }

    private fun passChanged(pass: String = Constants.EMPTY.EMPTY_STRING) {
        if (isEmptyInput(pass)) {
            if (pass.length > 7) {
                updateViewState { it.copy(userPass = pass, isErrorPass = false) }
            } else {
                updateViewState {
                    it.copy(
                        isErrorPass = true,
                        errorPassValue = Constants.ERROR.PASS_INVALID,
                        userPass = pass
                    )
                }
            }
        } else {
            updateViewState {
                it.copy(
                    isErrorPass = true,
                    errorPassValue = "${Constants.EMPTY.FIELD_IMPORTANT} ${Constants.EMPTY.EMPTY_FIELD}",
                    userPass = pass
                )
            }
        }

    }

    private fun showBanDialog(user: UserModel) {
        updateViewState {
            it.copy(
                showBanDialog = true, userBan = user
            )
        }
    }

    private fun showAddDialog() {
        emailChanged()
        passChanged()
        updateViewState {
            it.copy(
                showAddSheetDialog = true
            )
        }
    }

    private fun banUser() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = viewState.value.userBan
                if (user != null) {
                    if (user.id != sharedViewModel.viewState.value.user?.id && user.role != RoleModel.SYSTEM) {
                        val userRequest = UpdateUserRequest(
                            id = user.id,
                            login = user.login,
                            name = user.name,
                            phone = user.phone,
                            role = user.role.getStringByRole(),
                            isBanned = !user.isBanned,
                            percentSalary = user.percentSalary,
                            status = user.status.getStringByStatus()
                        )
                        val response = userApi.updateUser(userRequest)
                        if (response.success) {
                            val list =
                                viewState.value.listUser.value.map { it.copy() }.toMutableList()
                            val index = list.indexOfFirst { it.id == user.id }
                            list[index] = user.copy(
                                isBanned = !user.isBanned,
                            )
                            updateViewState { it.copy(listUser = MutableStateFlow(list)) }
                        } else {
                            sharedViewModel.message(response.message)
                        }
                    } else {
                        sharedViewModel.message(Constants.ERROR.RESRTRAINT)
                    }
                } else {
                    sharedViewModel.message(Constants.ERROR.GENERAL_ERROR)
                }
            } catch (e: Exception) {
                sharedViewModel.message(e.message)
            } finally {
                dismissBanDialog()
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

    private fun dismissUpdateUserDataDialog() {
        updateViewState {
            it.copy(
                showUpdateSheetDialog = false,
                userName = null,
                userPercent = null,
            )
        }
    }

    private fun dismissUpdateSettingsDataDialog() {
        updateViewState {
            it.copy(
                showUpdateSettingsSheetDialog = false
            )
        }
    }
}