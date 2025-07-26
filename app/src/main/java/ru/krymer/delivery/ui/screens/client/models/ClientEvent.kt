package ru.krymer.delivery.ui.screens.client.models

import ru.krymer.delivery.data.model.ClientModel
import ru.krymer.delivery.data.model.RouteModel

sealed class ClientEvent {
    data class ShowDeleteDialog(val client: ClientModel) : ClientEvent()
    data object ClientAddAction : ClientEvent()
    data object ShowAddDialog : ClientEvent()
    data class ShowUpdateDialog(val client: ClientModel) : ClientEvent()
    data object ClientUpdateAction : ClientEvent()
    data class ChangeNameClient(val name: String) : ClientEvent()
    data class ChangeArrearsClient(val arrears: String) : ClientEvent()
    data class ChangePhoneClient(val phone: String) : ClientEvent()
    data class ChangeCordClient(val cords: String) : ClientEvent()
    data class DropDownMenuState(val state: Boolean) : ClientEvent()
    data class SelectedItemMenu(val route: RouteModel? = null) : ClientEvent()
    data object DismissDeleteDialog : ClientEvent()
    data object DismissAddDialog : ClientEvent()
    data object DismissUpdateDialog : ClientEvent()
    data object DeleteClient : ClientEvent()
    data class ReorderClients(val list: List<ClientModel>) : ClientEvent()
}