package ru.krymer.delivery.ui.screens.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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
import ru.krymer.delivery.data.request.LogRequest
import ru.krymer.delivery.ui.screens.client.models.ClientAction
import ru.krymer.delivery.ui.screens.client.models.ClientEvent
import ru.krymer.delivery.ui.screens.client.models.ClientShopViewState
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import ru.krymer.delivery.utills.Constants
import ru.krymer.delivery.utills.findChangedFields
import ru.krymer.delivery.utills.isEmptyInput
import ru.krymer.delivery.utills.isValidCords
import ru.krymer.delivery.utills.isValidPhone
import javax.inject.Inject

@HiltViewModel
class ClientViewModel @Inject constructor(
    private val sharedViewModel: SharedViewModel,
    private val clientApi: ClientApi,
    private val loggerApi: LoggerApi
) : ViewModel(), EventHandler<ClientEvent> {

    private val _viewState = MutableStateFlow(ClientShopViewState())
    val viewState: StateFlow<ClientShopViewState> = _viewState

    private fun updateViewState(update: (ClientShopViewState) -> ClientShopViewState) {
        _viewState.update { update(it) }
    }

    override fun obtainEvent(event: ClientEvent) {
        when (event) {
            is ClientEvent.ClientActionInvoked -> clientActionInvoked()
            is ClientEvent.ClientAddAction -> saveClient()
            is ClientEvent.ShowAddDialog -> showAddDialog()
            is ClientEvent.ShowDeleteDialog -> showDeleteDialog(
                itemId = event.itemId, itemName = event.itemName
            )

            is ClientEvent.ShowUpdateDialog -> showUpdateDialog(event.route, event.client)
            is ClientEvent.ClientUpdateAction -> updateClient()
            is ClientEvent.ChangeArrearsClient -> changesAddArrears(event.arrears)
            is ClientEvent.ChangeCordClient -> changesAddCords(event.cords)
            is ClientEvent.ChangeNameClient -> changesAddName(event.name)
            is ClientEvent.ChangePhoneClient -> changesAddPhone(event.phone)
            is ClientEvent.DropDownMenuState -> changeStateDropMenu(event.state)
            is ClientEvent.SelectedItemMenu -> changeParamsRouteInClient(event.route)
            is ClientEvent.DismissAddDialog -> dismissAddDialog()
            is ClientEvent.DismissDeleteDialog -> dismissDeleteDialog()
            is ClientEvent.DismissUpdateDialog -> dismissUpdateDialog()
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
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val route = sharedViewModel.viewState.value.currentRoute
                if (route != null) {
                    val response = clientApi.getCurrentListClient(
                        idRoute = route.id
                    )
                    if (response.success) {
                        val clients = response.obj
                        if (clients != null) {
                            updateViewState {
                                it.copy(
                                    listClient = MutableStateFlow(clients), isLoadClientData = true
                                )
                            }
                        } else {
                            sharedViewModel.message(Constants.ERROR.SERVER_ERROR_RESPONSE)
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

    private fun changeParamsRouteInClient(route: RouteModel?) {
        if (route != null) {
            updateViewState { it.copy(selectedRoute = route) }
        }
    }

    private fun changePosClientInListOnUp(fromIndex: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = _viewState.value.listClient.value
            if (fromIndex in 1..<list.size) {
                try {
                    val itemFrom = list[fromIndex].copy()
                    val indexFrom = itemFrom.counter
                    val itemTo = list[fromIndex - 1].copy()
                    val indexTo = itemTo.counter
                    val newItemFrom = itemTo.toRequestUpdateIndex(indexFrom)
                    val newItemTo = itemFrom.toRequestUpdateIndex(indexTo)
                    val responseFrom = clientApi.updateClient(client = newItemFrom)
                    val responseTo = clientApi.updateClient(client = newItemTo)
                    if (responseTo.success && responseFrom.success) {
                        val updatedList = list.map { it.copy() }.toMutableList()
                        updatedList[fromIndex] = itemTo.copy(counter = indexFrom)
                        updatedList[fromIndex - 1] = itemFrom.copy(counter = indexTo)
                        updateViewState { it.copy(listClient = MutableStateFlow(updatedList)) }
                    } else {
                        sharedViewModel.message(
                            Constants.ERROR.SERVER_ERROR_RESPONSE
                        )
                    }
                } catch (e: Exception) {
                    sharedViewModel.message(e.message)
                }
            } else {
                sharedViewModel.message(Constants.ERROR.GENERAL_ERROR)
            }
        }
    }

    private fun ClientModel.toRequestUpdateIndex(index: Int): ClientRequest {
        return ClientRequest(
            id = this.id,
            idRoute = this.idRoute,
            idFactory = this.idFactory,
            name = this.name,
            phone = this.phone,
            cord = this.cord,
            counter = index,
            arrears = this.arrears,
            date = this.date,
        )
    }

    private fun changePosClientInListOnDown(fromIndex: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = _viewState.value.listClient.value.map { it.copy() }
            if (fromIndex in 0..list.size - 2) {
                try {
                    val itemFrom = list[fromIndex].copy()
                    val itemTo = list[fromIndex + 1].copy()
                    val indexFrom = itemFrom.counter
                    val indexTo = itemTo.counter
                    val newItemFrom = itemTo.toRequestUpdateIndex(indexFrom)
                    val newItemTo = itemFrom.toRequestUpdateIndex(indexTo)
                    val responseFrom = clientApi.updateClient(client = newItemFrom)
                    val responseTo = clientApi.updateClient(client = newItemTo)
                    if (responseTo.success && responseFrom.success) {
                        val updatedList = list.map { it.copy() }.toMutableList()
                        updatedList[fromIndex] = itemTo.copy(counter = indexFrom)
                        updatedList[fromIndex + 1] = itemFrom.copy(counter = indexTo)
                        updateViewState { it.copy(listClient = MutableStateFlow(updatedList)) }
                    } else {
                        sharedViewModel.message(
                            Constants.ERROR.SERVER_ERROR_RESPONSE
                        )
                    }
                } catch (e: Exception) {
                    sharedViewModel.message(e.message)
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
        if (isEmptyInput(arrears)) {
            updateViewState {
                it.copy(
                    arrears = arrears, isErrorArrears = false
                )
            }
        } else {
            updateViewState {
                it.copy(
                    isErrorArrears = true,
                    errorValue = Constants.EMPTY.EMPTY_ARREARS,
                    arrears = arrears
                )
            }
        }
    }

    private fun changesAddName(name: String = Constants.EMPTY.EMPTY_STRING) {
        if (isEmptyInput(name)) {
            updateViewState {
                it.copy(
                    name = name, isErrorName = false
                )
            }
        } else {
            updateViewState {
                it.copy(
                    isErrorName = true,
                    errorValue = Constants.EMPTY.FIELD_IMPORTANT + Constants.EMPTY.EMPTY_NAME,
                    name = name
                )
            }
        }
    }

    private fun changesAddPhone(phone: String) {
        if (isValidPhone(phone)) {
            updateViewState { it.copy(phone = phone, isErrorPhone = false) }
        } else {
            updateViewState {
                it.copy(
                    isErrorPhone = true, errorValue = Constants.ERROR.PHONE, phone = phone
                )
            }
        }
    }

    private fun changesAddCords(cords: String) {
        if (isValidCords(cords)) {
            updateViewState { it.copy(cords = cords, isErrorCords = false) }
        } else {
            updateViewState {
                it.copy(
                    isErrorCords = true, errorValue = Constants.ERROR.CORD, cords = cords
                )
            }
        }
    }

    private fun saveClient() {
        viewModelScope.launch(Dispatchers.IO) {
            val route = sharedViewModel.viewState.value.currentRoute
            val name = viewState.value.name
            val phone = viewState.value.phone
            val arrears = viewState.value.arrears
            val cords = viewState.value.cords
            val sizeList = viewState.value.listClient.value.size
            if (name.isNotEmpty() && route != null) {
                val clientRequest = ClientRequest(
                    idRoute = route.id,
                    idFactory = route.idFactory,
                    name = name,
                    phone = phone ?: Constants.EMPTY.EMPTY_STRING,
                    cord = cords ?: Constants.EMPTY.EMPTY_STRING,
                    counter = sizeList,
                    arrears = if (arrears?.length == 0) 0.0 else arrears?.toDouble() ?: 0.0,
                    date = System.currentTimeMillis()
                )

                try {
                    val response = clientApi.addClient(client = clientRequest)
                    if (response.success) {
                        val client = response.obj
                        if (client != null) {
                            loggerApi.addLog(
                                log = LogRequest(
                                    idFactory = sharedViewModel.viewState.value.factory?.id!!,
                                    log = "Пользователь: ${sharedViewModel.viewState.value.user?.name}, успешно добавил клиента: ${client.name},\nдолг:${client.arrears}",
                                    date = System.currentTimeMillis()
                                )
                            )
                            val list =
                                viewState.value.listClient.value.map { it.copy() }.toMutableList()
                            list.add(client)
                            updateViewState { it.copy(listClient = MutableStateFlow(list.sortedBy { c -> c.counter })) }
                        } else {
                            sharedViewModel.message(Constants.ERROR.SERVER_ERROR_RESPONSE)
                        }
                    } else {
                        sharedViewModel.message(response.message)
                    }
                } catch (e: Exception) {
                    sharedViewModel.message(e.message)
                } finally {
                    dismissAddDialog()
                }
            }
        }
    }


    private fun showAddDialog() {
        changesAddName()
        updateViewState {
            it.copy(
                isDialogAdd = true
            )
        }
    }

    private fun dismissAddDialog() {
        updateViewState {
            it.copy(
                isDialogAdd = false,
                name = Constants.EMPTY.EMPTY_STRING,
                cords = null,
                phone = null,
                arrears = null
            )
        }
        clearAllErrorsInputField()
    }

    private fun showDeleteDialog(itemId: Long, itemName: String) {
        updateViewState {
            it.copy(
                isDialogDelete = true, idDeleteClient = itemId, itemNameToDelete = itemName
            )
        }
    }

    fun deleteItemConfirmed() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val idClient = viewState.value.idDeleteClient
                if (idClient != null) {
                    val response = clientApi.deleteClient(idClient = idClient)
                    if (response.success) {
                        loggerApi.addLog(
                            log = LogRequest(
                                idFactory = sharedViewModel.viewState.value.factory?.id!!,
                                log = "Пользователь: ${sharedViewModel.viewState.value.user?.name}, успешно удалил клиента: ${viewState.value.itemNameToDelete}",
                                date = System.currentTimeMillis()
                            )
                        )
                        val list =
                            viewState.value.listClient.value.map { it.copy() }.toMutableList()
                        val item = list.first { it.id == idClient }
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
                dismissDeleteDialog()
            }
        }
    }

    private fun dismissDeleteDialog() {
        updateViewState {
            it.copy(
                isDialogDelete = false, idDeleteClient = null, itemNameToDelete = ""
            )
        }
    }

    private fun showUpdateDialog(route: RouteModel?, client: ClientModel) {
        _viewState.value = viewState.value.copy(
            listRoute = MutableStateFlow(sharedViewModel.getListRoute()),
            isDialogUpdate = true,
            selectedRoute = route,
            clientUpdate = client,
            name = client.name,
            arrears = "${client.arrears}",
            phone = client.phone,
            cords = client.cord
        )
        clearAllErrorsInputField()
    }

    private fun updateClient() {
        val client = viewState.value.clientUpdate
        val name = viewState.value.name
        val phone = viewState.value.phone
        val arrears = viewState.value.arrears
        val cords = viewState.value.cords
        val route = viewState.value.selectedRoute
        if (client != null && route != null && name.isNotEmpty() && sharedViewModel.initSysAdmMod()) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val request = ClientRequest(
                        id = client.id,
                        idRoute = route.id,
                        idFactory = client.idFactory,
                        name = name,
                        phone = phone ?: Constants.EMPTY.EMPTY_STRING,
                        cord = cords ?: Constants.EMPTY.EMPTY_STRING,
                        counter = client.counter,
                        arrears = if (arrears?.length == 0) 0.0 else arrears?.toDouble() ?: 0.0,
                        date = client.date
                    )

                    val response = clientApi.updateClient(client = request)
                    if (response.success) {
                        val list =
                            viewState.value.listClient.value.map { it.copy() }.toMutableList()
                        val index = list.indexOfFirst { it.id == client.id }
                        if (client.idRoute == route.id) {
                            val updatedClient = client.copy(
                                name = name,
                                phone = phone ?: Constants.EMPTY.EMPTY_STRING,
                                arrears = if (arrears?.length == 0) 0.0 else arrears?.toDouble()
                                    ?: 0.0,
                                cord = cords ?: Constants.EMPTY.EMPTY_STRING
                            )
                            loggerApi.addLog(
                                log = LogRequest(
                                    idFactory = sharedViewModel.viewState.value.factory?.id!!,
                                    log = "Пользователь: ${sharedViewModel.viewState.value.user?.name}, успешно изменил клиента.\n${
                                        findChangedFields(
                                            client, updatedClient
                                        )
                                    }",
                                    date = System.currentTimeMillis()
                                )
                            )
                            list[index] = updatedClient
                            updateViewState { it.copy(listClient = MutableStateFlow(list)) }
                        } else {
                            val item = list.first { it.id == client.id }
                            val listNew = list - item
                            updateViewState { it.copy(listClient = MutableStateFlow(listNew.sortedBy { c -> c.counter })) }
                        }
                    } else {
                        sharedViewModel.message(response.message)
                    }
                } catch (e: Exception) {
                    sharedViewModel.message(e.message)
                } finally {
                    dismissUpdateDialog()
                }
            }
        }
    }

    private fun dismissUpdateDialog() {
        updateViewState {
            it.copy(
                isDialogUpdate = false,
                name = Constants.EMPTY.EMPTY_STRING,
                cords = null,
                phone = null,
                arrears = null
            )
        }
    }

    private fun clearAllErrorsInputField() {
        updateViewState {
            it.copy(
                isErrorPhone = false,
                isErrorCords = false,
                isErrorArrears = false,
                isErrorName = false
            )
        }
    }

    private fun clientActionInvoked() {
        updateViewState { it.copy(clientAction = ClientAction.None) }
    }
}