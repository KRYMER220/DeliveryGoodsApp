package ru.krymer.delivery.ui.screens.route

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.krymer.delivery.common.EventHandler
import ru.krymer.delivery.data.api.LoggerApi
import ru.krymer.delivery.data.api.RouteApi
import ru.krymer.delivery.data.model.RouteModel
import ru.krymer.delivery.data.model.utilModel.TypeMessageModel
import ru.krymer.delivery.data.request.LogRequest
import ru.krymer.delivery.data.request.RouteRequest
import ru.krymer.delivery.ui.screens.route.models.RouteAction
import ru.krymer.delivery.ui.screens.route.models.RouteEvent
import ru.krymer.delivery.ui.screens.route.models.RouteViewState
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import ru.krymer.delivery.utills.Constants
import ru.krymer.delivery.utills.isEmptyInput
import javax.inject.Inject

@HiltViewModel
class RouteViewModel @Inject constructor(
    private val routeApi: RouteApi,
    private val sharedViewModel: SharedViewModel, private val loggerApi: LoggerApi
) : ViewModel(), EventHandler<RouteEvent> {

    private val _viewState = MutableStateFlow(RouteViewState())
    val viewState: StateFlow<RouteViewState> = _viewState

    private fun updateViewState(update: (RouteViewState) -> RouteViewState) {
        _viewState.update { update(it) }
    }

    override fun obtainEvent(event: RouteEvent) {
        when (event) {
            is RouteEvent.RouteActionInvoked -> routeActionInvoked()
            is RouteEvent.RouteSaveAction -> saveRoute()
            is RouteEvent.NameRouteChangedAdd -> changeNameRoute(event.name)
            is RouteEvent.RouteItemClickedToShop -> clickedRouteToClient(event.route)
            is RouteEvent.ShowDeleteDialog -> showDeleteDialog(event.itemID, event.itemName)
            is RouteEvent.ShowAddDialog -> showAddDialog()
            is RouteEvent.RouteItemLongClicked -> showUpdateDialog(event.route)
            is RouteEvent.NameRouteChangedUpdate -> nameChangeUpdated(event.name)
            is RouteEvent.RouteUpdateAction -> updateRoute()
            RouteEvent.DismissAddDialog -> dismissAddDialog()
            RouteEvent.DismissDeleteDialog -> dismissDeleteDialog()
            RouteEvent.DismissUpdateDialog -> dismissUpdateDialog()
            RouteEvent.DeleteRoute -> deleteItemConfirmed()
        }
    }

    init {
        getDataRoutes()
    }

    private fun getDataRoutes() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = sharedViewModel.viewState.value.user
                if (user != null) {
                    val idFactory = user.idFactory
                    val response =
                        routeApi.getCurrentListRoute(idFactory = idFactory)
                    if (response.success) {
                        val routes = response.obj
                        if (routes != null) {
                            updateViewState {
                                it.copy(
                                    listRoute = MutableStateFlow(routes), isLoadRouteData = true
                                )
                            }
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

    private fun dismissUpdateDialog() {
        updateViewState {
            it.copy(
                isDialogUpdate = false, itemNameUpdate = ""
            )
        }
    }

    private fun dismissAddDialog() {
        updateViewState { it.copy(showDialogAdd = false, itemNameAdd = "") }
    }

    private fun dismissDeleteDialog() {
        updateViewState {
            it.copy(
                isDialogDelete = false, itemIdDelete = null, itemNameDelete = null
            )
        }
    }

    private fun updateRoute() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val route = viewState.value.routeUpdated
                val name = viewState.value.itemNameUpdate
                if (name.isNotEmpty() && route != null) {
                    val routeRequest = RouteRequest(
                        name = name,
                        idFactory = route.idFactory,
                        date = route.date,
                        id = route.id
                    )
                    val response = routeApi.updateRoute(route = routeRequest)
                    if (response.success) {
                        loggerApi.addLog(
                            log = LogRequest(
                                idFactory = sharedViewModel.viewState.value.factory?.id!!,
                                log = "Пользователь: ${sharedViewModel.viewState.value.user?.name}, успешно обновил название: ${route.name} -> $name ",
                                date = System.currentTimeMillis()
                            )
                        )
                        val list = viewState.value.listRoute.value.map { it.copy() }.toMutableList()
                        val index = list.indexOfFirst { it.id == route.id }
                        list[index] = route.copy(
                            name = name
                        )
                        updateViewState { it.copy(listRoute = MutableStateFlow(list)) }
                        sharedViewModel.message(
                            response.message,
                            typeMessageModel = TypeMessageModel.SUCCEED
                        )
                    } else {
                        sharedViewModel.message(response.message)
                    }
                } else {
                    when {
                        name.isEmpty() -> sharedViewModel.message(Constants.EMPTY.EMPTY_NAME)
                        route == null -> sharedViewModel.message(Constants.EMPTY.EMPTY_DATA)
                    }
                }
            } catch (e: Exception) {
                sharedViewModel.message(message = e.message)
            } finally {
                dismissUpdateDialog()
            }
        }
    }

    private fun saveRoute() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val name = viewState.value.itemNameAdd
                val factory = sharedViewModel.viewState.value.factory
                if (factory != null) {
                    if (name.isNotEmpty()) {
                        val routeRequest = RouteRequest(
                            name = name, date = System.currentTimeMillis(), idFactory = factory.id
                        )
                        val response = routeApi.addRoute(route = routeRequest)
                        if (response.success) {
                            val route = response.obj
                            loggerApi.addLog(
                                log = LogRequest(
                                    idFactory = factory.id,
                                    log = "Пользователь: ${sharedViewModel.viewState.value.user?.name}, успешно добавил маршрут: $name",
                                    date = System.currentTimeMillis()
                                )
                            )
                            if (route != null) {
                                val list = viewState.value.listRoute.value.map { it.copy() }
                                    .toMutableList()
                                list.add(route)
                                updateViewState { it.copy(listRoute = MutableStateFlow(list.sortedBy { r -> r.name })) }
                            } else {
                                sharedViewModel.message(Constants.ERROR.SERVER_ERROR_RESPONSE)
                            }
                        } else {
                            sharedViewModel.message(response.message)
                        }
                    } else {
                        sharedViewModel.message(Constants.EMPTY.EMPTY_NAME)
                    }
                }
            } catch (e: Exception) {
                sharedViewModel.message(message = e.message)
            } finally {
                dismissAddDialog()
            }
        }
    }

    private fun showAddDialog() {
        updateViewState {
            it.copy(
                showDialogAdd = true
            )
        }
        clearAllErrorsInputField()
    }


    private fun clearAllErrorsInputField() {
        updateViewState { it.copy(isErrorName = false) }
    }

    private fun nameChangeUpdated(name: String) {
        if (validName(name)) {
            updateViewState { it.copy(itemNameUpdate = name) }
        }
    }

    private fun validName(name: String): Boolean {
        return if (isEmptyInput(name)) {
            updateViewState { it.copy(itemNameUpdate = name, isErrorName = false) }
            true
        } else {
            updateViewState {
                it.copy(
                    errorName = Constants.EMPTY.EMPTY_FIELD, isErrorName = true
                )
            }
            false
        }
    }

    private fun showUpdateDialog(route: RouteModel) {
        if (sharedViewModel.initSysAdmMod()) {
            updateViewState {
                it.copy(
                    isDialogUpdate = true, itemNameUpdate = route.name, routeUpdated = route
                )
            }
            clearAllErrorsInputField()
        }
    }

    private fun showDeleteDialog(itemId: Long, itemName: String) {
        updateViewState {
            it.copy(
                isDialogDelete = true, itemIdDelete = itemId, itemNameDelete = itemName
            )
        }
    }

    private fun deleteItemConfirmed() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val idRoute = viewState.value.itemIdDelete
                if (idRoute != null) {
                    val response = routeApi.deleteRoute(idRoute = idRoute)
                    if (response.success) {
                        loggerApi.addLog(
                            log = LogRequest(
                                idFactory = sharedViewModel.viewState.value.factory?.id!!,
                                log = "Пользователь: ${sharedViewModel.viewState.value.user?.name}, успешно удалил маршрут: ${viewState.value.itemNameDelete}",
                                date = System.currentTimeMillis()
                            )
                        )
                        val list = viewState.value.listRoute.value.map { it.copy() }.toMutableList()
                        val item = list.first { it.id == idRoute }
                        val listNew = list - item
                        updateViewState { it.copy(listRoute = MutableStateFlow(listNew.sortedBy { r -> r.name })) }
                    } else {
                        sharedViewModel.message(response.message)
                    }
                } else {
                    sharedViewModel.message(Constants.EMPTY.EMPTY_DATA)
                }
            } catch (e: Exception) {
                sharedViewModel.message(e.message)
            } finally {
                dismissDeleteDialog()
            }
        }
    }

    private fun clickedRouteToClient(route: RouteModel) {
        val routes = viewState.value.listRoute.value
        sharedViewModel.saveRouteList(routes)
        updateViewState { it.copy(routeAction = RouteAction.OpenClients) }
        sharedViewModel.initCurrentRoute(route)
    }


    private fun changeNameRoute(name: String) {
        if (validName(name)) {
            updateViewState { it.copy(itemNameAdd = name) }
        }
    }

    private fun routeActionInvoked() {
        updateViewState { it.copy(routeAction = RouteAction.None) }
    }
}