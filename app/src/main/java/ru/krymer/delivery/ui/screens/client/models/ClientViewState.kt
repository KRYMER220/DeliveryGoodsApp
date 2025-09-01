package ru.krymer.delivery.ui.screens.client.models

import ru.krymer.delivery.data.model.ClientModel
import ru.krymer.delivery.data.model.RouteModel

data class ClientViewState(
    val isError: Boolean = false,
    val clients: List<ClientModel> = emptyList(),
    val route: RouteModel? = null,
    val listNewClient: List<ClientModel> = emptyList(),
    val listRoute: List<RouteModel> = emptyList(),
    val isLoading: Boolean = false,
    val toggleAddDialog: Boolean = false,
    val toggleUpdateDialog: Boolean = false,
    val toggleDeleteDialog: Boolean = false,
    val clientDelete: ClientModel? = null,
    val clientUpdate: ClientModel? = null,
    val name: String = "",
    val arrears: String = "",
    val phone: String = "",
    val cords: String = "",
    val selectedRoute: RouteModel? = null,
    val client: ClientModel? = null,
)