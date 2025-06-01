package ru.krymer.delivery.ui.screens.route.models

import ru.krymer.delivery.data.model.RouteModel

sealed class RouteEvent {
    data class NameRouteChangedAdd(val name: String) : RouteEvent()
    data object RouteSaveAction : RouteEvent()
    data object RouteUpdateAction : RouteEvent()
    data class ShowDeleteDialog(val route: RouteModel) : RouteEvent()
    data object ShowAddDialog : RouteEvent()
    data class ShowUpdateDialog(val route: RouteModel) : RouteEvent()
    data class UpdateNameRoute(val name: String) : RouteEvent()
    data object DismissDeleteDialog : RouteEvent()
    data object DismissAddDialog : RouteEvent()
    data object DismissUpdateDialog : RouteEvent()
    data object DeleteRoute : RouteEvent()
}