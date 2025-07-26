package ru.krymer.delivery.ui.screens.analitic.models

import ru.krymer.delivery.data.model.ClientModel
import ru.krymer.delivery.data.model.RouteModel
import ru.krymer.delivery.data.model.TripModel

sealed class AnaliticEvent {
    data object AllInfoClickedToOpen : AnaliticEvent()
    data object TripInfoClickedToOpen : AnaliticEvent()
    data object ClientInfoClickedToOpen : AnaliticEvent()
    data object AnaliticActionInvoked : AnaliticEvent()
    data object ShowLogView : AnaliticEvent()
    data object ShowDatePicker : AnaliticEvent()
    data object DismissDatePicker : AnaliticEvent()
    data class ChangeRangeDatePicker(val pair: Pair<Long, Long>) : AnaliticEvent()
    data class ChangeStateDropDownMenuClients(val isShow: Boolean) : AnaliticEvent()
    data class SetCurrentClient(val client: ClientModel) : AnaliticEvent()
    data class SetCurrentTrip(val trip: TripModel) : AnaliticEvent()
    data object DeleteLogs: AnaliticEvent()
}