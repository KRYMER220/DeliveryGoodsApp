package ru.krymer.delivery.ui.screens.trip.models

import ru.krymer.delivery.data.model.RouteModel
import ru.krymer.delivery.data.model.TripModel
import ru.krymer.delivery.data.model.user.UserModel

sealed class TripEvent {
    data object SaveTrip : TripEvent()
    data object UpdateTrip : TripEvent()
    data object DeleteTrip : TripEvent()

    data class SelectCourier(val courier: UserModel?) : TripEvent()
    data class SelectRoute(val route: RouteModel?) : TripEvent()
    data class ChangeDate(val date: Long) : TripEvent()

    data object ToggleAddDialog : TripEvent()
    data class ToggleDeleteDialog(val trip: TripModel?) : TripEvent()
    data class ToggleUpdateDialog(val trip: TripModel?) : TripEvent()

    data object ToggleFilterDialog: TripEvent()

    data class IsFilter(val isFilter: Boolean) : TripEvent()
    data object SubmitFilter: TripEvent()
    data object LoadMoreTrips : TripEvent()
    data class ChangeSalaryTrip(val salary: String) : TripEvent()
    data class ChangeCourierFilter(val courier: UserModel?) : TripEvent()
    data class ChangeRouteFilter(val route: RouteModel?) : TripEvent()
    data class ChangeSort(val boolean: Boolean) : TripEvent()

    data object LocalData : TripEvent()

    data object RefreshTrips : TripEvent()

    data object LoadListDropMenuRoutes : TripEvent()
    data object LoadListDropMenuCouriers : TripEvent()
    data object LoadSettings : TripEvent()

    data class TripsLoaded(val trips: List<TripModel>, val hasMore: Boolean) : TripEvent()
    data class ListCouriersLoaded(val couriers: List<UserModel>) : TripEvent()
    data class ListRoutesLoaded(val routes: List<RouteModel>) : TripEvent()

    data class Error(val message: String?) : TripEvent()
}