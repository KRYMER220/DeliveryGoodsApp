package ru.krymer.delivery.ui.request.models

import ru.krymer.delivery.data.model.MessageModel
import ru.krymer.delivery.data.model.RequestModel
import ru.krymer.delivery.data.model.ShopModel
import ru.krymer.delivery.data.model.utilModel.TypePayModel

data class RequestViewState(
    val requests: List<RequestModel> = emptyList(),
    val shop: ShopModel? = null,
    val orderMoney: Double = 0.0,
    val getCash: String = "",
    val getNoCash: String = "",
    val typePay: TypePayModel = TypePayModel.CASH,
    val isOldPrice: Boolean = false,
    val messages: List<MessageModel> = emptyList(),
    val message: String = "",

    val toggleDeleteShop: Boolean = false,
    val toggleConfirmSaveShopDialog: Boolean = false,
    val toggleMessageDialog: Boolean = false,
    val toggleAddSumDialog: Boolean = false,
    val toggleArrearsDialog: Boolean = false,
    val toggleSwitchBonus: Boolean = false,
)