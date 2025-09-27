package ru.krymer.delivery.ui.screens.route.models

import ru.krymer.delivery.data.model.RouteModel

sealed class RouteEvent {
    object RefreshRoutes : RouteEvent()
    data class RoutesLoaded(val routes: List<RouteModel>) : RouteEvent()
    data class Error(val message: String?) : RouteEvent()

    object CreateRoute : RouteEvent()
    object UpdateRoute : RouteEvent()
    object DeleteRoute : RouteEvent()

    data class ChangeAddName(val name: String) : RouteEvent()
    data class ChangeUpdateName(val name: String) : RouteEvent()

    data class ToggleAddDialog(val dummy: Unit = Unit) : RouteEvent()
    data class ToggleUpdateDialog(val route: RouteModel?) : RouteEvent()
    data class ToggleDeleteDialog(val route: RouteModel?) : RouteEvent()
}