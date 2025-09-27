package ru.krymer.delivery.ui.screens.route

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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
) : ViewModel() {

    private val _events = MutableSharedFlow<RouteEvent>(extraBufferCapacity = 64)

    val viewState: StateFlow<RouteViewState> = _events
        .onStart {
            emit(RouteEvent.RefreshRoutes)
        }
        .runningFold(RouteViewState()) { state, event ->
            when (event) {
                is RouteEvent.RefreshRoutes -> {
                    viewModelScope.launch(Dispatchers.IO) { loadRoutes() }
                    state.copy(isLoading = true)
                }

                is RouteEvent.RoutesLoaded -> {
                    state.copy(isLoading = false, routes = event.routes)
                }

                is RouteEvent.Error -> {
                    sharedViewModel.message(event.message, type = TypeMessageModel.ERROR)
                    state.copy(isLoading = false)
                }

                is RouteEvent.ToggleAddDialog ->
                    state.copy(toggleDialogAdd = !state.toggleDialogAdd)

                is RouteEvent.ToggleUpdateDialog ->
                    state.copy(toggleDialogUpdate = !state.toggleDialogUpdate, route = event.route)

                is RouteEvent.ToggleDeleteDialog ->
                    state.copy(toggleDialogDelete = !state.toggleDialogDelete, route = event.route)

                is RouteEvent.ChangeAddName ->
                    state.copy(nameRouteAdd = event.name)

                is RouteEvent.ChangeUpdateName ->
                    state.copy(nameRouteUpdate = event.name)

                is RouteEvent.CreateRoute -> {
                    viewModelScope.launch(Dispatchers.IO) { createRoute() }
                    state
                }

                is RouteEvent.UpdateRoute -> {
                    viewModelScope.launch(Dispatchers.IO) { updateRoute() }
                    state
                }

                is RouteEvent.DeleteRoute -> {
                    viewModelScope.launch(Dispatchers.IO) { deleteRoute() }
                    state
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RouteViewState())

    fun obtainEvent(event: RouteEvent) {
        _events.tryEmit(event)
    }

    private suspend fun loadRoutes() {
        val user = sharedViewModel.viewState.value.user ?: run {
            _events.emit(RouteEvent.Error(Constants.ERROR.AGAIN))
            return
        }

        when (val res = repository.getRoutes(user.idFactory)) {
            is MyResult.Success -> _events.emit(RouteEvent.RoutesLoaded(res.data))
            is MyResult.Error -> _events.emit(RouteEvent.Error(res.message))
        }
    }

    private suspend fun createRoute() {
        val state = viewState.value
        val user = sharedViewModel.viewState.value.user ?: run {
            _events.emit(RouteEvent.Error(message = Constants.ERROR.AGAIN))
            return
        }

        val name = viewState.value.nameRouteAdd.ifBlank { "Маршрут без имени ${state.routes.size}" }
        val request =
            RouteRequest(name = name, date = System.currentTimeMillis(), idFactory = user.idFactory)

        when (val res = repository.addRoute(request)) {
            is MyResult.Success -> {
                _events.emit(RouteEvent.RefreshRoutes)
                _events.emit(RouteEvent.ToggleAddDialog())
            }

            is MyResult.Error -> _events.emit(RouteEvent.Error(message = res.message))
        }
    }

    private suspend fun updateRoute() {
        val route = viewState.value.route ?: return
        val name = viewState.value.nameRouteUpdate.ifBlank { route.name }
        val req =
            RouteRequest(id = route.id, name = name, date = route.date, idFactory = route.idFactory)

        when (val res = repository.updateRoute(req)) {
            is MyResult.Success -> {
                _events.emit(RouteEvent.RefreshRoutes)
                _events.emit(RouteEvent.ToggleUpdateDialog(route = null))
            }

            is MyResult.Error -> _events.emit(RouteEvent.Error(res.message))
        }
    }

    private suspend fun deleteRoute() {
        val route = viewState.value.route ?: return
        when (val res = repository.deleteRoute(id = route.id)) {
            is MyResult.Success -> {
                _events.emit(RouteEvent.RefreshRoutes)
                _events.emit(RouteEvent.ToggleDeleteDialog(route = null))
            }

            is MyResult.Error -> _events.emit(RouteEvent.Error(message = res.message))
        }
    }
}