package ru.krymer.delivery.ui.screens.route

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
import ru.krymer.delivery.data.api.RouteApi
import ru.krymer.delivery.data.model.RouteModel
import ru.krymer.delivery.data.model.utilModel.TypeMessageModel
import ru.krymer.delivery.data.request.RouteRequest
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
    val viewState = _viewState.asStateFlow()

    private fun updateViewState(update: (RouteViewState) -> RouteViewState) {
        _viewState.update { update(it) }
    }

    private fun launchCoroutine(block: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                block()
            } catch (_: CancellationException) {
                sharedViewModel.message(Constants.ERROR.CANCEL_OPERATION, type = TypeMessageModel.ERROR)
            } catch (_: TimeoutCancellationException) {
                sharedViewModel.message(Constants.ERROR.CANCEL_OPERATION, type = TypeMessageModel.ERROR)
            } catch (e: Exception) {
                sharedViewModel.message(e.message, type = TypeMessageModel.ERROR)
            }
        }
    }

    override fun obtainEvent(event: RouteEvent) {
        when (event) {
            is RouteEvent.RouteSaveAction -> saveRoute()
            is RouteEvent.NameRouteChangedAdd -> changeNameRoute(event.name)
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
            val user = sharedViewModel.viewState.value.user
            if (user != null) {
                val response = routeApi.getRoutes(idFactory = user.idFactory)
                if (response.success) {
                    val routes = response.obj
                    if (routes.isNullOrEmpty()) {
                        updateViewState {
                            it.copy(
                                listRoute = MutableStateFlow(emptyList()),
                                isLoadingData = true
                            )
                        }
                    } else {
                        updateViewState {
                            it.copy(
                                listRoute = MutableStateFlow(routes),
                                isLoadingData = true
                            )
                        }
                    }
                } else {
                    sharedViewModel.message(response.message, type = TypeMessageModel.ERROR)
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
                val name = if (route.name == "") "Маршрут без имени" else route.name
                val routeRequest = RouteRequest(
                    name = name, idFactory = route.idFactory, date = route.date, id = route.id
                )
                val response = routeApi.update(route = routeRequest)
                if (response.success) {
                    getDataRoutes()
                    dismissUpdateDialog()
                } else {
                    sharedViewModel.message(response.message, type = TypeMessageModel.ERROR)
                }
            } else {
                sharedViewModel.message(Constants.ERROR.AGAIN, type = TypeMessageModel.ERROR)
            }
        }
    }

    private fun saveRoute() {
        launchCoroutine {
            val user = sharedViewModel.viewState.value.user
            if (user != null) {
                val name = if (viewState.value.nameRouteAdd == "") "Маршрут без имени" else viewState.value.nameRouteAdd
                val routeRequest = RouteRequest(
                    name = name,
                    date = System.currentTimeMillis(),
                    idFactory = user.idFactory
                )
                val response = routeApi.add(route = routeRequest)
                if (response.success) {
                    val route = response.obj
                    if (route != null) {
                        getDataRoutes()
                        dismissAddDialog()
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
        updateViewState {
            it.copy(
                isDialogUpdate = true, routeUpdated = route
            )
        }
    }

    private fun showDeleteDialog(route: RouteModel) {
        updateViewState {
            it.copy(
                isDialogDelete = true, routeDeleted = route
            )
        }
    }

    private fun deleteItemConfirmed() {
        launchCoroutine {
            val route = viewState.value.routeDeleted
            if (route != null) {
                val response = routeApi.delete(id = route.id)
                if (response.success) {
                    getDataRoutes()
                    dismissDeleteDialog()
                } else {
                    sharedViewModel.message(response.message, type = TypeMessageModel.ERROR)
                }
            } else {
                sharedViewModel.message(Constants.ERROR.AGAIN, type = TypeMessageModel.ERROR)
            }
        }
    }

    private fun changeNameRoute(name: String) {
        updateViewState { it.copy(nameRouteAdd = name) }
    }
}