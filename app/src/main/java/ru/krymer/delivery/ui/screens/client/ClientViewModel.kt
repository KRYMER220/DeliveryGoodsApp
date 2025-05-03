package ru.krymer.delivery.ui.screens.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.krymer.delivery.common.EventHandler
import ru.krymer.delivery.data.api.ClientApi
import ru.krymer.delivery.data.api.LoggerApi
import ru.krymer.delivery.data.model.ClientModel
import ru.krymer.delivery.data.model.RouteModel
import ru.krymer.delivery.data.request.ClientRequest
import ru.krymer.delivery.ui.screens.client.models.ClientAction
import ru.krymer.delivery.ui.screens.client.models.ClientEvent
import ru.krymer.delivery.ui.screens.client.models.ClientShopViewState
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import ru.krymer.delivery.utills.Constants
import javax.inject.Inject

@HiltViewModel
class ClientViewModel @Inject constructor(
    private val sharedViewModel: SharedViewModel,
    private val clientApi: ClientApi
) : ViewModel(), EventHandler<ClientEvent> {

    private val _viewState = MutableStateFlow(ClientShopViewState())
    val viewState: StateFlow<ClientShopViewState> = _viewState

    private fun updateViewState(update: (ClientShopViewState) -> ClientShopViewState) {
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

    override fun obtainEvent(event: ClientEvent) {
        when (event) {
            is ClientEvent.ClientActionInvoked -> clientActionInvoked()
            is ClientEvent.ClientAddAction -> saveClient()
            is ClientEvent.ShowAddDialog -> showAddDialog()
            is ClientEvent.ShowDeleteDialog -> showDeleteDialog(
                client = event.client
            )
            is ClientEvent.ShowUpdateDialog -> showUpdateDialog(event.route, event.client)
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
            is ClientEvent.DownItemIndex -> if (sharedViewModel.initSysAdmMod()) changePosClientInListOnDown(
                event.index
            )
            is ClientEvent.UpItemIndex -> if (sharedViewModel.initSysAdmMod()) changePosClientInListOnUp(
                event.index
            )
        }
    }

    init {
        getDataClients()
    }

    private fun getDataClients() {
        launchCoroutine {
            val route = sharedViewModel.viewState.value.currentRoute
            if (route != null) {
                val response = clientApi.getClientsByRoute(
                    idRoute = route.id
                )
                if (response.success) {
                    val clients = response.obj
                    if (clients != null) {
                        updateViewState {
                            it.copy(
                                listClient = MutableStateFlow(clients)
                            )
                        }
                    } else {
                        delay(5000)
                        getDataClients()
                    }
                } else {
                    sharedViewModel.message(response.message)
                }
            }
        }
    }

    private fun changeParamsRouteInClient(route: RouteModel?) {
        if (route != null) {
            updateViewState { it.copy(selectedRoute = route) }
        }
    }

    private fun changePosClientInListOnUp(fromIndex: Int) {
        launchCoroutine {
            val list = _viewState.value.listClient.value
            if (fromIndex in 1..<list.size) {
                val itemFrom = list[fromIndex].copy()
                val indexFrom = itemFrom.counter
                val itemTo = list[fromIndex - 1].copy()
                val indexTo = itemTo.counter
                val newItemFrom = itemTo.toRequestUpdateIndex(indexFrom)
                val newItemTo = itemFrom.toRequestUpdateIndex(indexTo)
                val responseFrom = clientApi.update(client = newItemFrom)
                val responseTo = clientApi.update(client = newItemTo)
                if (responseTo.success && responseFrom.success) {
                    val updatedList = list.map { it.copy() }.toMutableList()
                    updatedList[fromIndex] = itemTo.copy(counter = indexFrom)
                    updatedList[fromIndex - 1] = itemFrom.copy(counter = indexTo)
                    updateViewState { it.copy(listClient = MutableStateFlow(updatedList)) }
                } else {
                    sharedViewModel.message(Constants.ERROR.SERVER_ERROR_RESPONSE)
                }
            } else {
                sharedViewModel.message(Constants.ERROR.GENERAL_ERROR)
            }
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

    private fun changePosClientInListOnDown(fromIndex: Int) {
        launchCoroutine {
            val list = _viewState.value.listClient.value.map { it.copy() }
            if (fromIndex in 0..list.size - 2) {
                val itemFrom = list[fromIndex].copy()
                val itemTo = list[fromIndex + 1].copy()
                val indexFrom = itemFrom.counter
                val indexTo = itemTo.counter
                val newItemFrom = itemTo.toRequestUpdateIndex(indexFrom)
                val newItemTo = itemFrom.toRequestUpdateIndex(indexTo)
                val responseFrom = clientApi.update(client = newItemFrom)
                val responseTo = clientApi.update(client = newItemTo)
                if (responseTo.success && responseFrom.success) {
                    val updatedList = list.map { it.copy() }.toMutableList()
                    updatedList[fromIndex] = itemTo.copy(counter = indexFrom)
                    updatedList[fromIndex + 1] = itemFrom.copy(counter = indexTo)
                    updateViewState { it.copy(listClient = MutableStateFlow(updatedList)) }
                } else {
                    sharedViewModel.message(Constants.ERROR.SERVER_ERROR_RESPONSE)
                }
            } else {
                sharedViewModel.message(Constants.ERROR.GENERAL_ERROR)
            }
        }
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
            val route = sharedViewModel.viewState.value.currentRoute
            val name = viewState.value.name
            val phone = viewState.value.phone
            val arrears = if (viewState.value.arrears == "") 0.0 else viewState.value.arrears.toDouble()
            val cords = viewState.value.cords
            val sizeList = viewState.value.listClient.value.size
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
                    } else {
                        sharedViewModel.message(Constants.ERROR.SERVER_ERROR_RESPONSE)
                    }
                } else {
                    sharedViewModel.message(response.message)
                }
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
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val client = viewState.value.clientDelete
                if (client != null) {
                    val response = clientApi.delete(id = client.id)
                    if (response.success) {
                        val list =
                            viewState.value.listClient.value.map { it.copy() }.toMutableList()
                        val item = list.first { it.id == client.id }
                        val listNew = list - item
                        updateViewState { it.copy(listClient = MutableStateFlow(listNew.sortedBy { c -> c.counter })) }
                    } else {
                        sharedViewModel.message(response.message)
                    }
                } else {
                    sharedViewModel.message(Constants.ERROR.GENERAL_ERROR)
                }
            } catch (e: Exception) {
                sharedViewModel.message(e.message)
            } finally {
                dismissDialogs()
            }
        }
    }

    private fun showUpdateDialog(route: RouteModel?, client: ClientModel) {
        updateViewState {
            it.copy(
                listRoute = MutableStateFlow(sharedViewModel.getListRoute()),
            isDialogUpdate = true,
            selectedRoute = route,
            clientUpdate = client,
            name = client.name,
            arrears = "${client.arrears}",
            phone = client.phone,
                cords = client.cord
            )
        }
    }

    private fun updateClient() {
        launchCoroutine {
            val client = viewState.value.clientUpdate
            val name = viewState.value.name
            val phone = viewState.value.phone
            val arrears = if (viewState.value.arrears == "") 0.0 else viewState.value.arrears.toDouble()
            val cords = viewState.value.cords
            val route = viewState.value.selectedRoute
            if (client != null && route != null && name.isNotEmpty() && sharedViewModel.initSysAdmMod()) {
                val request = ClientRequest(
                    id = client.id,
                    idRoute = route.id,
                    idFactory = client.idFactory,
                    name = name,
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
                    sharedViewModel.message(response.message)
                }
            }
        }
    }

    private fun clientActionInvoked() {
        updateViewState { it.copy(clientAction = ClientAction.None) }
    }
}