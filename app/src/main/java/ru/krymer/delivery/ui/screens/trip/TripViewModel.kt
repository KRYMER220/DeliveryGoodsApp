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
import ru.krymer.delivery.data.model.TripModel
import ru.krymer.delivery.data.model.user.UserModel
import ru.krymer.delivery.data.model.utilModel.TypeMessageModel
import ru.krymer.delivery.data.repositoryImpl.CourierRepositoryImpl
import ru.krymer.delivery.data.repositoryImpl.RouteRepositoryImpl
import ru.krymer.delivery.data.repositoryImpl.TripRepositoryImpl
import ru.krymer.delivery.data.request.TripRequest
import ru.krymer.delivery.di.AppPreferencesManager
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import ru.krymer.delivery.ui.screens.trip.models.TripEvent
import ru.krymer.delivery.ui.screens.trip.models.TripEvent.*
import ru.krymer.delivery.ui.screens.trip.models.TripViewState
import ru.krymer.delivery.utills.Constants
import ru.krymer.delivery.utills.MyResult
import ru.krymer.delivery.utills.getStartOfNextDay
import java.io.IOException
import java.net.UnknownHostException
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
    companion object {
        private const val PAGINATION_LIMIT = 10
    }

    private fun launchCoroutine(block: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                block()
            } catch (_: UnknownHostException) {
                sharedViewModel.message("Отсутствует интернет соединение")
            } catch (e: IOException) {
                sharedViewModel.message("Ошибка сети: ${e.message}")
            } catch (e: CancellationException) {
                _events.emit(Error(Constants.ERROR.CANCEL_OPERATION))
                throw e
            } catch (e: TimeoutCancellationException) {
                _events.emit(Error(Constants.ERROR.TIMEOUT))
                throw e
            } catch (e: Exception) {
                _events.emit(Error(e.message))
                throw e
            }
        }
    }

    private fun getCurrentUser(): UserModel? {
        return sharedViewModel.viewState.value.user
    }

    private suspend fun getCurrentUserOrEmitError(): UserModel? {
        val user = getCurrentUser()
        if (user == null) {
            _events.emit(Error(Constants.ERROR.GENERAL_ERROR))
        }
        return user
    }

    val viewState: StateFlow<TripViewState> = _events
        .onStart {
            emit(LoadListDropMenuRoutes)
            emit(LoadListDropMenuCouriers)
            emit(LoadSettings)
            emit(LocalData)
        }
        .runningFold(TripViewState()) { state, event ->
            when (event) {
                DeleteTrip -> {
                    launchCoroutine { deleteTrip() }
                    state
                }

                is ToggleDeleteDialog -> state.copy(toggleDeleteTrip = !state.toggleDeleteTrip, trip = event.trip)

                is ToggleUpdateDialog -> {
                    val isOpening = !state.toggleUpdateTrip

                    if (isOpening) {
                        state.copy(
                            trip = event.trip,
                            salary = event.trip?.salary?.toInt().toString(),
                            currentRoute = state.listRoute.firstOrNull { it.id == event.trip?.idRoute },
                            currentCourier = state.listCourier.firstOrNull { it.id == event.trip?.idCourier },
                            currentDate = event.trip?.date ?: getStartOfNextDay(),
                            toggleUpdateTrip = true
                        )
                    } else {
                        state.copy( toggleUpdateTrip = false)
                    }
                }

                RefreshTrips -> {
                    launchCoroutine { loadTrips() }
                    state.copy(isLoading = true)
                }

                SaveTrip -> {
                    launchCoroutine { createTrip() }
                    state
                }

                SubmitFilter -> {
                    launchCoroutine { submitFilter() }
                    state
                }

                UpdateTrip -> {
                    launchCoroutine { updateTrip() }
                    state
                }

                is ChangeCourierFilter -> state.copy(filterUid = event.courier?.id)

                is ChangeDate -> state.copy(currentDate = event.date)

                is ChangeRouteFilter -> state.copy(filterRouteId = event.route?.id)

                is ChangeSalaryTrip -> state.copy(salary = event.salary)

                is ChangeSort -> {
                    val newSort = event.boolean
                    manager.saveBoolean(Constants.KEYS.SORT, newSort)
                    state.copy(sort = newSort)
                }

                LoadListDropMenuCouriers -> {
                    launchCoroutine { loadListDropMenuCouriers() }
                    state
                }

                LoadListDropMenuRoutes -> {
                    launchCoroutine { loadListDropMenuRoutes() }
                    state
                }

                LoadMoreTrips -> {
                    val currentState = state
                    if (!currentState.isFilter && currentState.hasMore && !currentState.isLoading) {
                        launchCoroutine { loadPaginatedTrips(loadMore = true) }
                    }
                    state.copy(isLoading = true)
                }

                LoadSettings -> {
                    val isFilter = manager.getBooleanData(Constants.KEYS.FILTER) == true
                    val isSorted = manager.getBooleanData(Constants.KEYS.SORT) == true
                    val lightVersion = sharedViewModel.viewState.value.lightVersion
                    state.copy(isFilter = isFilter, sort = isSorted, lightVersion = lightVersion)
                }

                ToggleFilterDialog -> state.copy(isShowFilterDialog = !state.isShowFilterDialog)

                ToggleAddDialog -> state.copy(
                    toggleAddTrip = !state.toggleAddTrip,
                    currentDate = getStartOfNextDay(),
                )

                is TripsLoaded -> state.copy(trips = event.trips, isLoading = false, hasMore = event.hasMore, isInitialLoad = false )

                is ListCouriersLoaded -> state.copy(listCourier = event.couriers)

                is ListRoutesLoaded -> state.copy(listRoute = event.routes)

                is Error -> {
                    sharedViewModel.message(event.message, type = TypeMessageModel.ERROR)
                    state.copy(isLoading = false)
                }

                is IsFilter -> {
                    manager.saveBoolean(Constants.KEYS.FILTER, event.isFilter)
                    state.copy(isFilter = event.isFilter)
                }

                is SelectCourier -> state.copy(currentCourier = event.courier)
                is SelectRoute -> state.copy(currentRoute = event.route)
                LocalData -> {
                    launchCoroutine {
                        loadLocalData()
                    }
                    state.copy(isInitialLoad = true)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TripViewState())

    fun obtainEvent(event: TripEvent) {
        _events.tryEmit(event)
    }

    private suspend fun loadLocalData() {
        val user = getCurrentUserOrEmitError() ?: return
        val currentState = viewState.value
        val isFilter = currentState.isFilter
        val isSorted = currentState.sort
        
        val localTrips = database.tripDao().getTrips().let { trips ->
            val filtered = if (isFilter) {
                trips.filter { it.idCourier == user.id }
            } else {
                trips
            }
            if (isSorted) {
                filtered.sortedBy { it.date }
            } else {
                filtered.sortedByDescending { it.date }
            }
        }

        if (localTrips.isNotEmpty()) {
            _events.emit(TripsLoaded(trips = localTrips, hasMore = false))
        }

        loadTrips()
    }

    private suspend fun loadTrips() {
        val user = getCurrentUserOrEmitError() ?: return
        val currentState = viewState.value
        val uid = currentState.filterUid
        val routeId = currentState.filterRouteId
        val sort = currentState.sort
        val isFilter = currentState.isFilter

        if (isFilter) {
            when (val response = repository.getTrips(
                idFactory = user.idFactory,
                uid = uid,
                routeId = routeId,
                sortBy = if (sort) Constants.SORT.ASC else Constants.SORT.DESC
            )) {
                is MyResult.Success -> {
                    syncLocalDatabase(response.data, clearAll = true)
                    _events.emit(TripsLoaded(trips = response.data, hasMore = false))
                }
                is MyResult.Error -> _events.emit(Error(message = response.message))
            }
        } else {
            loadPaginatedTrips(loadMore = false)
        }
    }

    private suspend fun syncLocalDatabase(
        serverTrips: List<TripModel>,
        clearAll: Boolean = false
    ) {
        try {
            if (clearAll) {
                val localTrips = database.tripDao().getTrips()
                val serverTripIds = serverTrips.map { it.id }.toSet()
                
                val tripsToDelete = localTrips.filter { localTrip ->
                    !serverTripIds.contains(localTrip.id)
                }
                
                tripsToDelete.forEach { trip ->
                    database.tripDao().deleteTrip(trip)
                }
            }

            serverTrips.forEach { serverTrip ->
                database.tripDao().upsertTrip(serverTrip)
            }
        } catch (e: Exception) {
            println("Ошибка при синхронизации локальной базы: ${e.message}")
        }
    }

    private suspend fun createTrip() {
        val factory = sharedViewModel.viewState.value.factory ?: run {
            _events.emit(Error(Constants.ERROR.AGAIN))
            return
        }
        val currentState = viewState.value
        val curRoute = currentState.currentRoute
        val curCourier = currentState.currentCourier
        val date = currentState.currentDate + Random.nextInt(from = 1, until = 1000)

        if (curRoute == null || curCourier == null) {
            _events.emit(Error(Constants.ERROR.AGAIN))
            return
        }

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

        when (repository.add(trip = tripRequest)) {
            is MyResult.Success -> {
                _events.emit(ToggleAddDialog)
                _events.emit(RefreshTrips)
            }
            is MyResult.Error -> _events.emit(Error(message = "Ошибка создания рейса!"))
        }
    }

    private suspend fun updateTrip() {
        val currentState = viewState.value
        val trip = currentState.trip
        val route = currentState.currentRoute
        val courier = currentState.currentCourier
        val date = currentState.currentDate + Random.nextInt(from = 1, until = 1000)
        val salary = currentState.salary

        if (route == null || courier == null || trip == null) {
            _events.emit(Error(Constants.ERROR.GENERAL_ERROR))
            return
        }

        val tripRequest = TripRequest(
            id = trip.id,
            factoryId = trip.idFactory,
            date = date,
            courierId = courier.id,
            routeId = route.id,
            salary = salary.ifBlank { courier.salary.toString() }.toDoubleOrNull() ?: courier.salary,
            percentCourier = courier.percentSalary,
            priceMillage = trip.priceMillage,
            millage = trip.millage,
            nameCourier = courier.name,
            nameRoute = route.name,
        )

        when (repository.update(trip = tripRequest)) {
            is MyResult.Success -> {
                _events.emit(RefreshTrips)
                _events.emit(ToggleUpdateDialog(trip = null))
            }
            is MyResult.Error -> _events.emit(Error(message = "Ошибка обновления рейса!"))
        }
    }

    private suspend fun deleteTrip() {
        val currentState = viewState.value
        val trip = currentState.trip
        if (trip == null) {
            _events.emit(Error(Constants.ERROR.GENERAL_ERROR))
            return
        }

        when (repository.delete(id = trip.id)) {
            is MyResult.Success -> {
                database.tripDao().deleteTrip(trip)
                _events.emit(RefreshTrips)
                _events.emit(ToggleDeleteDialog(trip = null))
            }
            is MyResult.Error -> _events.emit(Error(message = "Ошибка удаления рейса!"))
        }
    }

    private suspend fun loadListDropMenuRoutes() {
        val user = getCurrentUserOrEmitError() ?: return
        when (val response = repositoryRoute.getRoutes(user.idFactory)) {
            is MyResult.Success -> {
                _events.emit(ListRoutesLoaded(routes = response.data))
            }
            is MyResult.Error -> _events.emit(Error(message = "Ошибка загрузки списка маршрутов!"))
        }
    }

    private suspend fun loadListDropMenuCouriers() {
        val user = getCurrentUserOrEmitError() ?: return
        when (val response = repositoryCourier.getUsers(user.idFactory)) {
            is MyResult.Success -> {
                _events.emit(ListCouriersLoaded(couriers = response.data))
            }
            is MyResult.Error -> _events.emit(Error(message = "Ошибка загрузки списка курьеров!"))
        }
    }

    private suspend fun loadPaginatedTrips(loadMore: Boolean) {
        val user = getCurrentUserOrEmitError() ?: return
        val currentState = viewState.value
        val lastTrip = if (loadMore && currentState.trips.isNotEmpty()) {
            currentState.trips.last()
        } else {
            null
        }

        when (val response = repository.getPaginatedTrips(
            idFactory = user.idFactory,
            limit = PAGINATION_LIMIT,
            lastDate = lastTrip?.date,
            lastId = lastTrip?.id
        )) {
            is MyResult.Success -> {
                val newTrips = response.data

                if (!loadMore) {
                    syncLocalDatabase(newTrips, clearAll = true)
                } else {
                    syncLocalDatabase(newTrips, clearAll = false)
                }

                val currentTrips = if (loadMore) {
                    currentState.trips + newTrips
                } else {
                    newTrips
                }

                val newHasMore = newTrips.size == PAGINATION_LIMIT
                _events.emit(TripsLoaded(trips = currentTrips, hasMore = newHasMore))
            }
            is MyResult.Error -> _events.emit(Error(message = "Ошибка загрузки списка рейсов!"))
        }
    }

    private suspend fun submitFilter() {
        val currentState = viewState.value
        if (!currentState.isFilter) {
            _events.emit(ChangeCourierFilter(courier = null))
            _events.emit(ChangeRouteFilter(route = null))
            _events.emit(ChangeSort(boolean = false))
        }
        _events.emit(RefreshTrips)
        _events.emit(ToggleFilterDialog)
    }
}