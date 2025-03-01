package ru.krymer.delivery.ui.screens.route.models

import ru.krymer.delivery.data.model.RouteModel

sealed class RouteEvent {
    data object RouteActionInvoked : RouteEvent()
    data class NameRouteChangedAdd(val name: String) : RouteEvent()
    data object RouteSaveAction : RouteEvent()
    data object RouteUpdateAction : RouteEvent()
    data class RouteItemClickedToShop(val route: RouteModel) : RouteEvent()
    data class ShowDeleteDialog(val itemName: String, val itemID: Long) : RouteEvent()
    data object ShowAddDialog : RouteEvent()
    data class RouteItemLongClicked(val route: RouteModel) : RouteEvent()
    data class NameRouteChangedUpdate(val name: String) : RouteEvent()
    data object DismissDeleteDialog : RouteEvent()
    data object DismissAddDialog : RouteEvent()
    data object DismissUpdateDialog : RouteEvent()
    data object DeleteRoute : RouteEvent()
}