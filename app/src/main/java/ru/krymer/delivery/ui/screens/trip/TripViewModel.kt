package ru.krymer.delivery.ui.screens.trip

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
import ru.krymer.delivery.AppDatabase
import ru.krymer.delivery.data.model.utilModel.TypeMessageModel
import ru.krymer.delivery.data.repositoryImpl.CourierRepositoryImpl
import ru.krymer.delivery.data.repositoryImpl.RouteRepositoryImpl
import ru.krymer.delivery.data.repositoryImpl.TripRepositoryImpl
import ru.krymer.delivery.data.request.TripRequest
import ru.krymer.delivery.di.AppPreferencesManager
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import ru.krymer.delivery.ui.screens.trip.models.TripEvent
import ru.krymer.delivery.ui.screens.trip.models.TripViewState
import ru.krymer.delivery.utills.Constants
import ru.krymer.delivery.utills.MyResult
import ru.krymer.delivery.utills.getStartOfNextDay
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class TripViewModel @Inject constructor(
    private val repository: TripRepositoryImpl,
    private val repositoryCourier: CourierRepositoryImpl,
    private val repositoryRoute: RouteRepositoryImpl,
    private val sharedViewModel: SharedViewModel,
    private val database: AppDatabase,
    private val manager: AppPreferencesManager
) : ViewModel() {

    private val _events = MutableSharedFlow<TripEvent>(extraBufferCapacity = 64)

    private fun launchCoroutine(block: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
                _events.emit(TripEvent.Error(Constants.ERROR.CANCEL_OPERATION))
            } catch (e: TimeoutCancellationException) {
                throw e
                _events.emit(TripEvent.Error(Constants.ERROR.TIMEOUT))
            } catch (e: Exception) {
                throw e
                _events.emit(TripEvent.Error(e.message))
            }
        }
    }

    val viewState: StateFlow<TripViewState> = _events
        .onStart {
            emit(TripEvent.LoadListDropMenuRoutes)
            emit(TripEvent.LoadListDropMenuCouriers)
            emit(TripEvent.LoadSettings)
            emit(TripEvent.LocalData)
        }
        .runningFold(TripViewState()) { state, event ->
            when (event) {
                TripEvent.DeleteTrip -> {
                    launchCoroutine { deleteTrip() }
                    state
                }

                is TripEvent.ToggleDeleteDialog -> state.copy(toggleDeleteTrip = !state.toggleDeleteTrip, trip = event.trip)

                is TripEvent.ToggleUpdateDialog -> state.copy(
                    toggleUpdateTrip = !state.toggleUpdateTrip,
                    trip = if (!state.toggleUpdateTrip) event.trip else null,
                    salary = if (!state.toggleUpdateTrip) event.trip?.salary?.toInt().toString() else "",
                    currentRoute = if (!state.toggleUpdateTrip) state.listRoute.first { it.id == event.trip?.idRoute} else null,
                    currentCourier = if (!state.toggleUpdateTrip) state.listCourier.first { it.id == event.trip?.idCourier} else null,
                )

                TripEvent.RefreshTrips -> {
                    launchCoroutine { loadTrips() }
                    state.copy(isLoading = true)
                }

                TripEvent.SaveTrip -> {
                    launchCoroutine { createTrip() }
                    state
                }

                TripEvent.SubmitFilter -> {
                    launchCoroutine { submitFilter() }
                    state
                }

                TripEvent.UpdateTrip -> {
                    launchCoroutine { updateTrip() }
                    state
                }

                is TripEvent.ChangeCourierFilter -> state.copy(filterUid = event.courier?.id)

                is TripEvent.ChangeDate -> state.copy(currentDate = event.date)

                is TripEvent.ChangeRouteFilter -> state.copy(filterRouteId = event.route?.id)

                is TripEvent.ChangeSalaryTrip -> state.copy(salary = event.salary)

                is TripEvent.ChangeSort -> {
                    val newSort = !event.boolean
                    manager.saveBoolean(Constants.KEYS.SORT, newSort)
                    state.copy(sort = newSort)
                }

                TripEvent.LoadListDropMenuCouriers -> {
                    launchCoroutine { loadListDropMenuCouriers() }
                    state
                }

                TripEvent.LoadListDropMenuRoutes -> {
                    launchCoroutine { loadListDropMenuRoutes() }
                    state
                }

                TripEvent.LoadMoreTrips -> {
                    val currentState = state
                    if (!currentState.isFilter && currentState.hasMore && !currentState.isLoading) {
                        launchCoroutine { loadPaginatedTrips(loadMore = true) }
                    }
                    state.copy(isLoading = true)
                }

                TripEvent.LoadSettings -> {
                    val isFilter = manager.getBooleanData(Constants.KEYS.FILTER) == true
                    val isSorted = manager.getBooleanData(Constants.KEYS.SORT) == true
                    val lightVersion = sharedViewModel.viewState.value.lightVersion
                    state.copy(isFilter = isFilter, sort = isSorted, lightVersion = lightVersion)
                }

                TripEvent.ToggleFilterDialog -> state.copy(isShowFilterDialog = !state.isShowFilterDialog)

                TripEvent.ToggleAddDialog -> state.copy(
                    toggleAddTrip = !state.toggleAddTrip,
                    currentDate = getStartOfNextDay(),
                )

                is TripEvent.TripsLoaded -> state.copy(trips = event.trips, isLoading = false, hasMore = event.hasMore)

                is TripEvent.ListCouriersLoaded -> state.copy(listCourier = event.couriers)

                is TripEvent.ListRoutesLoaded -> state.copy(listRoute = event.routes)

                is TripEvent.Error -> {
                    sharedViewModel.message(event.message, type = TypeMessageModel.ERROR)
                    state.copy(isLoading = false)
                }

                is TripEvent.IsFilter -> {
                    manager.saveBoolean(Constants.KEYS.FILTER, event.isFilter)
                    state.copy(isFilter = event.isFilter)
                }

                is TripEvent.SelectCourier -> state.copy(currentCourier = event.courier)
                is TripEvent.SelectRoute -> state.copy(currentRoute = event.route)
                TripEvent.LocalData -> {
                    launchCoroutine {
                        loadLocalData()
                    }
                    state
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TripViewState())

    fun obtainEvent(event: TripEvent) {
        _events.tryEmit(event)
    }

    private suspend fun loadLocalData() {
        val user = sharedViewModel.viewState.value.user ?: run {
            _events.emit(TripEvent.Error(Constants.ERROR.GENERAL_ERROR))
            return
        }

        val isFilter = viewState.value.isFilter
        val isSorted = viewState.value.sort
        val localTrips = if (!isFilter) {
            if (isSorted) {
                database.tripDao().getTrips().sortedBy { it.date }
            } else {
                database.tripDao().getTrips().sortedByDescending { it.date }
            }
        } else {
            database.tripDao().getTrips().filter { it.idCourier == user.id }.sortedByDescending { it.date }
        }

        if (localTrips.isNotEmpty()) {
            _events.emit(TripEvent.TripsLoaded(trips = localTrips, hasMore = false))
        }

        loadTrips()
    }

    private suspend fun loadTrips() {
        val user = sharedViewModel.viewState.value.user ?: run {
            _events.emit(TripEvent.Error(Constants.ERROR.GENERAL_ERROR))
            return
        }

        val uid = viewState.value.filterUid
        val routeId = viewState.value.filterRouteId
        val sort = viewState.value.sort
        val isFilter = viewState.value.isFilter

        if (isFilter) {
            when (val response = repository.getTrips(
                idFactory = user.idFactory,
                uid = uid,
                routeId = routeId,
                sortBy = if (sort) Constants.SORT.ASC else Constants.SORT.DESC
            )) {
                is MyResult.Success -> {
                    _events.emit(TripEvent.TripsLoaded(trips = response.data, hasMore = false))
                }
                is MyResult.Error -> _events.emit(TripEvent.Error(message = response.message))
            }
        } else {
            loadPaginatedTrips(loadMore = false)
        }
    }

    private suspend fun createTrip() {
        val factory = sharedViewModel.viewState.value.factory ?: run {
            _events.emit(TripEvent.Error(Constants.ERROR.AGAIN))
            return
        }
        val curRoute = viewState.value.currentRoute
        val curCourier = viewState.value.currentCourier
        val date = viewState.value.currentDate + Random.nextInt(from = 1, until = 1000)

        if (curRoute != null && curCourier != null) {
            val tripRequest = TripRequest(
                factoryId = factory.id,
                date = date,
                courierId = curCourier.id,
                routeId = curRoute.id,
                salary = curCourier.salary,
                percentCourier = curCourier.percentSalary,
                priceMillage = factory.priceMillage,
                nameRoute = curRoute.name,
                nameCourier = curCourier.name
            )

            when (val response = repository.add(trip = tripRequest)) {
                is MyResult.Success -> {
                    _events.emit(TripEvent.ToggleAddDialog)
                    _events.emit(TripEvent.RefreshTrips)
                }
                is MyResult.Error -> _events.emit(TripEvent.Error(message = response.message))
            }
        } else {
            _events.emit(TripEvent.Error(Constants.ERROR.AGAIN))
        }
    }

    private suspend fun updateTrip() {
        val trip = viewState.value.trip
        val route = viewState.value.currentRoute
        val courier = viewState.value.currentCourier
        val date = viewState.value.currentDate
        val salary = viewState.value.salary

        if (route != null && courier != null && trip != null) {
            val tripRequest = TripRequest(
                id = trip.id,
                factoryId = trip.idFactory,
                date = date + Random.nextInt(from = 1, until = 1000),
                courierId = courier.id,
                routeId = route.id,
                salary = if (salary == "") courier.salary else salary.toDouble(),
                percentCourier = courier.percentSalary,
                priceMillage = trip.priceMillage,
                millage = trip.millage,
                nameCourier = courier.name,
                nameRoute = route.name,
            )

            when (val response = repository.update(trip = tripRequest)) {
                is MyResult.Success -> {
                    _events.emit(TripEvent.RefreshTrips)
                    _events.emit(TripEvent.ToggleUpdateDialog(trip = null))
                }
                is MyResult.Error -> _events.emit(TripEvent.Error(message = response.message))
            }
        }
    }

    private suspend fun deleteTrip() {
        val trip = viewState.value.trip
        if (trip != null) {
            when (val response = repository.delete(id = trip.id)) {
                is MyResult.Success -> {
                    database.tripDao().deleteTrip(trip)
                    _events.emit(TripEvent.RefreshTrips)
                    _events.emit(TripEvent.ToggleDeleteDialog(trip = null))
                }
                is MyResult.Error -> _events.emit(TripEvent.Error(message = response.message))
            }
        } else {
            _events.emit(TripEvent.Error(Constants.ERROR.GENERAL_ERROR))
        }
    }

    private suspend fun loadListDropMenuRoutes() {
        val user = sharedViewModel.viewState.value.user ?: run {
            _events.emit(TripEvent.Error(Constants.ERROR.GENERAL_ERROR))
            return
        }

        when (val response = repositoryRoute.getRoutes(user.idFactory)) {
            is MyResult.Success -> {
                _events.emit(TripEvent.ListRoutesLoaded(routes = response.data))
            }
            is MyResult.Error -> _events.emit(TripEvent.Error(message = response.message))
        }
    }

    private suspend fun loadListDropMenuCouriers() {
        val user = sharedViewModel.viewState.value.user ?: run {
            _events.emit(TripEvent.Error(Constants.ERROR.GENERAL_ERROR))
            return
        }

        when (val response = repositoryCourier.getUsers(user.idFactory)) {
            is MyResult.Success -> {
                _events.emit(TripEvent.ListCouriersLoaded(couriers = response.data ))
            }
            is MyResult.Error -> _events.emit(TripEvent.Error(message = response.message))
        }
    }

    private suspend fun loadPaginatedTrips(loadMore: Boolean) {
        val user = sharedViewModel.viewState.value.user ?: run {
            _events.emit(TripEvent.Error(Constants.ERROR.GENERAL_ERROR))
            return
        }

        val limit = 10
        val lastTrip = if (loadMore && viewState.value.trips.isNotEmpty()) {
            viewState.value.trips.last()
        } else {
            null
        }

        when (val response = repository.getPaginatedTrips(
            idFactory = user.idFactory,
            limit = limit,
            lastDate = lastTrip?.date,
            lastId = lastTrip?.id
        )) {
            is MyResult.Success -> {
                val newTrips = response.data
                val currentTrips = if (loadMore) {
                    viewState.value.trips.toMutableList().apply { addAll(newTrips) }
                } else {
                    newTrips.toMutableList()
                }

                val newHasMore = newTrips.size == limit
                _events.emit(TripEvent.TripsLoaded(trips = currentTrips, hasMore = newHasMore))
            }
            is MyResult.Error -> _events.emit(TripEvent.Error(message = response.message))
        }
    }

    private suspend fun submitFilter() {
        val isFilter = viewState.value.isFilter
        if (!isFilter) {
            _events.emit(TripEvent.ChangeCourierFilter(courier = null))
            _events.emit(TripEvent.ChangeRouteFilter(route = null))
            _events.emit(TripEvent.ChangeSort(boolean = false))
        }
        _events.emit(TripEvent.RefreshTrips)
        _events.emit(TripEvent.ToggleFilterDialog)
    }
}