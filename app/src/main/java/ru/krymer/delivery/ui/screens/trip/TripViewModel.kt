package ru.krymer.delivery.ui.screens.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.krymer.delivery.AppDatabase
import ru.krymer.delivery.common.EventHandler
import ru.krymer.delivery.di.AppPreferencesManager
import ru.krymer.delivery.data.api.RouteApi
import ru.krymer.delivery.data.api.TripApi
import ru.krymer.delivery.data.api.UserApi
import ru.krymer.delivery.data.model.RouteModel
import ru.krymer.delivery.data.model.TripModel
import ru.krymer.delivery.data.model.user.UserModel
import ru.krymer.delivery.data.request.CreateTripRequest
import ru.krymer.delivery.data.request.UpdateTripRequest
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import ru.krymer.delivery.ui.screens.trip.models.TripAction
import ru.krymer.delivery.ui.screens.trip.models.TripEvent
import ru.krymer.delivery.ui.screens.trip.models.TripViewState
import ru.krymer.delivery.utills.Constants
import ru.krymer.delivery.utills.getStartOfNextDay
import javax.inject.Inject

@HiltViewModel
class TripViewModel @Inject constructor(
    private val tripApi: TripApi,
    private val userApi: UserApi,
    private val routeApi: RouteApi,
    private val sharedViewModel: SharedViewModel,
    private val database: AppDatabase,
    private val manager: AppPreferencesManager
) : ViewModel(), EventHandler<TripEvent> {


    private val _viewState = MutableStateFlow(TripViewState())
    val viewState = _viewState.asStateFlow()

    private fun updateViewState(update: (TripViewState) -> TripViewState) {
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

    override fun obtainEvent(event: TripEvent) {
        when (event) {
            is TripEvent.ShowHideAddDialog -> switchStateAddDialog()
            is TripEvent.ShowUpdateDialog -> showUpdateDialog(event.trip)
            is TripEvent.ShowDeleteDialog -> showDeleteDialog(trip = event.trip)
            is TripEvent.SaveTrip -> saveTrip()
            is TripEvent.OpenHideDropDownMenuWithCouriers -> changeStateDropMenuCourier()
            is TripEvent.OpenHideDropDownMenuWithRoutes -> changeStateDropMenuTrip()
            is TripEvent.SelectCourier -> changeCurrentCourier(event.courier)
            is TripEvent.SelectRoute -> changeCurrentRoute(event.route)
            is TripEvent.OpenHideDatePickerForAddTrip -> changeStateDropMenuDatePicker()
            is TripEvent.ChangeDate -> changeDate(event.date)
            is TripEvent.UpdateTrip -> updateTrip()
            is TripEvent.OpenTrip -> shopsItemClicked(event.trip)
            is TripEvent.TripActionDefault -> tripActionInvoked()
            is TripEvent.DismissDeleteDialog -> dismissDeleteDialog()
            is TripEvent.DismissUpdateDialog -> dismissUpdateDialog()
            is TripEvent.DeleteTrip -> deleteTrip()
            is TripEvent.OpenFilterTrip -> switcherFilterDialog()
            is TripEvent.ChangerCheckBoxFilterCourier -> changeFilterCourier()
            is TripEvent.SubmitFilter -> submitFilter()
            is TripEvent.LoadMoreTrips -> {
                val state = viewState.value
                if (!state.checkBoxIsFilterCourier && state.hasMore && !state.isLoading) {
                    loadPaginatedTrips(loadMore = true)
                }
            }
        }
    }

    init {
        getLocalData()
        loadListDropMenuRoutes()
        loadListDropMenuCouriers()
    }

    private fun submitFilter() {
        launchCoroutine {
            val user = sharedViewModel.viewState.value.user.value
            user?.let { u ->
                val isFilterCourier = viewState.value.checkBoxIsFilterCourier
                manager.saveBoolean(key = Constants.KEYS.COURIER_FILTER, isFilterCourier)
                manager.getBooleanData(key = Constants.KEYS.COURIER_FILTER)
                getAllDataTrips()
            }
            switcherFilterDialog()
        }
    }

    private fun getAllDataTrips() {
        launchCoroutine {
            val user = sharedViewModel.viewState.value.user.value
            if (user != null) {
                val isFilterCourier = viewState.value.checkBoxIsFilterCourier
                if (isFilterCourier) {
                    val response = tripApi.getTrips(idFactory = user.idFactory)
                    if (response.success) {
                        val allTrips = response.obj ?: emptyList()
                        val filteredTrips = allTrips.filter { it.idCourier == user.id }
                        updateViewState {
                            it.copy(
                                trips = MutableStateFlow(filteredTrips.sortedByDescending { it.date }),
                                unFilteredTrips = MutableStateFlow(allTrips.sortedByDescending { it.date }),
                                hasMore = false
                            )
                        }
                    } else {
                        sharedViewModel.message(response.message)
                    }
                } else {
                    loadPaginatedTrips(loadMore = false)
                }
            }
        }
    }

    private fun loadPaginatedTrips(loadMore: Boolean) {
        launchCoroutine {
            val isLoading = viewState.value.isLoading
            if (isLoading) return@launchCoroutine
            updateViewState { it.copy(isLoading = true) }
            val user = sharedViewModel.viewState.value.user.value
            if (user != null) {
                val limit = 10
                val offset = if (loadMore) viewState.value.trips.value.size.toLong() else 0
                val response = tripApi.gePaginatedTrips(
                    idFactory = user.idFactory,
                    limit = limit,
                    offset = offset
                )
                if (response.success) {
                    val newTrips = response.obj ?: emptyList()
                    val currentTrips = if (loadMore) {
                        viewState.value.trips.value.toMutableList().apply { addAll(newTrips) }
                    } else {
                        newTrips.toMutableList()
                    }
                    val newHasMore = newTrips.size == limit
                    updateViewState {
                        it.copy(
                            trips = MutableStateFlow(currentTrips.sortedByDescending { it.date }),
                            unFilteredTrips = MutableStateFlow(currentTrips.sortedByDescending { it.date }),
                            hasMore = newHasMore,
                            isLoading = false
                        )
                    }
                } else {
                    sharedViewModel.message(response.message)
                    updateViewState { it.copy(isLoading = false) }
                }
            } else {
                sharedViewModel.message(Constants.ERROR.GENERAL_ERROR)
                updateViewState { it.copy(isLoading = false) }
            }
        }
    }

    private fun changeFilterCourier() {
        val bool = !viewState.value.checkBoxIsFilterCourier
        updateViewState { it.copy(checkBoxIsFilterCourier = bool) }
    }

    private fun switcherFilterDialog() {
        updateViewState { it.copy(isShowFilterDialog = !it.isShowFilterDialog) }
    }

    private fun getLocalData() {
        launchCoroutine {
            val isFilterCourier = manager.getBooleanData(Constants.KEYS.COURIER_FILTER)
            isFilterCourier?.let { filter ->
                updateViewState {
                    it.copy(checkBoxIsFilterCourier = filter)
                }
            }
            val localTrips = database.tripDao().getTrips().sortedByDescending { it.date }
            if (localTrips.isNotEmpty()) {
                updateViewState { it.copy(trips = MutableStateFlow(localTrips), unFilteredTrips = MutableStateFlow(localTrips) ) }
            }
            getAllDataTrips()
        }
    }


    private fun tripActionInvoked() {
        updateViewState { it.copy(tripAction = TripAction.None) }
    }

    private fun updateTrip() {
        launchCoroutine {
            val trip = viewState.value.currentTrip
            val route = viewState.value.currentRoute
            val courier = viewState.value.currentCourier
            val date = viewState.value.currentDate

            if (route != null && courier != null && trip != null) {
                val tripRequest = UpdateTripRequest(
                    id = trip.id,
                    factoryId = trip.idFactory,
                    date = date,
                    courierId = courier.id,
                    routeId = route.id,
                    salary = courier.salary,
                    percentCourier = courier.percentSalary,
                    priceMillage = trip.priceMillage,
                    millage = trip.millage,
                    nameCourier = courier.name,
                    nameRoute = route.name,
                )
                val response = tripApi.update(trip = tripRequest)
                if (response.success) {
                    val list = viewState.value.trips.value.map { it.copy() }.toMutableList()
                    val index = list.indexOfFirst { it.id == trip.id }
                    list[index] = trip.copy(
                        nameRoute = route.name,
                        nameCourier = courier.name,
                        salary = courier.salary,
                        percentCourier = courier.percentSalary,
                        idRoute = route.id,
                        date = date,
                        idCourier = courier.id
                    )
                    updateViewState { it.copy(trips = MutableStateFlow(list.sortedByDescending { l -> l.date })) }
                    dismissUpdateDialog()
                } else {
                    sharedViewModel.message(response.message)
                }
            }
        }
    }

    private fun showUpdateDialog(trip: TripModel) {
        val routes = viewState.value.listRoute
        val couriers = viewState.value.listCourier
        updateViewState {
            it.copy(showUpdateSheetDialog = true,
                currentTrip = trip,
                currentDate = trip.date,
                currentCourier = couriers.value.first { c -> c.id == trip.idCourier },
                currentRoute = routes.value.first { r -> r.id == trip.idRoute })
        }
    }

    private fun changeDate(date: Long) {
        updateViewState { it.copy(currentDate = date) }
    }

    private fun changeStateDropMenuDatePicker() {
        updateViewState { it.copy(dropDownStateDatePicker = !it.dropDownStateDatePicker) }
    }

    private fun changeCurrentRoute(route: RouteModel) {
        updateViewState { it.copy(currentRoute = route) }
    }

    private fun changeCurrentCourier(courier: UserModel) {
        updateViewState { it.copy(currentCourier = courier) }
    }

    private fun saveTrip() {
        launchCoroutine {
            val curRoute = viewState.value.currentRoute
            val curCourier = viewState.value.currentCourier
            val date = viewState.value.currentDate
            if (curRoute != null && curCourier != null) {
                val tripRequest = CreateTripRequest(
                    factoryId = curRoute.idFactory,
                    date = date,
                    courierId = curCourier.id,
                    routeId = curRoute.id,
                    salary = curCourier.salary,
                    percentCourier = curCourier.percentSalary,
                    priceMillage = curCourier.salary,
                    nameRoute = curRoute.name,
                    nameCourier = curCourier.name
                )
                val response = tripApi.add(trip = tripRequest)
                if (response.success) {
                    switchStateAddDialog()
                    val trip = response.obj
                    trip?.let {
                        val list = viewState.value.trips.value.map { it.copy() }.toMutableList()
                        list.add(trip)
                        updateViewState { it.copy(trips = MutableStateFlow(list.sortedByDescending { t -> t.date })) }
                    }
                } else {
                    sharedViewModel.message(response.message)
                }
            } else {
                sharedViewModel.message(message = Constants.ERROR.AGAIN)
            }
        }
    }

    private fun switchStateAddDialog() {
        updateViewState {
            it.copy(
                stateAddDialog = !it.stateAddDialog,
                currentDate = getStartOfNextDay()
            )
        }
    }

    private fun loadListDropMenuRoutes() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = sharedViewModel.viewState.value.user.value
                if (user != null) {
                    val responseRoute = routeApi.getRoutes(user.idFactory)
                    if (responseRoute.success) {
                        val routes = responseRoute.obj
                        if (!routes.isNullOrEmpty()) {
                            val list = routes.sortedBy { r -> r.name }
                            updateViewState {
                                it.copy(
                                    listRoute = MutableStateFlow(list),
                                    currentRoute = list[0]
                                )
                            }
                        } else {
                            sharedViewModel.message(Constants.EMPTY.EMPTY_LIST + Constants.ADD.ROUTE)
                        }
                    } else {
                        sharedViewModel.message(responseRoute.message)
                    }
                } else {
                    sharedViewModel.message(Constants.ERROR.GENERAL_ERROR)
                }
            } catch (e: Exception) {
                sharedViewModel.message(message = e.message)
            }
        }
    }

    private fun loadListDropMenuCouriers() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = sharedViewModel.viewState.value.user.value
                if (user != null) {
                    val responseUsers = userApi.getUsers(user.idFactory)
                    if (responseUsers.success) {
                        val users = responseUsers.obj
                        if (!users.isNullOrEmpty()) {
                            val list = users.sortedBy { r -> r.name }
                            updateViewState {
                                it.copy(
                                    listCourier = MutableStateFlow(list),
                                    currentCourier = list[0]
                                )
                            }
                        } else {
                            sharedViewModel.message(Constants.EMPTY.EMPTY_LIST + Constants.ADD.COURIER)
                        }
                    } else {
                        sharedViewModel.message(responseUsers.message)
                    }
                } else {
                    sharedViewModel.message(Constants.ERROR.GENERAL_ERROR)
                }
            } catch (e: Exception) {
                sharedViewModel.message(message = e.message)
            }
        }
    }


    private fun changeStateDropMenuTrip() {
        updateViewState { it.copy(dropDownStateRoutes = !it.dropDownStateRoutes) }
    }

    private fun changeStateDropMenuCourier() {
        updateViewState { it.copy(dropDownStateCourier = !it.dropDownStateCourier) }
    }

    private fun showDeleteDialog(trip: TripModel) {
        updateViewState {
            it.copy(
                showDeleteDialog = true, deleteTrip = trip
            )
        }
    }


    private fun deleteTrip() {
       launchCoroutine {
           val trip = viewState.value.deleteTrip
           if (trip != null) {
               val response = tripApi.delete(id = trip.id)
               if (response.success) {
                   val list = viewState.value.trips.value.map { it.copy() }.toMutableList()
                   val item = list.first { it.id == trip.id }
                   val listNew = (list - item).sortedByDescending { it.date }
                   updateViewState { it.copy(trips = MutableStateFlow(listNew)) }
                   dismissDeleteDialog()
               } else {
                   sharedViewModel.message(response.message)
               }
           } else {
               sharedViewModel.message(Constants.ERROR.GENERAL_ERROR)
           }
       }
    }

    private fun dismissDeleteDialog() {
        updateViewState {
            it.copy(
                showDeleteDialog = false,
            )
        }
    }

    private fun dismissUpdateDialog() {
        updateViewState {
            it.copy(
                showUpdateSheetDialog = false,
                currentRoute = viewState.value.listRoute.value[0],
                currentCourier = viewState.value.listCourier.value[0],
            )
        }
    }

    private fun shopsItemClicked(trip: TripModel) {
        launchCoroutine {
            sharedViewModel.updateViewState { it.copy(currentTrip = trip) }
            updateViewState { it.copy(tripAction = TripAction.OpenShops) }
            val localTrip = database.tripDao().getTripById(trip.id)
            if (localTrip != null) {
                database.tripDao().updateTrip(trip)
            } else {
                database.tripDao().insertTrip(trip)
            }
        }
    }
}