package ru.krymer.delivery.ui.screens.route.models

import kotlinx.coroutines.flow.MutableStateFlow
import ru.krymer.delivery.data.model.RouteModel

data class RouteViewState(
    val listRoute: MutableStateFlow<List<RouteModel>> = MutableStateFlow(listOf()),

    val showDialogAdd: Boolean = false,
    val isDialogUpdate: Boolean = false,
    val nameRouteAdd: String = "",
    val isLoadingData: Boolean = false,
    val routeUpdated: RouteModel? = null,
    val routeDeleted: RouteModel? = null,
    val isDialogDelete: Boolean = false,
)