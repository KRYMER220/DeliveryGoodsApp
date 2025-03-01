package ru.krymer.delivery.ui.screens.client.models

import kotlinx.coroutines.flow.MutableStateFlow
import ru.krymer.delivery.data.model.ClientModel
import ru.krymer.delivery.data.model.RouteModel

sealed class ClientAction {
    data object OpenAnalitic : ClientAction()
    data object None : ClientAction()
}


data class ClientShopViewState(
    val clientAction: ClientAction = ClientAction.None,
    val isError: Boolean = false,
    val errorValue: String = "",
    val isLoadClientData: Boolean = false,
    val listClient: MutableStateFlow<List<ClientModel>> = MutableStateFlow(listOf()),
    val listRoute: MutableStateFlow<List<RouteModel>> = MutableStateFlow(listOf()),

    val idRoute: String = "",
    val isDialogAdd: Boolean = false,

    val isDialogUpdate: Boolean = false,

    val isDialogDelete: Boolean = false,
    val idDeleteClient: Long? = null,
    val itemNameToDelete: String = "",

    val isErrorArrears: Boolean = false,
    val isErrorName: Boolean = false,
    val isErrorPhone: Boolean = false,
    val isErrorCords: Boolean = false,

    val clientUpdate: ClientModel? = null,
    val name: String = "",
    val arrears: String? = "",
    val phone: String? = "",
    val cords: String? = "",

    val dropDownState: Boolean = false,
    val selectedRoute: RouteModel? = null,
)