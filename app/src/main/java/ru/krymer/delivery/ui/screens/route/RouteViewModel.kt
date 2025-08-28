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
import ru.krymer.delivery.data.model.RouteModel
import ru.krymer.delivery.data.model.utilModel.TypeMessageModel
import ru.krymer.delivery.data.repositoryImpl.RouteRepositoryImpl
import ru.krymer.delivery.data.request.RouteRequest
import ru.krymer.delivery.ui.screens.route.models.RouteEvent
import ru.krymer.delivery.ui.screens.route.models.RouteViewState
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import ru.krymer.delivery.utills.Constants
import ru.krymer.delivery.utills.MyResult
import javax.inject.Inject

@HiltViewModel
class RouteViewModel @Inject constructor(
    private val repository: RouteRepositoryImpl,
    private val sharedViewModel: SharedViewModel
) : ViewModel(), EventHandler<RouteEvent> {

    private val _viewState = MutableStateFlow(RouteViewState())
    val viewState = _viewState.asStateFlow()

    private fun updateState(update: (RouteViewState) -> RouteViewState) {
        _viewState.update { update(it) }
    }

    private fun launchCoroutine(block: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                block()
            } catch (_: CancellationException) {
                sharedViewModel.message(Constants.ERROR.CANCEL_OPERATION, type = TypeMessageModel.ERROR)
            } catch (_: TimeoutCancellationException) {
                sharedViewModel.message(Constants.ERROR.TIMEOUT, type = TypeMessageModel.ERROR)
            } catch (e: Exception) {
                sharedViewModel.message(e.message, type = TypeMessageModel.ERROR)
            }
        }
    }

    override fun obtainEvent(event: RouteEvent) {
        when (event) {
            is RouteEvent.CreateRoute -> createRoute()
            is RouteEvent.NameRouteChangedAdd -> setValue(add = event.name)
            is RouteEvent.UpdateNameRoute -> setValue(update = event.name)

            is RouteEvent.ToggleDeleteDialog -> setState(delete = !viewState.value.toggleDialogDelete, route = event.route)
            is RouteEvent.ToggleAddDialog -> setState(add = !viewState.value.toggleDialogAdd)
            is RouteEvent.ToggleUpdateDialog -> setState(update = !viewState.value.toggleDialogUpdate, route = event.route)

            is RouteEvent.UpdateRoute -> updateRoute()
            is RouteEvent.DeleteRoute -> deleteRoute()
        }
    }

    init {
        loadRoutes()
    }

    private fun loadRoutes() = launchCoroutine {
        updateState { it.copy(isLoading = true) }
        val user = sharedViewModel.viewState.value.user
        if (user == null) {
            updateState { it.copy(isLoading = false) }
            sharedViewModel.message(Constants.ERROR.AGAIN, type = TypeMessageModel.ERROR)
            return@launchCoroutine
        }

        when (val res = repository.getRoutes(idFactory = user.idFactory)) {
            is MyResult.Success -> updateState { it.copy(routes = res.data, isLoading = false) }
            is MyResult.Error -> {
                updateState { it.copy(isLoading = false) }
                sharedViewModel.message(res.message, type = TypeMessageModel.ERROR)
            }
        }
    }

    private fun setState(
        delete: Boolean = viewState.value.toggleDialogDelete,
        update: Boolean = viewState.value.toggleDialogUpdate,
        add: Boolean = viewState.value.toggleDialogAdd,
        route: RouteModel? = viewState.value.route
    ) {
        updateState { it.copy(
            route = route,
            toggleDialogDelete = delete,
            toggleDialogUpdate = update,
            toggleDialogAdd = add
        ) }
    }

    private fun setValue(
        add: String = viewState.value.nameRouteAdd,
        update: String = viewState.value.nameRouteUpdate
    ) {
        updateState { it.copy(
            nameRouteAdd = add,
            nameRouteUpdate = update
        ) }
    }

    private fun updateRoute() = launchCoroutine {
        val route = viewState.value.route
        if (route == null) {
            sharedViewModel.message(Constants.ERROR.AGAIN, type = TypeMessageModel.ERROR)
            return@launchCoroutine
        }
        val name = viewState.value.nameRouteUpdate.ifBlank { "Маршрут без имени" }
        val req = RouteRequest(id = route.id, name = name, date = route.date, idFactory = route.idFactory)

        when (val res = repository.updateRoute(req)) {
            is MyResult.Success -> {
                loadRoutes()
                setState(update = false)
            }
            is MyResult.Error -> sharedViewModel.message(res.message, type = TypeMessageModel.ERROR)
        }
    }

    private fun createRoute() = launchCoroutine {
        val user = sharedViewModel.viewState.value.user
        if (user == null) {
            sharedViewModel.message(Constants.ERROR.AGAIN, type = TypeMessageModel.ERROR)
            return@launchCoroutine
        }

        val name = viewState.value.nameRouteAdd.ifBlank { "Маршрут без имени" }
        val request = RouteRequest(name = name, date = System.currentTimeMillis(), idFactory = user.idFactory)

        when (val res = repository.addRoute(request)) {
            is MyResult.Success -> {
                loadRoutes()
                setState(add = false)
                setValue(add = "")
            }
            is MyResult.Error -> sharedViewModel.message(res.message, type = TypeMessageModel.ERROR)
        }
    }

    private fun deleteRoute() = launchCoroutine {
        val route = viewState.value.route
        if (route == null) {
            sharedViewModel.message(Constants.ERROR.AGAIN, type = TypeMessageModel.ERROR)
            return@launchCoroutine
        }

        when (val res = repository.deleteRoute(route.id)) {
            is MyResult.Success -> {
                loadRoutes()
                setState(delete = false)
            }
            is MyResult.Error -> sharedViewModel.message(res.message, type = TypeMessageModel.ERROR)
        }
    }
}