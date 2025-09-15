package ru.krymer.delivery.ui.screens.trip.models

import ru.krymer.delivery.data.model.RouteModel
import ru.krymer.delivery.data.model.TripModel
import ru.krymer.delivery.data.model.user.UserModel

sealed class TripEvent {
    data class ShowDeleteDialog(val trip: TripModel) : TripEvent()
    data object ShowHideAddDialog : TripEvent()
    data class ShowUpdateDialog(val trip: TripModel) : TripEvent()
    data object SaveTrip : TripEvent()
    data object UpdateTrip : TripEvent()
    data object OpenHideDropDownMenuWithRoutes : TripEvent()
    data object OpenHideDropDownMenuWithCouriers : TripEvent()
    data class SelectCourier(val courier: UserModel?) : TripEvent()
    data class SelectRoute(val route: RouteModel?) : TripEvent()
    data object OpenHideDatePickerForAddTrip : TripEvent()
    data class ChangeDate(val date: Long) : TripEvent()
    data object DismissDeleteDialog : TripEvent()
    data object DismissUpdateDialog : TripEvent()
    data object DeleteTrip : TripEvent()
    data object OpenFilterTrip: TripEvent()
    data class IsFilter(val boolean: Boolean) : TripEvent()
    data object SubmitFilter: TripEvent()
    data object LoadMoreTrips : TripEvent()
    data class ChangeSalaryTrip(val salary: String) : TripEvent()
    data class SyncTrip(val trip: TripModel): TripEvent()

    data class ChangeCourierFilter(val courier: UserModel?) : TripEvent()
    data class ChangeRouteFilter(val route: RouteModel?) : TripEvent()
    data class ChangeSort(val boolean: Boolean) : TripEvent()

}