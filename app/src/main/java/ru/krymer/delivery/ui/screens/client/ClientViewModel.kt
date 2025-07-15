package ru.krymer.delivery.ui.screens.client

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.krymer.delivery.common.EventHandler
import ru.krymer.delivery.data.api.ClientApi
import ru.krymer.delivery.data.model.ClientModel
import ru.krymer.delivery.data.model.RouteModel
import ru.krymer.delivery.data.model.utilModel.TypeMessageModel
import ru.krymer.delivery.data.request.ClientRequest
import ru.krymer.delivery.ui.screens.client.models.ClientAction
import ru.krymer.delivery.ui.screens.client.models.ClientEvent
import ru.krymer.delivery.ui.screens.client.models.ClientViewState
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import ru.krymer.delivery.utills.Constants
import ru.krymer.delivery.utills.convertToTextDate
import ru.krymer.delivery.utills.getStartOfNextDay
import javax.inject.Inject

@HiltViewModel
class ClientViewModel @Inject constructor(
    private val sharedViewModel: SharedViewModel,
    private val clientApi: ClientApi
) : ViewModel(), EventHandler<ClientEvent> {

    private val _viewState = MutableStateFlow(ClientViewState())
    val viewState = _viewState.asStateFlow()

    private fun updateViewState(update: (ClientViewState) -> ClientViewState) {
        _viewState.update { update(it) }
    }

    private fun launchCoroutine(block: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                block()
            } catch (e: CancellationException) {
                sharedViewModel.message(Constants.ERROR.CANCEL_OPERATION, type = TypeMessageModel.ERROR)
            } catch (e: Exception) {
                sharedViewModel.message(e.message, type = TypeMessageModel.ERROR)
            }
        }
    }

    override fun obtainEvent(event: ClientEvent) {
        when (event) {
            is ClientEvent.ClientAddAction -> saveClient()
            is ClientEvent.ShowAddDialog -> showAddDialog()
            is ClientEvent.ShowDeleteDialog -> showDeleteDialog(
                client = event.client
            )
            is ClientEvent.ShowUpdateDialog -> showUpdateDialog(event.client)
            is ClientEvent.ClientUpdateAction -> updateClient()
            is ClientEvent.ChangeArrearsClient -> changesAddArrears(event.arrears)
            is ClientEvent.ChangeCordClient -> changesAddCords(event.cords)
            is ClientEvent.ChangeNameClient -> changesAddName(event.name)
            is ClientEvent.ChangePhoneClient -> changesAddPhone(event.phone)
            is ClientEvent.DropDownMenuState -> changeStateDropMenu(event.state)
            is ClientEvent.SelectedItemMenu -> changeParamsRouteInClient(event.route)
            is ClientEvent.DismissAddDialog -> dismissDialogs()
            is ClientEvent.DismissDeleteDialog -> dismissDialogs()
            is ClientEvent.DismissUpdateDialog -> dismissDialogs()
            is ClientEvent.DeleteClient -> deleteItemConfirmed()
            is ClientEvent.ReorderClients -> {
                reorderClients(toIndex = event.toIndex, fromIndex = event.fromIndex)
            }
        }
    }

    private fun reorderClients(toIndex: Int, fromIndex: Int) {
        launchCoroutine {
            val currentList = viewState.value.listClient.value.toMutableList()
            if (fromIndex !in currentList.indices || toIndex !in currentList.indices) return@launchCoroutine

            val movedItem = currentList[fromIndex]
            val targetItem = currentList[toIndex]

            val tempCounter = movedItem.counter
            movedItem.counter = targetItem.counter
            targetItem.counter = tempCounter

            currentList.removeAt(fromIndex)
            currentList.add(toIndex, movedItem)

            updateViewState { it.copy(listClient = MutableStateFlow(currentList)) }
            val updateMovedItem = movedItem.toRequestUpdateIndex(movedItem.counter)
            val updateTargetItem = targetItem.toRequestUpdateIndex(targetItem.counter)

            val responseMoved = clientApi.update(client = updateMovedItem)
            val responseTarget = clientApi.update(client = updateTargetItem)

            if (!responseMoved.success || !responseTarget.success) {
                sharedViewModel.message(Constants.ERROR.AGAIN, type = TypeMessageModel.ERROR)
            }
        }
    }

    fun saveListRoute(list: List<RouteModel>) {
        updateViewState { it.copy(listRoute = MutableStateFlow(list)) }
    }

    fun getDataClients(route: RouteModel) {
        launchCoroutine {
            val response = clientApi.getClientsByRoute(
                idRoute = route.id
            )
            if (response.success) {
                val clients = response.obj
                if (clients != null) {
                    updateViewState {
                        it.copy(
                            listClient = MutableStateFlow(clients),
                            currentRoute = MutableStateFlow(route)
                        )
                    }
                } else {
                    updateViewState {
                        it.copy(
                        listClient = MutableStateFlow(emptyList()),
                        currentRoute = MutableStateFlow(route)
                    ) }
                }
            } else {
                sharedViewModel.message(response.message, type = TypeMessageModel.ERROR)
            }
        }
    }

    private fun changeParamsRouteInClient(route: RouteModel?) {
        if (route != null) {
            updateViewState { it.copy(selectedRoute = MutableStateFlow(route)) }
        }
    }

    private fun ClientModel.toRequestUpdateIndex(index: Int): ClientRequest {
        return ClientRequest(
            id = id,
            idRoute = idRoute,
            idFactory = idFactory,
            name = name,
            phone = phone,
            cord = cord,
            counter = index,
            arrears = arrears,
            date = date
        )
    }


    private fun changeStateDropMenu(state: Boolean) {
        updateViewState { it.copy(dropDownState = state) }
    }

    private fun changesAddArrears(arrears: String) {
        updateViewState {
            it.copy(
                arrears = arrears
            )
        }
    }

    private fun changesAddName(name: String = Constants.EMPTY.EMPTY_STRING) {
        updateViewState {
            it.copy(
                name = name
            )
        }
    }

    private fun changesAddPhone(phone: String) {
        updateViewState { it.copy(phone = phone) }
    }

    private fun changesAddCords(cords: String) {
        updateViewState { it.copy(cords = cords) }
    }


    private fun saveClient() {
        launchCoroutine {
            val name = viewState.value.name
            val phone = viewState.value.phone
            val arrears = if (viewState.value.arrears == "") 0.0 else viewState.value.arrears.toDouble()
            val cords = viewState.value.cords
            val sizeList = if (viewState.value.listClient.value.isNotEmpty())  viewState.value.listClient.value.last().counter+1 else 0
            val route = viewState.value.currentRoute.value
            if (route != null) {
                val clientRequest = ClientRequest(
                    idRoute = route.id,
                    idFactory = route.idFactory,
                    name = name, phone = phone, cord = cords,
                    counter = sizeList, arrears = arrears,
                    date = System.currentTimeMillis()
                )
                val response = clientApi.add(client = clientRequest)
                if (response.success) {
                    val client = response.obj
                    if (client != null) {
                        val list =
                            viewState.value.listClient.value.map { it.copy() }.toMutableList()
                        list.add(client)
                        updateViewState { it.copy(listClient = MutableStateFlow(list.sortedBy { c -> c.counter })) }
                        dismissDialogs()
                    }
                } else {
                    sharedViewModel.message(response.message, type = TypeMessageModel.ERROR)
                }
            } else {
                sharedViewModel.message(Constants.ERROR.AGAIN, type = TypeMessageModel.ERROR)
            }
        }
    }


    private fun showAddDialog() {
        updateViewState {
            it.copy(
                isDialogAdd = true
            )
        }
    }

    private fun dismissDialogs() {
        updateViewState {
            it.copy(
                isDialogDelete = false,
                isDialogUpdate = false,
                isDialogAdd = false,
                clientUpdate = null,
                clientDelete = null,
                name = Constants.EMPTY.EMPTY_STRING,
                cords = Constants.EMPTY.EMPTY_STRING,
                phone = Constants.EMPTY.EMPTY_STRING,
                arrears = Constants.EMPTY.EMPTY_STRING
            )
        }
    }

    private fun showDeleteDialog(client: ClientModel) {
        updateViewState {
            it.copy(
                isDialogDelete = true, clientDelete = client
            )
        }
    }

    fun deleteItemConfirmed() {
        launchCoroutine {
            val client = viewState.value.clientDelete
            if (client != null) {
                val response = clientApi.delete(id = client.id)
                if (response.success) {
                    val list =
                        viewState.value.listClient.value.map { it.copy() }.toMutableList()
                    val item = list.first { it.id == client.id }
                    val listNew = list - item
                    updateViewState { it.copy(listClient = MutableStateFlow(listNew.sortedBy { c -> c.counter })) }
                    dismissDialogs()
                } else {
                    sharedViewModel.message(response.message, type = TypeMessageModel.ERROR)
                }
            } else {
                sharedViewModel.message(Constants.ERROR.AGAIN, type = TypeMessageModel.ERROR)
            }
        }
    }

    private fun showUpdateDialog(client: ClientModel) {
        launchCoroutine {
            val route = viewState.value.listRoute.value.first {
                it.id == client.idRoute
            }
            updateViewState {
                it.copy(
                    selectedRoute = MutableStateFlow(route),
                    isDialogUpdate = true,
                    clientUpdate = client,
                    name = client.name,
                    arrears = "${client.arrears.toInt()}",
                    phone = client.phone,
                    cords = client.cord
                )
            }
        }
    }

    private fun updateClient() {
        launchCoroutine {
            val client = viewState.value.clientUpdate
            val name = viewState.value.name
            val phone = viewState.value.phone
            val arrears = if (viewState.value.arrears == "") 0.0 else viewState.value.arrears.toDouble()
            val cords = viewState.value.cords
            val route = viewState.value.selectedRoute.value
            if (client != null && route != null) {
                val request = ClientRequest(
                    id = client.id,
                    idRoute = route.id,
                    idFactory = client.idFactory,
                    name = if (name == "") client.name else name,
                    phone = phone,
                    cord = cords,
                    counter = client.counter,
                    arrears = arrears,
                    date = client.date
                )
                val response = clientApi.update(client = request)
                if (response.success) {
                    val list =
                        viewState.value.listClient.value.map { it.copy() }.toMutableList()
                    val index = list.indexOfFirst { it.id == client.id }
                    if (client.idRoute == route.id) {
                        val updatedClient = client.copy(
                            name = name,
                            phone = phone,
                            arrears = arrears,
                            cord = cords
                        )
                        list[index] = updatedClient
                        updateViewState { it.copy(listClient = MutableStateFlow(list)) }
                    } else {
                        val item = list.first { it.id == client.id }
                        val listNew = list - item
                        updateViewState { it.copy(listClient = MutableStateFlow(listNew.sortedBy { c -> c.counter })) }
                    }
                    dismissDialogs()
                } else {
                    sharedViewModel.message(response.message, type = TypeMessageModel.ERROR)
                }
            } else {
                sharedViewModel.message(Constants.ERROR.AGAIN, type = TypeMessageModel.ERROR)
            }
        }
    }
}