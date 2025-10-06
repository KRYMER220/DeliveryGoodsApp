package ru.krymer.delivery.ui.request.models

import ru.krymer.delivery.data.model.MessageModel
import ru.krymer.delivery.data.model.RequestModel

sealed class RequestEvent {
    object ChangeTypePay : RequestEvent()
    object ChangeTypePrice : RequestEvent()
    object SwitchBonus : RequestEvent()

    object ToggleArrearsDialog : RequestEvent()
    object ToggleAddSumDialog : RequestEvent()

    object ToggleMessageDialog : RequestEvent()
    object ToggleDeleteShop : RequestEvent()

    data class SetArrearsInField(val arrears: Double) : RequestEvent()
    data object SetOrderInField : RequestEvent()
    data class SetOrderAndArrearsSumInField(val arrears: Double) : RequestEvent()
    data class SetArrearsAndAddInField(val sum: Double) : RequestEvent()
    data class SetOrderAndAddInField(val addSum: Double) : RequestEvent()
    data class SetAddInField(val addSum: Double) : RequestEvent()
    data class SetOrderAndArrearsAndAddSumInField(val arrears: Double, val addSum: Double) :
        RequestEvent()

    data class ChangeCash(val cash: String) : RequestEvent()
    data class ChangeNoCash(val noCash: String) : RequestEvent()
    data class ChangeArrears(val arrears: String) : RequestEvent()
    data class ChangeAddSum(val addSum: String) : RequestEvent()

    object SubmitSaveShop : RequestEvent()

    data class ChangeCountRequest(val count: String, val request: RequestModel) : RequestEvent()
    data class ChangeExchangeRequest(val exchange: String, val request: RequestModel) :
        RequestEvent()

    data class ChangeBonusRequest(val bonus: String, val request: RequestModel) : RequestEvent()

    object DeleteShop : RequestEvent()
    object UpdateShop : RequestEvent()
    object ToggleConfirmDeleteShopDialog : RequestEvent()

    object SendMessage : RequestEvent()
    data class ChangeMessage(val message: String) : RequestEvent()
    data class DeleteMessage(val message: MessageModel) : RequestEvent()
}