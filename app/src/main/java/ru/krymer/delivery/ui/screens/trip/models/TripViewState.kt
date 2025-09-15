package ru.krymer.delivery.ui.screens.trip.models

import kotlinx.coroutines.flow.MutableStateFlow
import ru.krymer.delivery.data.model.RouteModel
import ru.krymer.delivery.data.model.TripModel
import ru.krymer.delivery.data.model.user.UserModel
import ru.krymer.delivery.utills.getStartOfNextDay

data class TripViewState(
    val isError: Boolean = false,
    val errorValue: String = "",
    val trips: List<TripModel> = emptyList(),

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
    val listRoute: List<RouteModel> = emptyList(),
    val listCourier: List<UserModel> = emptyList(),
    var currentDate: Long = getStartOfNextDay(),

    var currentTrip: TripModel? = null,

    val isShowFilterDialog: Boolean = false,
    val isFilter: Boolean = false,
    var hasMore: Boolean = true,
    var isLoading: Boolean = false,
    val lightVersion: Boolean = false,

    val filterUid: Long? = null,
    val filterRouteId: Long? = null,
    val sort: Boolean = false
)