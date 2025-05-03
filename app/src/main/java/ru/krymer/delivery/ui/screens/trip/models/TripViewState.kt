package ru.krymer.delivery.ui.screens.trip.models

import kotlinx.coroutines.flow.MutableStateFlow
import ru.krymer.delivery.data.model.RouteModel
import ru.krymer.delivery.data.model.TripModel
import ru.krymer.delivery.data.model.user.UserModel

sealed class TripAction {
    data object OpenShops : TripAction()
    data object None : TripAction()
}

data class TripViewState(
    val isError: Boolean = false,
    val errorValue: String = "",
    val listTrip: MutableStateFlow<List<TripModel>> = MutableStateFlow(listOf()),


    val deleteTrip: TripModel? = null,
    val showDeleteDialog: Boolean = false,

    val showAddSheetDialog: Boolean = false,
    val showUpdateSheetDialog: Boolean = false,

    val dropDownStateCourier: Boolean = false,
    val dropDownStateTrips: Boolean = false,
    val dropDownStateDatePicker: Boolean = false,
    var currentRoute: RouteModel? = null,
    var currentCourier: UserModel? = null,
    val listRoute: MutableStateFlow<List<RouteModel>> = MutableStateFlow(listOf()),
    val listCourier: MutableStateFlow<List<UserModel>> = MutableStateFlow(listOf()),
    var currentDate: Long = 0,

    var currentTrip: TripModel? = null,

    val tripAction: TripAction = TripAction.None,
)