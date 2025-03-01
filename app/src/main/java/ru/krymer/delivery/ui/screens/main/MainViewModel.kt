package ru.krymer.delivery.ui.screens.main

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import ru.krymer.delivery.common.EventHandler
import ru.krymer.delivery.ui.screens.main.models.MenuAction
import ru.krymer.delivery.ui.screens.main.models.MenuEvent
import ru.krymer.delivery.ui.screens.main.models.MenuViewState
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val sharedViewModel: SharedViewModel
) : ViewModel(), EventHandler<MenuEvent> {

    private val _viewState = mutableStateOf(MenuViewState())
    val viewState: State<MenuViewState> = _viewState

    override fun obtainEvent(event: MenuEvent) {
        when (event) {
            is MenuEvent.RouteClickedToOpen -> routeClicked()
            is MenuEvent.SignOutUser -> signOut()
            is MenuEvent.MenuActionInvoked -> loginActionInvoked()
            is MenuEvent.ProductClickedToOpen -> productClicked()
            is MenuEvent.CourierClickedToOpen -> courierClicked()
            is MenuEvent.TripClickedToOpen -> tripClicked()
            is MenuEvent.AnaliticClickedToOpen -> analiticClicked()
        }
    }

    private fun analiticClicked() {
        _viewState.value =
            viewState.value.copy(menuAction = MenuAction.OpenAnalitic)
    }

    private fun tripClicked() {
        _viewState.value =
            viewState.value.copy(menuAction = MenuAction.OpenTrips)
    }

    private fun courierClicked() {
        _viewState.value =
            viewState.value.copy(menuAction = MenuAction.OpenCouriers)
    }

    private fun productClicked() {
        _viewState.value =
            viewState.value.copy(menuAction = MenuAction.OpenProduct)
    }


    private fun loginActionInvoked() {
        _viewState.value = viewState.value.copy(menuAction = MenuAction.None)
    }

    private fun signOut() {
        sharedViewModel.logout()
    }

    private fun routeClicked() {
        _viewState.value =
            viewState.value.copy(menuAction = MenuAction.OpenRoute)
    }
}