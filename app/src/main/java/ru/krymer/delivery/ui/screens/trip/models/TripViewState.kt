package ru.krymer.delivery.ui.screens.trip.models

import kotlinx.coroutines.flow.MutableStateFlow
import ru.krymer.delivery.data.model.RouteModel
import ru.krymer.delivery.data.model.TripModel
import ru.krymer.delivery.data.model.user.UserModel
import ru.krymer.delivery.utills.getStartOfNextDay

sealed class TripAction {
    data object OpenShops : TripAction()
    data object None : TripAction()
}

data class TripViewState(
    val isError: Boolean = false,
    val errorValue: String = "",
    val trips: MutableStateFlow<List<TripModel>> = MutableStateFlow(listOf()),
    val unFilteredTrips: MutableStateFlow<List<TripModel>> = MutableStateFlow(listOf()),

    val deleteTrip: TripModel? = null,
    val showDeleteDialog: Boolean = false,

    val stateAddDialog: Boolean = false,
    val showUpdateSheetDialog: Boolean = false,

    val dropDownStateCourier: Boolean = false,
    val dropDownStateRoutes: Boolean = false,
    val dropDownStateDatePicker: Boolean = false,
    var currentRoute: RouteModel? = null,
    var currentCourier: UserModel? = null,
    var salary: String = "",
    val listRoute: MutableStateFlow<List<RouteModel>> = MutableStateFlow(listOf()),
    val listCourier: MutableStateFlow<List<UserModel>> = MutableStateFlow(listOf()),
    var currentDate: Long = getStartOfNextDay(),

    var currentTrip: TripModel? = null,

    val tripAction: TripAction = TripAction.None,
    val isShowFilterDialog: Boolean = false,
    val checkBoxIsFilterCourier: Boolean = false,
    var hasMore: Boolean = true,
    var isLoading: Boolean = false
)