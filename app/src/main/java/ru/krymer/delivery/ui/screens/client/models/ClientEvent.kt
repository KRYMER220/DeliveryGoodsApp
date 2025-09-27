package ru.krymer.delivery.ui.screens.client.models

import ru.krymer.delivery.data.model.ClientModel
import ru.krymer.delivery.data.model.RouteModel

sealed class ClientEvent {
    data class ClientsLoaded(val clients: List<ClientModel>) : ClientEvent()
    data class Initialize(val route: RouteModel, val routes: List<RouteModel>) : ClientEvent()

    object RefreshClients : ClientEvent()
    object CreateClient : ClientEvent()
    object UpdateClient : ClientEvent()
    object DeleteClient : ClientEvent()

    data class ToggleDeleteDialog(val client: ClientModel?) : ClientEvent()
    data object ToggleAddDialog : ClientEvent()
    data class ToggleUpdateDialog(val client: ClientModel?) : ClientEvent()

    data class ChangeNameClient(val name: String) : ClientEvent()
    data class ChangeArrearsClient(val arrears: String) : ClientEvent()
    data class ChangePhoneClient(val phone: String) : ClientEvent()
    data class ChangeCordClient(val cords: String) : ClientEvent()

    data class SelectedItemMenu(val route: RouteModel) : ClientEvent()

    data class ReorderClients(val list: List<ClientModel>) : ClientEvent()
}