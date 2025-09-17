package ru.krymer.delivery.ui.screens.trip

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.krymer.delivery.AppDatabase
import ru.krymer.delivery.common.EventHandler
import ru.krymer.delivery.data.api.RouteApi
import ru.krymer.delivery.data.api.TripApi
import ru.krymer.delivery.data.api.UserApi
import ru.krymer.delivery.data.model.RouteModel
import ru.krymer.delivery.data.model.TripModel
import ru.krymer.delivery.data.model.user.RoleModel
import ru.krymer.delivery.data.model.user.StatusModel
import ru.krymer.delivery.data.model.user.UserModel
import ru.krymer.delivery.data.model.utilModel.TypeMessageModel
import ru.krymer.delivery.data.request.CreateTripRequest
import ru.krymer.delivery.data.request.UpdateTripRequest
import ru.krymer.delivery.di.AppPreferencesManager
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
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
            } catch (e: kotlin.coroutines.cancellation.CancellationException) {
                throw e
                sharedViewModel.message(Constants.ERROR.CANCEL_OPERATION, type = TypeMessageModel.ERROR)
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
            is TripEvent.DismissDeleteDialog -> dismissDeleteDialog()
            is TripEvent.DismissUpdateDialog -> dismissUpdateDialog()
            is TripEvent.DeleteTrip -> deleteTrip()
            is TripEvent.OpenFilterTrip -> switcherFilterDialog()
            is TripEvent.IsFilter -> changeFilter(event.boolean)
            is TripEvent.SubmitFilter -> submitFilter()
            is TripEvent.LoadMoreTrips -> {
                val state = viewState.value
                if (!state.isFilter && state.hasMore && !state.isLoading) {
                    loadPaginatedTrips(loadMore = true)
                }
            }

            is TripEvent.ChangeSalaryTrip -> changeSalaryTrip(event.salary)
            is TripEvent.ChangeCourierFilter -> changeCourier(event.courier)
            is TripEvent.ChangeRouteFilter -> changeRoute(event.route)
            is TripEvent.ChangeSort -> changeSort(event.boolean)
            is TripEvent.SyncTrip -> syncTrip(trip = event.trip)
        }
    }

    private fun syncTrip(trip: TripModel) {
        launchCoroutine {
            val response = tripApi.getTrip(trip.id)
            val tripResponse = response.obj
            if (response.success && tripResponse != null) {
                updateViewState { it.copy(currentTrip = tripResponse) }
                val trips = viewState.value.trips.map { tripItem ->
                    if (tripItem.id == tripResponse.id) tripResponse else tripItem
                }
                updateViewState { it.copy(trips = trips) }
            }
        }
    }


    private fun changeSort(boolean: Boolean) {
        updateViewState { it.copy(sort = !boolean) }
        manager.saveBoolean(Constants.KEYS.SORT, !boolean)
    }

    private fun changeCourier(user: UserModel?) {
        updateViewState { it.copy(filterUid = user?.id, currentCourier = user) }
    }

    private fun changeRoute(route: RouteModel?) {
        updateViewState { it.copy(filterRouteId = route?.id, currentRoute = route) }
    }

    private fun changeSalaryTrip(salaryTrip: String) {
        updateViewState { it.copy(salary = salaryTrip) }
    }

    init {
        getLocalData()
        loadListDropMenuRoutes()
        loadListDropMenuCouriers()
        getSettings()
    }

    private fun getSettings() {
        val value = sharedViewModel.viewState.value.lightVersion
        updateViewState { it.copy(lightVersion = value) }
    }

    private fun submitFilter() {
        launchCoroutine {
            val isFilter = viewState.value.isFilter
            if (!isFilter) {
                updateViewState { it.copy(filterRouteId = null, filterUid = null, sort = false) }
            }
            getLocalData()
            switcherFilterDialog()
        }
    }

    private fun changeFilter(boolean: Boolean) {
        updateViewState { it.copy(isFilter = !boolean) }
        manager.saveBoolean(key = Constants.KEYS.FILTER, !boolean)
    }

    private fun getAllDataTrips() {
        launchCoroutine {
            val user = sharedViewModel.viewState.value.user
            val uid = viewState.value.filterUid
            val routeId = viewState.value.filterRouteId
            val sort = viewState.value.sort
            if (user != null) {
                val isFilter = viewState.value.isFilter
                if (isFilter) {
                    val response = tripApi.getTrips(
                        idFactory = user.idFactory,
                        uid = uid,
                        routeId = routeId,
                        sortBy = if (sort) Constants.SORT.ASC else Constants.SORT.DESC
                    )
                    if (response.success) {
                        val trips = response.obj ?: emptyList()
                        updateViewState {
                            it.copy(
                                trips = trips,
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
            val user = sharedViewModel.viewState.value.user

            if (user != null) {
                val limit = 10
                val lastTrip = if (loadMore && viewState.value.trips.isNotEmpty()) {
                    viewState.value.trips.last()
                } else {
                    null
                }

                val response = tripApi.gePaginatedTrips(
                    idFactory = user.idFactory,
                    limit = limit,
                    lastDate = lastTrip?.date,
                    lastId = lastTrip?.id
                )

                if (response.success) {
                    val newTrips = response.obj ?: emptyList()
                    val currentTrips = if (loadMore) {
                        viewState.value.trips.toMutableList().apply { addAll(newTrips) }
                    } else {
                        newTrips.toMutableList()
                    }

                    val newHasMore = newTrips.size == limit
                    updateViewState {
                        it.copy(
                            trips = currentTrips,
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



    private fun switcherFilterDialog() {
        updateViewState { it.copy(isShowFilterDialog = !it.isShowFilterDialog) }
    }

    private fun getLocalData() {
        launchCoroutine {
            val isFilter = manager.getBooleanData(Constants.KEYS.FILTER) == true
            val isSorted = manager.getBooleanData(Constants.KEYS.SORT) == true
            updateViewState { it.copy(isFilter = isFilter, sort = isSorted) }
            val user = sharedViewModel.viewState.value.user
            user?.let {
                if (!isFilter) {
                    val localTrips = database.tripDao().getTrips().sortedByDescending { it.date }
                    if (localTrips.isNotEmpty()) {
                        updateViewState {
                            it.copy(
                                trips = localTrips,
                            )
                        }
                    }
                } else {
                    val localTrips =
                        database.tripDao().getTrips().filter { it.idCourier == user.id }
                            .sortedByDescending { it.date }
                    if (localTrips.isNotEmpty()) {
                        updateViewState {
                            it.copy(
                                trips = localTrips,
                            )
                        }
                    }
                }
                getAllDataTrips()
            }
        }
    }

    private fun updateTrip() {
        launchCoroutine {
            val trip = viewState.value.currentTrip
            val route = viewState.value.currentRoute
            val courier = viewState.value.currentCourier
            val date = viewState.value.currentDate
            val salary = viewState.value.salary
            if (route != null && courier != null && trip != null) {
                val tripRequest = UpdateTripRequest(
                    id = trip.id,
                    factoryId = trip.idFactory,
                    date = date,
                    courierId = courier.id,
                    routeId = route.id,
                    salary = if (salary == "") courier.salary else salary.toDouble(),
                    percentCourier = courier.percentSalary,
                    priceMillage = trip.priceMillage,
                    millage = trip.millage,
                    nameCourier = courier.name,
                    nameRoute = route.name,
                )
                val response = tripApi.update(trip = tripRequest)
                if (response.success) {
                    val list = viewState.value.trips.map { it.copy() }.toMutableList()
                    val index = list.indexOfFirst { it.id == trip.id }
                    val newTrip = trip.copy(
                        nameRoute = tripRequest.nameRoute,
                        nameCourier = tripRequest.nameCourier,
                        salary = tripRequest.salary,
                        percentCourier = tripRequest.percentCourier,
                        idRoute = tripRequest.routeId,
                        date = tripRequest.date,
                        idCourier = tripRequest.courierId
                    )
                    list[index] = newTrip
                    updateViewState { it.copy(trips = list.sortedByDescending { l -> l.date }) }
                    dismissUpdateDialog()
                } else {
                    sharedViewModel.message(response.message)
                }
            }
        }
    }

    private fun showUpdateDialog(trip: TripModel) {
        var courier = viewState.value.listCourier.firstOrNull() { it.id == trip.idCourier }
        var route = viewState.value.listRoute.firstOrNull() { it.id == trip.idRoute }
        if (courier == null) {
            courier = UserModel(
                id = trip.idCourier,
                email = "",
                login = "",
                password = "",
                idFactory = trip.idFactory,
                name = trip.nameCourier,
                phone = "",
                status = StatusModel.OFFLINE,
                isBan = false,
                role = RoleModel.USER,
                percentSalary = trip.percentCourier,
                salary = trip.salaryCourier
            )
        }
        if (route == null) {
            route = RouteModel(
                id = trip.idRoute,
                name = trip.nameRoute,
                date = trip.date,
                idFactory = trip.idFactory
            )
        }
        updateViewState {
            it.copy(
                salary = "${trip.salary.toInt()}",
                currentTrip = trip,
                currentDate = trip.date,
                showUpdateSheetDialog = true,
                currentCourier = courier,
                currentRoute = route
            )

        }
    }

    private fun changeDate(date: Long) {
        updateViewState { it.copy(currentDate = date) }
    }

    private fun changeStateDropMenuDatePicker() {
        updateViewState { it.copy(dropDownStateDatePicker = !it.dropDownStateDatePicker) }
    }

    private fun changeCurrentRoute(route: RouteModel?) {
        updateViewState { it.copy(currentRoute = route) }
    }

    private fun changeCurrentCourier(courier: UserModel?) {
        updateViewState { it.copy(currentCourier = courier, salary = courier?.salary.toString()) }
    }

    private fun saveTrip() {
        launchCoroutine {
            val factory = sharedViewModel.viewState.value.factory
            val curRoute = viewState.value.currentRoute
            val curCourier = viewState.value.currentCourier
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
                    switchStateAddDialog()
                    getAllDataTrips()
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
                currentDate = getStartOfNextDay(),
                currentCourier = null,
                currentRoute = null
            )
        }
    }

    private fun loadListDropMenuRoutes() {
        launchCoroutine {
            val user = sharedViewModel.viewState.value.user
            if (user != null) {
                val responseRoute = routeApi.getRoutes(user.idFactory)
                if (responseRoute.success) {
                    val routes = responseRoute.obj
                    if (!routes.isNullOrEmpty()) {
                        val list = routes.sortedBy { r -> r.name }
                        updateViewState {
                            it.copy(
                                listRoute = list,
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
        }
    }

    private fun loadListDropMenuCouriers() {
        launchCoroutine {
            val user = sharedViewModel.viewState.value.user
            if (user != null) {
                val responseUsers = userApi.getUsers(user.idFactory)
                if (responseUsers.success) {
                    val users = responseUsers.obj
                    if (!users.isNullOrEmpty()) {
                        val list = users.sortedBy { r -> r.name }
                        updateViewState {
                            it.copy(
                                listCourier = list,
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
                   database.tripDao().deleteTrip(trip)
                   val list = viewState.value.trips.map { it.copy() }.toMutableList()
                   val item = list.first { it.id == trip.id }
                   val listNew = (list - item).sortedByDescending { it.date }
                   updateViewState { it.copy(trips = listNew) }
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
                currentRoute = viewState.value.listRoute[0],
                currentCourier = viewState.value.listCourier[0],
            )
        }
    }
}