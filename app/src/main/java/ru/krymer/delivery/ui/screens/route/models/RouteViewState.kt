package ru.krymer.delivery.ui.screens.route.models

import ru.krymer.delivery.data.model.RouteModel

data class RouteViewState(
    val routes: List<RouteModel> = emptyList(),
    val toggleDialogAdd: Boolean = false,
    val toggleDialogUpdate: Boolean = false,
    val nameRouteAdd: String = "",
    val isLoading: Boolean = false,
    val nameRouteUpdate: String = "",
    val route: RouteModel? = null,
    val toggleDialogDelete: Boolean = false,
)