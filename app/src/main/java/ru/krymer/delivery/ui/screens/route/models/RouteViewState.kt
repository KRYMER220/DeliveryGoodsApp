package ru.krymer.delivery.ui.screens.route.models

import kotlinx.coroutines.flow.MutableStateFlow
import ru.krymer.delivery.data.model.RouteModel

sealed class RouteAction {
    data object OpenClients : RouteAction()
    data object None : RouteAction()
}


data class RouteViewState(
    val routeAction: RouteAction = RouteAction.None,
    val isLoadRouteData: Boolean = false,
    val listRoute: MutableStateFlow<List<RouteModel>> = MutableStateFlow(listOf()),

    val showDialogAdd: Boolean = false,
    val isDialogUpdate: Boolean = false,
    val itemNameAdd: String = "",
    val isErrorName: Boolean = false,
    val errorName: String? = null,

    val itemNameUpdate: String = "",
    val routeUpdated: RouteModel? = null,

    val itemIdDelete: Long? = null,
    val itemNameDelete: String? = null,
    val isDialogDelete: Boolean = false,
)