package ru.krymer.delivery.ui.screens.trip.models

import ru.krymer.delivery.data.model.RouteModel
import ru.krymer.delivery.data.model.TripModel
import ru.krymer.delivery.data.model.user.UserModel

sealed class TripEvent {
    data object TripActionInvoked : TripEvent()
    data class ShowDeleteDialog(val itemName: String, val itemID: Long) : TripEvent()
    data object ShowAddDialog : TripEvent()
    data class ShowChangeCourierDialog(val trip: TripModel) : TripEvent()
    data object TripSaveAction : TripEvent()
    data object TripUpdateAction : TripEvent()
    data class ChangeDropDownStateTrip(val state: Boolean) : TripEvent()
    data class ChangeDropDownStateCourier(val state: Boolean) : TripEvent()
    data class SelectDropDownCourier(val courier: UserModel) : TripEvent()
    data class SelectDropDownRoute(val route: RouteModel) : TripEvent()
    data class ChangeDropDownStateDatePicker(val state: Boolean) : TripEvent()
    data class ChangeDate(val date: Long) : TripEvent()
    data class TripItemClicked(val trip: TripModel) : TripEvent()
    data object DismissDeleteDialog : TripEvent()
    data object DismissAddDialog : TripEvent()
    data object DismissUpdateDialog : TripEvent()
    data object DeleteTrip : TripEvent()
}