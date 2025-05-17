package ru.krymer.delivery.ui.screens.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
import java.util.Calendar
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
    val viewState: StateFlow<TripViewState> = _viewState

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
            is TripEvent.ShowAddDialog -> showAddDialog()
            is TripEvent.ShowChangeCourierDialog -> showUpdateDialog(event.trip)
            is TripEvent.ShowDeleteDialog -> showDeleteDialog(trip = event.trip)
            is TripEvent.TripSaveAction -> saveTrip()
            is TripEvent.ChangeDropDownStateCourier -> changeStateDropMenuCourier(event.state)
            is TripEvent.ChangeDropDownStateTrip -> changeStateDropMenuTrip(event.state)
            is TripEvent.SelectDropDownCourier -> changeCurrentCourier(event.courier)
            is TripEvent.SelectDropDownRoute -> changeCurrentRoute(event.route)
            is TripEvent.ChangeDropDownStateDatePicker -> changeStateDropMenuDatePicker(event.state)
            is TripEvent.ChangeDate -> changeDate(event.date)
            is TripEvent.TripUpdateAction -> updateTrip()
            is TripEvent.TripItemClicked -> shopsItemClicked(event.trip)
            TripEvent.TripActionInvoked -> tripActionInvoked()
            TripEvent.DismissAddDialog -> dismissAddDialog()
            TripEvent.DismissDeleteDialog -> dismissDeleteDialog()
            TripEvent.DismissUpdateDialog -> dismissUpdateDialog()
            TripEvent.DeleteTrip -> deleteTrip()
            TripEvent.SwitcherFilterDialog -> switcherFilterDialog()
            is TripEvent.ChangerCheckBoxFilterCourier -> changeFilterCourier()
            TripEvent.SubmitFilter -> submitFilter()
        }
    }

    private fun submitFilter() {
        launchCoroutine {
            val user = sharedViewModel.viewState.value.user.value
            user?.let { u ->
                val isFilterCourier = viewState.value.checkBoxIsFilterCourier
                manager.saveBoolean(key = Constants.KEYS.COURIER_FILTER, isFilterCourier)
                manager.getBooleanData(key = Constants.KEYS.COURIER_FILTER)
                updateViewState {
                    it.copy(
                        trips = if (isFilterCourier) MutableStateFlow(it.trips.value.filter { it.idCourier == u.id }) else MutableStateFlow(
                            it.unFilteredTrips.value
                        )
                    )
                }
            }
            switcherFilterDialog()
        }
    }

    private fun changeFilterCourier() {
        val bool = !viewState.value.checkBoxIsFilterCourier
        updateViewState { it.copy(checkBoxIsFilterCourier = bool) }
    }

    private fun switcherFilterDialog() {
        updateViewState { it.copy(isShowFilterDialog = !it.isShowFilterDialog) }
    }

    init {
        getLocalData()

        loadListDropMenuRoutes()
        loadListDropMenuCouriers()
    }

    private fun getLocalData() {
        launchCoroutine {
            val isFilterCourier = manager.getBooleanData(Constants.KEYS.COURIER_FILTER)
            isFilterCourier?.let { filter ->
                updateViewState {
                    it.copy(checkBoxIsFilterCourier = filter)
                }
            }
            getDataTrips()
        }
    }

    fun getDataTrips() {
        launchCoroutine {
            val user = sharedViewModel.viewState.value.user.value
            if (user != null) {
                val trips = database.tripDao().getTrips().sortedByDescending { it.date }
                if (trips.isNotEmpty()) {
                    updateViewState { it.copy(trips = MutableStateFlow(trips)) }
                }
                val response =
                    tripApi.getTrips(idFactory = user.idFactory)
                if (response.success) {
                    val trips = response.obj
                    if (!trips.isNullOrEmpty()) {
                        if (viewState.value.checkBoxIsFilterCourier) {
                            updateViewState {
                                it.copy(
                                    unFilteredTrips = MutableStateFlow(trips),
                                    trips = MutableStateFlow(trips.filter { it.idCourier == user.id })
                                )
                            }
                        } else {
                            updateViewState {
                                it.copy(
                                    trips = MutableStateFlow(trips),
                                    unFilteredTrips = MutableStateFlow(trips)
                                )
                            }
                        }
                    } else {
                        delay(5000)
                        getDataTrips()
                    }
                } else {
                    sharedViewModel.message(response.message)
                }
            }
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

    private fun changeStateDropMenuDatePicker(state: Boolean) {
        updateViewState { it.copy(dropDownStateDatePicker = state) }
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
            val factory = sharedViewModel.viewState.value.factory
            val date = viewState.value.currentDate
            if (curRoute != null && curCourier != null && factory != null) {
                val tripRequest = CreateTripRequest(
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
                val response = tripApi.add(trip = tripRequest)
                if (response.success) {
                    val trip = response.obj
                    if (trip != null) {
                        val list = viewState.value.trips.value.map { it.copy() }.toMutableList()
                        list.add(trip)
                        updateViewState { it.copy(trips = MutableStateFlow(list.sortedByDescending { t -> t.date })) }
                    } else {
                        sharedViewModel.message(message = Constants.ERROR.ERROR)
                    }
                    dismissAddDialog()
                } else {
                    sharedViewModel.message(response.message)
                }
            } else {
                sharedViewModel.message(message = Constants.ERROR.ERROR)
            }
        }
    }

    private fun showAddDialog() {
        updateViewState {
            it.copy(
                showAddSheetDialog = true
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


    private fun changeStateDropMenuTrip(state: Boolean) {
        updateViewState { it.copy(dropDownStateTrips = state) }
    }

    private fun changeStateDropMenuCourier(state: Boolean) {
        updateViewState { it.copy(dropDownStateCourier = state) }
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

    private fun dismissAddDialog() {
        updateViewState {
            it.copy(
                showAddSheetDialog = false,
                currentDate = Calendar.getInstance().timeInMillis + 86400000
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