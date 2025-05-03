package ru.krymer.delivery.ui.screens.route

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
import ru.krymer.delivery.data.api.RouteApi
import ru.krymer.delivery.data.model.RouteModel
import ru.krymer.delivery.data.request.RouteRequest
import ru.krymer.delivery.ui.screens.route.models.RouteAction
import ru.krymer.delivery.ui.screens.route.models.RouteEvent
import ru.krymer.delivery.ui.screens.route.models.RouteViewState
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import ru.krymer.delivery.utills.Constants
import javax.inject.Inject

@HiltViewModel
class RouteViewModel @Inject constructor(
    private val routeApi: RouteApi,
    private val sharedViewModel: SharedViewModel
) : ViewModel(), EventHandler<RouteEvent> {

    private val _viewState = MutableStateFlow(RouteViewState())
    val viewState: StateFlow<RouteViewState> = _viewState

    private fun updateViewState(update: (RouteViewState) -> RouteViewState) {
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

    override fun obtainEvent(event: RouteEvent) {
        when (event) {
            is RouteEvent.RouteActionInvoked -> routeActionInvoked()
            is RouteEvent.RouteSaveAction -> saveRoute()
            is RouteEvent.NameRouteChangedAdd -> changeNameRoute(event.name)
            is RouteEvent.RouteItemClickedToShop -> openClientsByRoute(event.route)
            is RouteEvent.ShowDeleteDialog -> showDeleteDialog(route = event.route)
            is RouteEvent.ShowAddDialog -> showAddDialog()
            is RouteEvent.ShowUpdateDialog -> showUpdateDialog(event.route)
            is RouteEvent.UpdateNameRoute -> changeNameUpdate(event.name)
            is RouteEvent.RouteUpdateAction -> updateRoute()
            is RouteEvent.DismissAddDialog -> dismissAddDialog()
            is RouteEvent.DismissDeleteDialog -> dismissDeleteDialog()
            is RouteEvent.DismissUpdateDialog -> dismissUpdateDialog()
            is RouteEvent.DeleteRoute -> deleteItemConfirmed()
        }
    }

    init {
        getDataRoutes()
    }

    private fun getDataRoutes() {
        launchCoroutine {
            val user = sharedViewModel.viewState.value.user.value
            if (user != null) {
                val response = routeApi.getRoutes(idFactory = user.idFactory)
                if (response.success) {
                    val routes = response.obj
                    if (routes != null) {
                        updateViewState {
                            it.copy(
                                listRoute = MutableStateFlow(routes)
                            )
                        }
                    } else {
                        delay(5000)
                        getDataRoutes()
                    }
                } else {
                    sharedViewModel.message(response.message)
                }
            }
        }
    }


    private fun dismissUpdateDialog() {
        updateViewState {
            it.copy(
                isDialogUpdate = false, routeUpdated = null
            )
        }
    }

    private fun dismissAddDialog() {
        updateViewState { it.copy(showDialogAdd = false, nameRouteAdd = "") }
    }

    private fun dismissDeleteDialog() {
        updateViewState {
            it.copy(
                isDialogDelete = false, routeDeleted = null
            )
        }
    }

    private fun updateRoute() {
        launchCoroutine {
            val route = viewState.value.routeUpdated
            if (route != null) {
                val routeRequest = RouteRequest(
                    name = route.name, idFactory = route.idFactory, date = route.date, id = route.id
                )
                val response = routeApi.update(route = routeRequest)
                if (response.success) {
                    val list = viewState.value.listRoute.value.map { it.copy() }.toMutableList()
                    val index = list.indexOfFirst { it.id == route.id }
                    list[index] = route.copy(
                        name = route.name
                    )
                    updateViewState { it.copy(listRoute = MutableStateFlow(list)) }
                    dismissUpdateDialog()
                } else {
                    sharedViewModel.message(response.message)
                }
            } else {
                sharedViewModel.message(Constants.EMPTY.EMPTY_DATA)
            }
        }
    }

    private fun saveRoute() {
        launchCoroutine {
            val user = sharedViewModel.viewState.value.user.value
            if (user != null) {
                val routeRequest = RouteRequest(
                    name = viewState.value.nameRouteAdd,
                    date = System.currentTimeMillis(),
                    idFactory = user.idFactory
                )
                val response = routeApi.add(route = routeRequest)
                if (response.success) {
                    val route = response.obj
                    if (route != null) {
                        val list = viewState.value.listRoute.value.map { it.copy() }.toMutableList()
                        list.add(route)
                        updateViewState { it.copy(listRoute = MutableStateFlow(list.sortedBy { r -> r.name })) }
                        dismissAddDialog()
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
                showDialogAdd = true
            )
        }
    }

    private fun changeNameUpdate(name: String) {
        val route = viewState.value.routeUpdated
        if (route != null) {
            updateViewState { it.copy(routeUpdated = route.copy(name = name)) }
        }
    }

    private fun showUpdateDialog(route: RouteModel) {
        if (sharedViewModel.initSysAdmMod()) {
            updateViewState {
                it.copy(
                    isDialogUpdate = true, routeUpdated = route
                )
            }
        }
    }

    private fun showDeleteDialog(route: RouteModel) {
        if (sharedViewModel.initSysAdmMod()) {
            updateViewState {
                it.copy(
                    isDialogDelete = true, routeDeleted = route
                )
            }
        }
    }

    private fun deleteItemConfirmed() {
        launchCoroutine {
            val route = viewState.value.routeDeleted
            if (route != null) {
                val response = routeApi.delete(id = route.id)
                if (response.success) {
                    val list = viewState.value.listRoute.value.map { it.copy() }.toMutableList()
                    val item = list.first { it.id == route.id }
                    val listNew = list - item
                    updateViewState { it.copy(listRoute = MutableStateFlow(listNew.sortedBy { r -> r.name })) }
                    dismissDeleteDialog()
                } else {
                    sharedViewModel.message(response.message)
                }
            } else {
                sharedViewModel.message(Constants.EMPTY.EMPTY_DATA)
            }
        }
    }

    private fun openClientsByRoute(route: RouteModel) {
        val routes = viewState.value.listRoute.value
        sharedViewModel.saveRouteList(routes)
        updateViewState { it.copy(routeAction = RouteAction.OpenClients) }
        sharedViewModel.initCurrentRoute(route)
    }


    private fun changeNameRoute(name: String) {
        updateViewState { it.copy(nameRouteAdd = name) }
    }

    private fun routeActionInvoked() {
        updateViewState { it.copy(routeAction = RouteAction.None) }
    }
}