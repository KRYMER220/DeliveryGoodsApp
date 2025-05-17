package ru.krymer.delivery.ui.screens.main.models

sealed class MenuAction {
    data object OpenRoute : MenuAction()
    data object OpenProduct : MenuAction()
    data object OpenCouriers : MenuAction()
    data object OpenTrips : MenuAction()
    data object OpenAnalitic : MenuAction()
    data object None : MenuAction()
}

data class MenuViewState(
    val isProfileSignOut: Boolean = false,
    val menuAction: MenuAction = MenuAction.None,
    val versionValue: String = "",
    val usernameValue: String = "",
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorValue: String = "",
    val isLoadData: Boolean = true,
)