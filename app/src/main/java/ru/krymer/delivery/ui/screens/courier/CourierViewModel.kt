package ru.krymer.delivery.ui.screens.courier

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.krymer.delivery.AppDatabase
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
import ru.krymer.delivery.ui.screens.client.models.ClientEvent
import ru.krymer.delivery.ui.screens.courier.models.CourierEvent
import ru.krymer.delivery.ui.screens.courier.models.CourierViewState
import ru.krymer.delivery.ui.screens.route.models.RouteEvent
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import ru.krymer.delivery.utills.Constants
import ru.krymer.delivery.utills.MyResult
import javax.inject.Inject

@HiltViewModel
class CourierViewModel @Inject constructor(
    private val repository: CourierRepositoryImpl,
    private val sharedViewModel: SharedViewModel,
    private val room: AppDatabase
) : ViewModel() {

    private val _events = MutableSharedFlow<CourierEvent>(extraBufferCapacity = 64)

    private fun launchCoroutine(block: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
                _events.emit(CourierEvent.Error(Constants.ERROR.CANCEL_OPERATION))
            } catch (e: TimeoutCancellationException) {
                throw e
                _events.emit(CourierEvent.Error(Constants.ERROR.TIMEOUT))
            } catch (e: Exception) {
                throw e
                _events.emit(CourierEvent.Error(e.message))
            }
        }
    }

    val viewState: StateFlow<CourierViewState> = _events
        .onStart {
            emit(CourierEvent.RefreshCouriers)
        }
        .runningFold(CourierViewState()) { state, event ->
            when (event) {
                CourierEvent.BanUser -> {
                    val user = state.user
                    if (user != null) state.copy(user = user.copy(isBan = !user.isBan)) else state
                }

                CourierEvent.CreateUser -> {
                    launchCoroutine { createUser() }
                    state
                }

                CourierEvent.DeleteUser -> {
                    launchCoroutine { deleteUser() }
                    state
                }

                CourierEvent.RefreshCouriers -> {
                    launchCoroutine { loadCouriers() }
                    state.copy(isLoading = true)
                }

                CourierEvent.UpdateSettings -> {
                    launchCoroutine { updateSettings() }
                    state
                }

                CourierEvent.UpdateUser -> {
                    launchCoroutine { updateUser() }
                    state
                }

                is CourierEvent.ChangeUserPercent -> state.copy(userPercent = event.percent)
                is CourierEvent.ChangeUserSalary -> state.copy(userSalary = event.salary)
                is CourierEvent.ChangeUsername -> state.copy(userName = event.name)
                is CourierEvent.ChangedEmailUser -> state.copy(userEmail = event.email)
                is CourierEvent.ChangedPassUser -> state.copy(userPass = event.pass)
                is CourierEvent.ChangedPrice -> state.copy(priceMillage = event.price)
                is CourierEvent.ChangedSalary -> state.copy(salaryChange = event.salary)
                is CourierEvent.CouriersLoaded -> state.copy(couriers = event.couriers, isLoading = false)

                is CourierEvent.SelectedItemMenu -> state.copy(userRole = event.role)

                CourierEvent.ToggleAddDialog -> state.copy(toggleAddDialog = !state.toggleAddDialog)
                is CourierEvent.ToggleBanUser -> state.copy(user = event.user, toggleBanDialog = !state.toggleBanDialog)
                is CourierEvent.ToggleDeleteDialog -> state.copy(user = event.user, toggleDeleteCourier = !state.toggleDeleteCourier)
                is CourierEvent.ToggleUpdateDialog -> state.copy(user = event.user, toggleUpdateCourier = !state.toggleUpdateCourier)
                CourierEvent.ToggleUpdateSettingsDialog -> {
                    val factory = sharedViewModel.viewState.value.factory
                    if (factory != null) state.copy(factory = factory, toggleUpdateSettings = !state.toggleUpdateSettings) else state
                }

                is CourierEvent.Error -> {
                    sharedViewModel.message(event.message, type = TypeMessageModel.ERROR)
                    state.copy(isLoading = false)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CourierViewState())

    fun obtainEvent(event: CourierEvent) {
        _events.tryEmit(event)
    }

    private suspend fun loadCouriers() {
        val user = sharedViewModel.viewState.value.user ?: run {
            _events.emit(CourierEvent.Error(Constants.ERROR.AGAIN))
            return
        }

        when (val res = repository.getUsers(idFactory = user.idFactory)) {
            is MyResult.Success -> _events.emit(CourierEvent.CouriersLoaded(couriers = res.data))
            is MyResult.Error -> _events.emit(CourierEvent.Error(message = res.message))
        }
    }

    private suspend fun deleteUser() {
        val user = viewState.value.user ?: return
        val userSignIn = sharedViewModel.viewState.value.user ?: return
        if (user.id == userSignIn.id) {
            sharedViewModel.message(message = Constants.ERROR.RESRTRAINT, type = TypeMessageModel.ERROR)
            return
        }

        when (val res = repository.delete(id = user.id)) {
            is MyResult.Success -> {
                _events.emit(CourierEvent.RefreshCouriers)
                _events.emit(CourierEvent.ToggleDeleteDialog(user = null))
            }

            is MyResult.Error -> _events.emit(CourierEvent.Error(message = res.message))
        }
    }

    private suspend fun updateSettings() {
        val factory = viewState.value.factory ?: return

        val salary = viewState.value.salaryChange.ifBlank { factory.priceMillage.toString() }.toDouble()
        val price = viewState.value.priceMillage.ifBlank { factory.priceMillage.toString() }.toDouble()

        val request = FactoryRequest(
            id = factory.id,
            name = factory.name,
            dateAdd = factory.dateAdd,
            salary = salary,
            priceMillage = price
        )

        when (val res = repository.updateFactory(factory = request)) {
            is MyResult.Success -> {
                val newFactory = factory.copy(salary = salary, priceMillage = price)
                sharedViewModel.updateFactory(factory = newFactory)
                room.factoryDao().updateFactory(factory = newFactory)
                _events.emit(CourierEvent.ToggleUpdateSettingsDialog)
            }

            is MyResult.Error -> sharedViewModel.message(res.message, type = TypeMessageModel.ERROR)
        }
    }


    private suspend fun updateUser() {
        var user = viewState.value.user ?: return
        val userSignIn = sharedViewModel.viewState.value.user ?: return

        if (user.id == userSignIn.id) {
            sharedViewModel.message(Constants.ERROR.RESRTRAINT, type = TypeMessageModel.ERROR)
            return
        }

        val name = viewState.value.userName.ifBlank { user.name }
        val salary = viewState.value.userSalary.ifBlank { user.salary.toString() }.toDouble()
        val percent = viewState.value.userPercent.ifBlank { user.percentSalary.toString() }.toDouble()
        val role = viewState.value.userRole ?: user.role

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
                _events.emit(CourierEvent.RefreshCouriers)
                _events.emit(CourierEvent.ToggleUpdateDialog(null))
            }
            is MyResult.Error -> sharedViewModel.message(res.message, type = TypeMessageModel.ERROR)
        }
    }

    private suspend fun createUser() {
        val user = sharedViewModel.viewState.value.user ?: return

        val email = viewState.value.userEmail.ifBlank { return }
        val pass = viewState.value.userPass.ifBlank { return }
        val name = viewState.value.userName.ifBlank { "Пользователь #${viewState.value.couriers.size}" }

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
                _events.emit(CourierEvent.RefreshCouriers)
                _events.emit(CourierEvent.ToggleAddDialog)
            }

            is MyResult.Error -> sharedViewModel.message(res.message, type = TypeMessageModel.ERROR)
        }
    }
}