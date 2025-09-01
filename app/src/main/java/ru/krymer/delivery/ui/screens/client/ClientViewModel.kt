package ru.krymer.delivery.ui.screens.client

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
import ru.krymer.delivery.data.model.ClientModel
import ru.krymer.delivery.data.model.RouteModel
import ru.krymer.delivery.data.model.utilModel.TypeMessageModel
import ru.krymer.delivery.data.repositoryImpl.ClientRepositoryImpl
import ru.krymer.delivery.data.request.ClientRequest
import ru.krymer.delivery.ui.screens.client.models.ClientEvent
import ru.krymer.delivery.ui.screens.client.models.ClientViewState
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import ru.krymer.delivery.utills.Constants
import ru.krymer.delivery.utills.MyResult
import javax.inject.Inject

@HiltViewModel
class ClientViewModel @Inject constructor(
    private val sharedViewModel: SharedViewModel, private val repository: ClientRepositoryImpl
) : ViewModel(), EventHandler<ClientEvent> {

    private val _viewState = MutableStateFlow(ClientViewState())
    val viewState = _viewState.asStateFlow()

    private fun updateState(update: (ClientViewState) -> ClientViewState) {
        _viewState.update { update(it) }
    }

    private fun launchCoroutine(block: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
                sharedViewModel.message(
                    Constants.ERROR.CANCEL_OPERATION, type = TypeMessageModel.ERROR
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


    override fun obtainEvent(event: ClientEvent) {
        when (event) {

            is ClientEvent.ToggleAddDialog -> setState(delete = !viewState.value.toggleAddDialog)
            is ClientEvent.ToggleDeleteDialog -> setState(
                delete = !viewState.value.toggleDeleteDialog,
                client = event.client
            )

            is ClientEvent.ToggleUpdateDialog -> setState(
                update = !viewState.value.toggleUpdateDialog, client = event.client
            )

            is ClientEvent.ClientUpdateAction -> updateClient()
            is ClientEvent.ClientAddAction -> createClient()
            is ClientEvent.DeleteClient -> deleteClient()

            is ClientEvent.ChangeArrearsClient -> setValue(arrears = event.arrears)
            is ClientEvent.ChangeCordClient -> setValue(cords = event.cords)
            is ClientEvent.ChangeNameClient -> setValue(name = event.name)
            is ClientEvent.ChangePhoneClient -> setValue(phone = event.phone)
            is ClientEvent.SelectedItemMenu -> setValue(route = event.route)

            is ClientEvent.ReorderClients -> {
                reorderClients(list = event.list)
            }
        }
    }

    private fun setState(
        add: Boolean = viewState.value.toggleAddDialog,
        update: Boolean = viewState.value.toggleUpdateDialog,
        delete: Boolean = viewState.value.toggleDeleteDialog,
        client: ClientModel? = viewState.value.client
    ) {
        if (update && client != null) {
            val route = viewState.value.listRoute.first {
                it.id == client.idRoute
            }
            updateState { it.copy(selectedRoute = route) }
        }
        updateState {
            it.copy(
                toggleAddDialog = add,
                toggleUpdateDialog = update,
                toggleDeleteDialog = delete,
                client = client
            )
        }
    }

    private fun reorderClients(list: List<ClientModel>) = launchCoroutine {
            val requests = list.map { client -> ClientRequest(
                id = client.id,
                idRoute = client.idRoute,
                idFactory = client.idFactory,
                name = client.name,
                phone = client.phone,
                cord = client.cord,
                counter = client.counter,
                arrears = client.arrears,
                date = client.date
            ) }
        when (val res = repository.moves(requests = requests)) {
            is MyResult.Error -> sharedViewModel.message(res.message, type = TypeMessageModel.ERROR)
            is MyResult.Success -> {}
        }
    }

    fun saveListRoute(list: List<RouteModel>) {
        updateState { it.copy(listRoute = list) }
    }

    fun getDataClients(route: RouteModel) = launchCoroutine {
        updateState { it.copy(isLoading = true, route = route) }

        when (val res = repository.getClients(idRoute = route.id)) {
            is MyResult.Success -> updateState { it.copy(clients = res.data, isLoading = false) }
            is MyResult.Error -> {
                updateState { it.copy(isLoading = false) }
                sharedViewModel.message(res.message, type = TypeMessageModel.ERROR)
            }
        }
    }


    private fun createClient() = launchCoroutine {
        val route = viewState.value.route
        if (route == null) {
            sharedViewModel.message(Constants.ERROR.AGAIN, type = TypeMessageModel.ERROR)
            return@launchCoroutine
        }
        val name =
            viewState.value.name.ifBlank { "Магазин без имени ${viewState.value.clients.last().counter + 1}" }
        val phone = viewState.value.phone.trim()
        val arrears =
            if (viewState.value.arrears == "") 0.0 else viewState.value.arrears.trim().toDouble()
        val cords = viewState.value.cords.trim()
        val sizeList =
            if (viewState.value.clients.isNotEmpty()) viewState.value.clients.last().counter + 1 else 0

        val request = ClientRequest(
            idRoute = route.id,
            idFactory = route.idFactory,
            name = name,
            phone = phone,
            cord = cords,
            counter = sizeList,
            arrears = arrears,
            date = System.currentTimeMillis()
        )

        when (val res = repository.addClient(request)) {
            is MyResult.Success -> {
                getDataClients(route = route)
                setState(add = false)
                setValue(name = "", phone = "", route = null, cords = "", arrears = "")
            }

            is MyResult.Error -> sharedViewModel.message(res.message, type = TypeMessageModel.ERROR)
        }
    }

    private fun setValue(
        name: String = viewState.value.name,
        cords: String = viewState.value.cords,
        phone: String = viewState.value.phone,
        route: RouteModel? = viewState.value.selectedRoute,
        arrears: String = viewState.value.arrears
    ) {
        updateState {
            it.copy(
                name = name, cords = cords, phone = phone, selectedRoute = route, arrears = arrears
            )
        }
    }

    fun deleteClient() = launchCoroutine {
        val client = viewState.value.client
        val route = viewState.value.route

        if (client == null) {
            sharedViewModel.message(Constants.ERROR.AGAIN, type = TypeMessageModel.ERROR)
            return@launchCoroutine
        }

        if (route == null) {
            sharedViewModel.message(Constants.ERROR.AGAIN, type = TypeMessageModel.ERROR)
            return@launchCoroutine
        }

        when (val res = repository.deleteClient(client.id)) {
            is MyResult.Success -> {
                getDataClients(route = route)
                setState(delete = false, client = null)
            }

            is MyResult.Error -> sharedViewModel.message(res.message, type = TypeMessageModel.ERROR)
        }
    }

    private fun updateClient() = launchCoroutine {
        val client = viewState.value.client
        val route = viewState.value.route
        val selectedRoute = viewState.value.selectedRoute

        if (client == null) {
            sharedViewModel.message(Constants.ERROR.AGAIN, type = TypeMessageModel.ERROR)
            return@launchCoroutine
        }

        if (selectedRoute == null) {
            sharedViewModel.message(Constants.ERROR.AGAIN, type = TypeMessageModel.ERROR)
            return@launchCoroutine
        }

        if (route == null) {
            sharedViewModel.message(Constants.ERROR.AGAIN, type = TypeMessageModel.ERROR)
            return@launchCoroutine
        }

        val name =
            viewState.value.name.ifBlank { client.name }
        val phone = viewState.value.phone.trim()
        val arrears = if (viewState.value.arrears == "") 0.0 else viewState.value.arrears.trim().toDouble()
        val cords = viewState.value.cords.trim()

        val request = ClientRequest(
            id = client.id,
            idRoute = selectedRoute.id,
            idFactory = client.idFactory,
            name = name,
            phone = phone,
            cord = cords,
            counter = client.counter,
            arrears = arrears,
            date = client.date
        )

        when (val res = repository.updateClient(request = request)) {
            is MyResult.Success -> {
                getDataClients(route)
                setState(update = false, client = null)
            }
            is MyResult.Error -> sharedViewModel.message(res.message, type = TypeMessageModel.ERROR)
        }
    }
}
