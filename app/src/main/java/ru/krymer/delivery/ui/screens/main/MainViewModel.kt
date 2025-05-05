package ru.krymer.delivery.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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

    private fun updateViewState(update: (MenuViewState) -> MenuViewState) {
        _viewState.update { update(it) }
    }
    
    private val _viewState = MutableStateFlow(MenuViewState())
    val viewState: StateFlow<MenuViewState> = _viewState
    
    override fun obtainEvent(event: MenuEvent) {
        when (event) {
            is MenuEvent.RouteClickedToOpen -> selectAction(menuAction = MenuAction.OpenRoute)
            is MenuEvent.SignOutUser -> signOut()
            is MenuEvent.MenuActionInvoked -> selectAction(menuAction = MenuAction.None)
            is MenuEvent.ProductClickedToOpen ->selectAction(menuAction = MenuAction.OpenProduct)
            is MenuEvent.CourierClickedToOpen -> selectAction(menuAction = MenuAction.OpenCouriers)
            is MenuEvent.TripClickedToOpen -> selectAction(menuAction = MenuAction.OpenTrips)
            is MenuEvent.AnaliticClickedToOpen -> selectAction(menuAction = MenuAction.OpenAnalitic)
            is MenuEvent.Chat -> selectAction(menuAction = MenuAction.Chat)
        }
    }
    
    private fun selectAction(menuAction: MenuAction) {
        when(menuAction) {
            MenuAction.None -> changeState(acton = menuAction)
            MenuAction.OpenAnalitic -> changeState(acton = menuAction)
            MenuAction.OpenCouriers -> changeState(acton = menuAction)
            MenuAction.OpenProduct -> changeState(acton = menuAction)
            MenuAction.OpenRoute -> changeState(acton = menuAction)
            MenuAction.OpenTrips -> changeState(acton = menuAction)
            MenuAction.Chat -> changeState(acton = menuAction)
        }
    }
    
    private fun changeState(acton: MenuAction) {
        updateViewState { it.copy(menuAction = acton) }
    }


    private fun signOut() {
        viewModelScope.launch(Dispatchers.IO) {
            sharedViewModel.logout()
        }
    }
}