package ru.krymer.delivery.ui.screens.courier

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.krymer.delivery.common.EventHandler
import ru.krymer.delivery.data.model.FactoryModel
import ru.krymer.delivery.data.model.user.RoleModel
import ru.krymer.delivery.data.model.user.StatusModel
import ru.krymer.delivery.data.model.user.UserModel
import ru.krymer.delivery.data.model.user.getStringByRole
import ru.krymer.delivery.data.model.user.getStringByStatus
import ru.krymer.delivery.data.model.utilModel.TypeMessageModel
import ru.krymer.delivery.data.repositoryImpl.CourierRepositoryImpl
import ru.krymer.delivery.data.request.FactoryRequest
import ru.krymer.delivery.data.request.SignUpRequest
import ru.krymer.delivery.data.request.UserRequest
import ru.krymer.delivery.ui.screens.courier.models.CourierEvent
import ru.krymer.delivery.ui.screens.courier.models.CourierViewState
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import ru.krymer.delivery.utills.Constants
import ru.krymer.delivery.utills.MyResult
import javax.inject.Inject

@HiltViewModel
class CourierViewModel @Inject constructor(
    private val repository: CourierRepositoryImpl,
    private val sharedViewModel: SharedViewModel,
) : ViewModel(), EventHandler<CourierEvent> {

    private val _viewState = MutableStateFlow(CourierViewState())
    val viewState = _viewState.asStateFlow()

    private fun updateState(update: (CourierViewState) -> CourierViewState) {
        _viewState.update { update(it) }
    }

    private fun launchCoroutine(block: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
                sharedViewModel.message(
                    Constants.ERROR.CANCEL_OPERATION,
                    type = TypeMessageModel.ERROR
                )
            } catch (e: TimeoutCancellationException) {
                throw e
                sharedViewModel.message(Constants.ERROR.TIMEOUT, type = TypeMessageModel.ERROR)
            } catch (e: Exception) {
                throw e
                sharedViewModel.message(e.message, type = TypeMessageModel.ERROR)
            }
        }
    }


    override fun obtainEvent(event: CourierEvent) {
        when (event) {
            is CourierEvent.ToggleAddDialog -> setState(add = !viewState.value.toggleAddCourier)
            is CourierEvent.ToggleBanUser -> setState(ban = !viewState.value.toggleBanDialog, user = event.user)
            is CourierEvent.ToggleUpdateDialog -> setState(user = event.user, updateCourier = !viewState.value.toggleUpdateCourier)
            is CourierEvent.CreateUser -> createUser()
            is CourierEvent.ChangedEmailUser -> setValue(email = event.email)
            is CourierEvent.ChangedPassUser -> setValue(pass = event.pass)
            is CourierEvent.ChangeUsername -> setValue(name = event.name)
            is CourierEvent.ChangeUserPercent -> setValue(percent = event.percent)
            is CourierEvent.UpdateUser -> updateUser()
            is CourierEvent.ChangedPrice -> setValue(priceKm = event.price)
            is CourierEvent.ChangedSalary -> setValue(salarySys = event.salary)
            is CourierEvent.UpdateSettings -> updateSettings()
            is CourierEvent.ToggleUpdateSettingsDialog -> {
                val factory = sharedViewModel.viewState.value.factory
                if (factory != null) {
                    setState(factory = factory, updateSettings = !viewState.value.toggleSettingsFactory)
                }
            }

            is CourierEvent.BanUser -> {
                val user = viewState.value.user
                if (user != null)
                    updateState { it.copy(user = user.copy(isBan = !user.isBan)) }
                setState(ban = false)
            }
            is CourierEvent.ToggleDeleteDialog -> setState(user = event.user, delete = !viewState.value.toggleDeleteCourier)
            is CourierEvent.DeleteUser -> deleteUser()
            is CourierEvent.SelectedItemMenu -> setValue(role = event.role)
            is CourierEvent.ChangeUserSalary -> setValue(salary = event.salary)
        }
    }

    init {
        getDataUsers()
    }

    private fun setValue(
        name: String = viewState.value.userName,
        role: RoleModel? = viewState.value.userRole,
        percent: String = viewState.value.userPercent,
        salary: String = viewState.value.userSalary,
        email: String = viewState.value.userEmail,
        pass: String = viewState.value.userPass,
        priceKm: String = viewState.value.priceMillage,
        salarySys: String = viewState.value.salaryChange,
    ) {
        updateState {
            it.copy(
                userPass = pass,
                userRole = role,
                userSalary = salary,
                userEmail = email,
                userName = name,
                userPercent = percent,
                priceMillage = priceKm,
                salaryChange = salarySys,
            )
        }
    }

    private fun getDataUsers() = launchCoroutine {
        updateState { it.copy(isLoading = true) }
        val user = sharedViewModel.viewState.value.user
        if (user == null) {
            updateState { it.copy(isLoading = false) }
            sharedViewModel.message(Constants.ERROR.AGAIN, type = TypeMessageModel.ERROR)
            return@launchCoroutine
        }

        when (val res = repository.getUsers(idFactory = user.idFactory)) {
            is MyResult.Success -> updateState { it.copy(couriers = res.data, isLoading = false) }
            is MyResult.Error -> {
                updateState { it.copy(isLoading = false) }
                sharedViewModel.message(res.message, type = TypeMessageModel.ERROR)
            }
        }
    }

    private fun deleteUser() = launchCoroutine {
        val user = viewState.value.user
        val userSignIn = sharedViewModel.viewState.value.user

        if (user == null) {
            sharedViewModel.message(Constants.ERROR.AGAIN, type = TypeMessageModel.ERROR)
            return@launchCoroutine
        }

        if (userSignIn == null) {
            sharedViewModel.message(Constants.ERROR.AGAIN, type = TypeMessageModel.ERROR)
            return@launchCoroutine
        }

        if (user.id != userSignIn.id) {
            when (val res = repository.delete(id = user.id)) {
                is MyResult.Success -> {
                    getDataUsers()
                    setState(user = null, delete = false)
                }

                is MyResult.Error -> sharedViewModel.message(
                    res.message,
                    type = TypeMessageModel.ERROR
                )
            }
        } else {
            sharedViewModel.message(Constants.ERROR.RESRTRAINT, type = TypeMessageModel.ERROR)
        }
    }

    private fun setState(
        factory: FactoryModel? = viewState.value.factory,
        user: UserModel? = viewState.value.user,
        delete: Boolean = viewState.value.toggleDeleteCourier,
        updateCourier: Boolean = viewState.value.toggleUpdateCourier,
        updateSettings: Boolean = viewState.value.toggleSettingsFactory,
        add: Boolean = viewState.value.toggleAddCourier,
        ban: Boolean = viewState.value.toggleBanDialog,
    ) {
        updateState {
            it.copy(
                user = user,
                factory = factory,
                toggleAddCourier = add,
                toggleUpdateCourier = updateCourier,
                toggleDeleteCourier = delete,
                toggleSettingsFactory = updateSettings,
                toggleBanDialog = ban
            )
        }

        if (updateCourier && user != null) {
            setValue(name = user.name, percent = user.percentSalary.toString(), salary = user.percentSalary.toString(), role = user.role)
        }

        if (updateSettings && factory != null) {
            setValue(salarySys = factory.salary.toString(), priceKm = factory.priceMillage.toString())
        }
    }


    private fun updateSettings() = launchCoroutine {
        val factory = viewState.value.factory

        if (factory == null) {
            sharedViewModel.message(Constants.ERROR.AGAIN, type = TypeMessageModel.ERROR)
            return@launchCoroutine
        }

        val salary = viewState.value.salaryChange.toDouble()
        val price = viewState.value.priceMillage.toDouble()

        val request = FactoryRequest(
            id = factory.id,
            name = factory.name,
            dateAdd = factory.dateAdd,
            salary = salary,
            priceMillage = price
        )

        when (val res = repository.updateFactory(factory = request)) {
            is MyResult.Success -> {
                sharedViewModel.updateFactory(factory = factory.copy(salary = salary, priceMillage = price))
                setState(updateSettings = false)
            }

            is MyResult.Error -> sharedViewModel.message(res.message, type = TypeMessageModel.ERROR)
        }
    }


    private fun updateUser() = launchCoroutine {
        var user = viewState.value.user
        val userRole = viewState.value.userRole
        val userSignIn = sharedViewModel.viewState.value.user

        if (userSignIn == null) {
            sharedViewModel.message(Constants.ERROR.AGAIN, type = TypeMessageModel.ERROR)
            return@launchCoroutine
        }

        if (user == null) {
            sharedViewModel.message(Constants.ERROR.AGAIN, type = TypeMessageModel.ERROR)
            return@launchCoroutine
        }

        if (userRole == null) {
            sharedViewModel.message(Constants.ERROR.AGAIN, type = TypeMessageModel.ERROR)
            return@launchCoroutine
        }

        if (user.id == userSignIn.id) {
            sharedViewModel.message(Constants.ERROR.RESRTRAINT, type = TypeMessageModel.ERROR)
            return@launchCoroutine
        }

        val name = viewState.value.userName.ifBlank { user.name }
        val salary = viewState.value.userSalary.toDouble()
        val percent = viewState.value.userPercent.toDouble()
        val role = viewState.value.userRole ?: RoleModel.USER

        val request = UserRequest(
            id = user.id,
            login = user.login,
            name = name,
            phone = user.phone,
            role = role.getStringByRole(),
            isBanned = user.isBan,
            percentSalary = percent,
            status = user.status.getStringByStatus(),
            salary = salary
        )

        when (val res = repository.update(request = request)) {
            is MyResult.Success -> {
                getDataUsers()
                setState(updateCourier = false, user = null)
            }
            is MyResult.Error -> sharedViewModel.message(res.message, type = TypeMessageModel.ERROR)
        }
    }


    private fun createUser() = launchCoroutine {
        val email = viewState.value.userEmail
        val pass = viewState.value.userPass
        val name = viewState.value.userName.ifBlank { "Пользователь #${viewState.value.couriers.size}" }
        val user = sharedViewModel.viewState.value.user
        if (user == null) {
            sharedViewModel.message(Constants.ERROR.AGAIN, type = TypeMessageModel.ERROR)
            return@launchCoroutine
        }

        val request = SignUpRequest(
            email = email,
            password = pass,
            role = Constants.Role.USER,
            idFactory = user.idFactory,
            name = name,
            status = StatusModel.OFFLINE.getStringByStatus()
        )

        when (val res = repository.signUp(request = request)) {
            is MyResult.Success -> {
                getDataUsers()
                setState(add = false)
            }

            is MyResult.Error -> sharedViewModel.message(res.message, type = TypeMessageModel.ERROR)
        }
    }
}