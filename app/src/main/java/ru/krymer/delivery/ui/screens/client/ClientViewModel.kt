package ru.krymer.delivery.ui.screens.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.krymer.delivery.data.model.ClientModel
import ru.krymer.delivery.data.model.RouteModel
import ru.krymer.delivery.data.model.utilModel.TypeMessageModel
import ru.krymer.delivery.data.repositoryImpl.ClientRepositoryImpl
import ru.krymer.delivery.data.request.ClientRequest
import ru.krymer.delivery.ui.screens.client.models.ClientEvent
import ru.krymer.delivery.ui.screens.client.models.ClientViewState
import ru.krymer.delivery.ui.screens.product.models.ProductEvent
import ru.krymer.delivery.ui.screens.route.models.RouteEvent
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import ru.krymer.delivery.utills.Constants
import ru.krymer.delivery.utills.MyResult
import javax.inject.Inject

@HiltViewModel
class ClientViewModel @Inject constructor(
    private val sharedViewModel: SharedViewModel, private val repository: ClientRepositoryImpl
) : ViewModel() {

    private val _events = MutableSharedFlow<ClientEvent>(extraBufferCapacity = 64)

    private fun launchCoroutine(block: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                block()
            } catch (e: CancellationException) {
                _events.emit(ClientEvent.Error(Constants.ERROR.CANCEL_OPERATION))
                throw e
            } catch (e: TimeoutCancellationException) {
                _events.emit(ClientEvent.Error(Constants.ERROR.TIMEOUT))
                throw e
            } catch (e: Exception) {
                _events.emit(ClientEvent.Error(e.message))
                throw e
            }
        }
    }

    val viewState: StateFlow<ClientViewState> = _events
        .runningFold(ClientViewState()) { state, event ->
            when (event) {
                is ClientEvent.Initialize -> {
                    val newState = state.copy(
                        route = event.route,
                        listRoute = event.routes,
                        isLoading = true
                    )
                    launchCoroutine {
                        loadClients(route = event.route)
                    }
                    newState
                }

                is ClientEvent.RefreshClients -> {
                    val currentRoute = state.route
                    if (currentRoute == null) {
                        _events.emit(ClientEvent.Error(message = Constants.ERROR.AGAIN))
                        state.copy(isLoading = false)
                    } else {
                        launchCoroutine { loadClients(route = currentRoute) }
                        state.copy(isLoading = true)
                    }
                }

                is ClientEvent.ClientsLoaded -> {
                    state.copy(isLoading = false, clients = event.clients)
                }

                is ClientEvent.ToggleAddDialog -> {
                    state.copy(toggleAddDialog = !state.toggleAddDialog)
                }

                is ClientEvent.ToggleUpdateDialog -> {
                    state.copy(
                        client = event.client,
                        selectedRoute = event.client?.let { client ->
                            state.listRoute.firstOrNull { it.id == client.idRoute }
                        },
                        toggleUpdateDialog = !state.toggleUpdateDialog,
                    )
                }

                is ClientEvent.ToggleDeleteDialog -> {
                    state.copy(
                        client = event.client,
                        toggleDeleteDialog = !state.toggleDeleteDialog,
                    )
                }

                is ClientEvent.ChangeNameClient -> {
                    state.copy(name = event.name)
                }

                is ClientEvent.ChangeArrearsClient -> {
                    state.copy(arrears = event.arrears)
                }

                is ClientEvent.ChangePhoneClient -> {
                    state.copy(phone = event.phone)
                }

                is ClientEvent.ChangeCordClient -> {
                    state.copy(cords = event.cords)
                }

                is ClientEvent.SelectedItemMenu -> {
                    state.copy(selectedRoute = event.route)
                }

                is ClientEvent.CreateClient -> {
                    launchCoroutine { createClient() }
                    state
                }

                is ClientEvent.UpdateClient -> {
                    launchCoroutine { updateClient() }
                    state
                }

                is ClientEvent.DeleteClient -> {
                    launchCoroutine { deleteClient() }
                    state
                }

                is ClientEvent.ReorderClients -> {
                    launchCoroutine { reorderClients(clients = event.list) }
                    state
                }

                is ClientEvent.Error -> {
                    sharedViewModel.message(message = event.message)
                    state
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ClientViewState())

    fun obtainEvent(event: ClientEvent) {
        _events.tryEmit(event)
    }

    private suspend fun loadClients(route: RouteModel) {
        when (val res = repository.getClients(route.id)) {
            is MyResult.Success -> _events.emit(ClientEvent.ClientsLoaded(res.data))
            is MyResult.Error -> {
                _events.emit(ClientEvent.Error(message = res.message))
                _events.emit(ClientEvent.ClientsLoaded(emptyList()))
            }
        }
    }

    private suspend fun createClient() {
        val state = viewState.value
        val route = state.route ?: run {
            _events.emit(ClientEvent.Error(message = Constants.ERROR.AGAIN))
            return
        }

        val name = state.name.ifBlank {
            "Магазин без имени ${state.clients.lastOrNull()?.counter?.plus(1) ?: 0}"
        }

        val phone = state.phone.trim()
        val arrears = state.arrears.toDoubleOrNull() ?: 0.0
        val cords = state.cords.trim()
        val counter = state.clients.lastOrNull()?.counter?.plus(1) ?: 0

        val request = ClientRequest(
            idRoute = route.id,
            idFactory = route.idFactory,
            name = name,
            phone = phone,
            cord = cords,
            counter = counter,
            arrears = arrears,
            date = System.currentTimeMillis()
        )

        when (val res = repository.addClient(request)) {
            is MyResult.Success -> {
                _events.emit(ClientEvent.ToggleAddDialog)
                _events.emit(ClientEvent.RefreshClients)
            }

            is MyResult.Error -> _events.emit(ClientEvent.Error(message = res.message))

        }
    }

    private suspend fun updateClient() {
        val state = viewState.value
        val client = state.client ?: return
        val selectedRoute = state.selectedRoute ?: return

        val request = ClientRequest(
            id = client.id,
            idRoute = selectedRoute.id,
            idFactory = client.idFactory,
            name = state.name.ifBlank { client.name },
            phone = state.phone.ifBlank { client.phone },
            cord = state.cords.ifBlank { client.cord },
            counter = client.counter,
            arrears = state.arrears.toDoubleOrNull() ?: client.arrears,
            date = client.date
        )

        when (val res = repository.updateClient(request)) {
            is MyResult.Success -> {
                _events.emit(ClientEvent.ToggleUpdateDialog(client = null))
                _events.emit(ClientEvent.RefreshClients)
            }

            is MyResult.Error -> _events.emit(ClientEvent.Error(message = res.message))
        }
    }

    private suspend fun deleteClient() {
        val state = viewState.value
        val client = state.client ?: return

        when (val res = repository.deleteClient(client.id)) {
            is MyResult.Success -> {
                _events.emit(ClientEvent.ToggleDeleteDialog(client = null))
                _events.emit(ClientEvent.RefreshClients)
            }

            is MyResult.Error -> _events.emit(ClientEvent.Error(message = res.message))
        }
    }

    private suspend fun reorderClients(clients: List<ClientModel>) {
        val requests = clients.map { c ->
            ClientRequest(
                id = c.id,
                idRoute = c.idRoute,
                idFactory = c.idFactory,
                name = c.name,
                phone = c.phone,
                cord = c.cord,
                counter = c.counter,
                arrears = c.arrears,
                date = c.date
            )
        }

        when (val res = repository.moves(requests = requests)) {
            is MyResult.Error -> _events.emit(ClientEvent.Error(message = res.message))
            is MyResult.Success -> Unit

        }
    }
}
