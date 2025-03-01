package ru.krymer.delivery.ui.screens.main.models

sealed class MenuEvent {
    data object RouteClickedToOpen : MenuEvent()
    data object ProductClickedToOpen : MenuEvent()
    data object CourierClickedToOpen : MenuEvent()
    data object TripClickedToOpen : MenuEvent()
    data object AnaliticClickedToOpen : MenuEvent()
    data object SignOutUser : MenuEvent()
    data object MenuActionInvoked : MenuEvent()
}