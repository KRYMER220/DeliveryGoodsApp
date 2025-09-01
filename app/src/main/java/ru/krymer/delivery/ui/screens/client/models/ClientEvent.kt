package ru.krymer.delivery.ui.screens.client.models

import ru.krymer.delivery.data.model.ClientModel
import ru.krymer.delivery.data.model.RouteModel

sealed class ClientEvent {
    data class ToggleDeleteDialog(val client: ClientModel?) : ClientEvent()
    data object ClientAddAction : ClientEvent()
    data object ToggleAddDialog : ClientEvent()
    data class ToggleUpdateDialog(val client: ClientModel?) : ClientEvent()
    data object ClientUpdateAction : ClientEvent()
    data class ChangeNameClient(val name: String) : ClientEvent()
    data class ChangeArrearsClient(val arrears: String) : ClientEvent()
    data class ChangePhoneClient(val phone: String) : ClientEvent()
    data class ChangeCordClient(val cords: String) : ClientEvent()
    data class SelectedItemMenu(val route: RouteModel) : ClientEvent()
    data object DeleteClient : ClientEvent()
    data class ReorderClients(val list: List<ClientModel>) : ClientEvent()
}