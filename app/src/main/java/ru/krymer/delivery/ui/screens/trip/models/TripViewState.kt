package ru.krymer.delivery.ui.screens.trip.models

import ru.krymer.delivery.data.model.RouteModel
import ru.krymer.delivery.data.model.TripModel
import ru.krymer.delivery.data.model.user.UserModel
import ru.krymer.delivery.utills.getStartOfNextDay

data class TripViewState(
    val trips: List<TripModel> = emptyList(),
    val trip: TripModel? = null,
    val isInitialLoad: Boolean = true,

    val toggleDeleteTrip: Boolean = false,
    val toggleAddTrip: Boolean = false,
    val toggleUpdateTrip: Boolean = false,

    val currentRoute: RouteModel? = null,
    val currentCourier: UserModel? = null,
    val salary: String = "",
    val listRoute: List<RouteModel> = emptyList(),
    val listCourier: List<UserModel> = emptyList(),
    val currentDate: Long = getStartOfNextDay(),

    val isShowFilterDialog: Boolean = false,
    val isFilter: Boolean = false,
    val hasMore: Boolean = true,
    val isLoading: Boolean = false,
    val lightVersion: Boolean = false,

    val filterUid: Long? = null,
    val filterRouteId: Long? = null,
    val sort: Boolean = false
)