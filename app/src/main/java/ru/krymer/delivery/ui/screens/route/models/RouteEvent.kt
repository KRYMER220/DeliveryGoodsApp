package ru.krymer.delivery.ui.screens.route.models

import ru.krymer.delivery.data.model.RouteModel

sealed class RouteEvent {
    data class NameRouteChangedAdd(val name: String) : RouteEvent()
    data object CreateRoute : RouteEvent()
    data object UpdateRoute : RouteEvent()
    data class ToggleDeleteDialog(val route: RouteModel?) : RouteEvent()
    data object ToggleAddDialog : RouteEvent()
    data class ToggleUpdateDialog(val route: RouteModel?) : RouteEvent()
    data class UpdateNameRoute(val name: String) : RouteEvent()
    data object DeleteRoute : RouteEvent()
}