package ru.krymer.delivery.ui.request

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.krymer.delivery.AppDatabase
import ru.krymer.delivery.common.EventHandler
import ru.krymer.delivery.data.api.MessageApi
import ru.krymer.delivery.data.api.RequestApi
import ru.krymer.delivery.data.api.ShopApi
import ru.krymer.delivery.data.model.MessageModel
import ru.krymer.delivery.data.model.RequestModel
import ru.krymer.delivery.data.model.ShopModel
import ru.krymer.delivery.data.model.utilModel.StatusModel
import ru.krymer.delivery.data.model.utilModel.TypePayModel
import ru.krymer.delivery.data.model.utilModel.TypePayModel.ANOTHER
import ru.krymer.delivery.data.model.utilModel.TypePayModel.CASH
import ru.krymer.delivery.data.model.utilModel.TypePayModel.NO_CASH
import ru.krymer.delivery.data.model.utilModel.getStringByTypePay
import ru.krymer.delivery.data.model.utilModel.getTypePayByString
import ru.krymer.delivery.data.model.utilModel.toStr
import ru.krymer.delivery.data.request.CreateMessage
import ru.krymer.delivery.data.request.RequestShopRequest
import ru.krymer.delivery.data.request.UpdateShopRequest
import ru.krymer.delivery.ui.request.models.RequestEvent
import ru.krymer.delivery.ui.request.models.RequestViewState
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import ru.krymer.delivery.ui.screens.shop.RequestUpdateType
import ru.krymer.delivery.utills.Constants
import ru.krymer.delivery.utills.isSameDay
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

private enum class ShopUpdateType { ARREARS, ADD_SUM }

@HiltViewModel
class RequestViewModel @Inject constructor(
    private val sharedViewModel: SharedViewModel,
    private val requestApi: RequestApi,
    private val shopApi: ShopApi,
    private val room: AppDatabase,
    private val messageApi: MessageApi,
) : ViewModel(), EventHandler<RequestEvent> {

    private val _viewState = MutableStateFlow(RequestViewState())
    val viewState = _viewState.asStateFlow()

    private fun updateViewState(update: (RequestViewState) -> RequestViewState) {
        _viewState.update(update)
    }

    private fun launchCoroutine(block: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
                sharedViewModel.message(Constants.ERROR.CANCEL_OPERATION)
            } catch (e: TimeoutCancellationException) {
                throw e
                sharedViewModel.message(Constants.ERROR.TIMEOUT)
            } catch (e: Exception) {
                throw e
                sharedViewModel.message(e.message)
            }
        }
    }

    override fun obtainEvent(event: RequestEvent) {
        when (event) {
            is RequestEvent.ChangeBonusRequest -> updateRequestValue(
                item = event.request,
                value = event.bonus,
                type = RequestUpdateType.BONUS,
            )

            is RequestEvent.ChangeCountRequest -> updateRequestValue(
                item = event.request,
                value = event.count,
                type = RequestUpdateType.COUNT,
            )

            is RequestEvent.ChangeExchangeRequest -> updateRequestValue(
                item = event.request,
                value = event.exchange,
                type = RequestUpdateType.EXCHANGE,
            )

            is RequestEvent.ChangeCash -> updateViewState { it.copy(getCash = event.cash) }
            is RequestEvent.ChangeNoCash -> updateViewState { it.copy(getNoCash = event.noCash) }
            RequestEvent.ChangeTypePay -> changeTypePay()
            RequestEvent.ChangeTypePrice -> switchStatePrice()
            is RequestEvent.SetAddInField -> updatePayment(amount = event.addSum)
            is RequestEvent.SetArrearsAndAddInField -> updatePayment(amount = event.sum)
            is RequestEvent.SetArrearsInField -> updatePayment(amount = event.arrears)
            is RequestEvent.SetOrderAndAddInField -> updatePayment(amount = viewState.value.orderMoney + event.addSum)
            is RequestEvent.SetOrderAndArrearsAndAddSumInField -> updatePayment(viewState.value.orderMoney + event.addSum + event.arrears)
            is RequestEvent.SetOrderAndArrearsSumInField -> updatePayment(amount = viewState.value.orderMoney + event.arrears)
            RequestEvent.SetOrderInField -> updatePayment(amount = viewState.value.orderMoney)
            RequestEvent.SubmitSaveShop -> initRequest()
            RequestEvent.ToggleAddSumDialog -> updateViewState { it.copy(toggleAddSumDialog = !it.toggleAddSumDialog) }
            RequestEvent.ToggleArrearsDialog -> updateViewState { it.copy(toggleArrearsDialog = !it.toggleArrearsDialog) }
            RequestEvent.SwitchBonus -> switchBonus()
            RequestEvent.ToggleDeleteShop -> updateViewState { it.copy(toggleDeleteShop = !it.toggleDeleteShop) }
            RequestEvent.ToggleMessageDialog -> updateViewState { it.copy(toggleMessageDialog = !it.toggleMessageDialog) }
            RequestEvent.DeleteShop -> deleteShop()
            RequestEvent.ToggleConfirmDeleteShopDialog -> updateViewState {
                it.copy(
                    toggleConfirmSaveShopDialog = !it.toggleConfirmSaveShopDialog
                )
            }

            RequestEvent.UpdateShop -> launchCoroutine { updateShop() }
            is RequestEvent.ChangeAddSum -> updateShopValue(
                value = event.addSum,
                type = ShopUpdateType.ADD_SUM
            )

            is RequestEvent.ChangeArrears -> updateShopValue(
                value = event.arrears,
                type = ShopUpdateType.ARREARS
            )

            is RequestEvent.ChangeMessage -> updateViewState { it.copy(message = event.message) }
            is RequestEvent.DeleteMessage -> launchCoroutine { deleteMessage(message = event.message) }
            RequestEvent.SendMessage -> launchCoroutine { sendMessage() }
        }
    }

    fun initData(shop: ShopModel) {
        launchCoroutine {
            updateViewState {
                it.copy(
                    shop = shop,
                    typePay = shop.typePay.getTypePayByString(),
                    getCash = if (shop.cash > 0.0) shop.cash.toInt().toString() else "",
                    getNoCash = if (shop.noCash > 0.0) shop.noCash.toInt().toString() else "",
                    isOldPrice = shop.isOldPrice
                )
            }
            getLocalData(shop = shop)
            val messages = getMessages(shop.id)
            updateViewState { it.copy(messages = messages) }
        }
    }

    private suspend fun sendMessage() {
        viewState.value.shop?.let {
            val message = viewState.value.message
            val obj = CreateMessage(
                idClient = it.id, text = message, date = System.currentTimeMillis()
            )
            messageApi.add(message = obj)
            val messages = getMessages(it.id)
            updateViewState { it.copy(messages = messages) }
        }
    }

    private suspend fun deleteMessage(message: MessageModel) {
        messageApi.delete(message.id)
        val list = viewState.value.messages - message
        updateViewState { it.copy(messages = list) }
    }

    private fun updateShopValue(value: String, type: ShopUpdateType) {
        val shop = viewState.value.shop
        val user = sharedViewModel.viewState.value.user
        if (shop != null && user != null) {
            val num = value.toDoubleOrNull() ?: 0.0
            val newShop = when (type) {
                ShopUpdateType.ARREARS -> shop.copy(arrears = num, isChanged = !user.isSysOrAdmin())
                ShopUpdateType.ADD_SUM -> shop.copy(addSum = num, isChanged = !user.isSysOrAdmin())
            }
            updateViewState { it.copy(shop = newShop) }
        }
    }

    private fun switchBonus() {
        val shop = viewState.value.shop
        if (shop != null) {
            updateViewState { it.copy(shop = shop.copy(isBonus = !shop.isBonus)) }
        }
    }

    private fun updatePayment(amount: Double) {
        when (viewState.value.typePay) {
            CASH -> updateViewState { it.copy(getCash = amount.toInt().toString()) }
            NO_CASH -> updateViewState { it.copy(getNoCash = amount.toInt().toString()) }
            ANOTHER -> {}
        }
    }

    private fun initRequest() {
        launchCoroutine {
            val shop = viewState.value.shop
            if (shop != null) if (shop.status) {
                updateViewState { it.copy(toggleConfirmSaveShopDialog = true) }
            } else {
                updateShop()
            }
        }
    }


    private fun changeTypePay() {
        val currentType = viewState.value.typePay
        val allTypes = TypePayModel.entries
        val currentIndex = allTypes.indexOf(currentType)
        val nextIndex = (currentIndex + 1) % allTypes.size
        val nextType = allTypes[nextIndex]
        val currentShop = viewState.value.shop
        currentShop?.let {
            updateViewState {
                it.copy(
                    typePay = nextType,
                    getNoCash = if (nextType != CASH && currentShop.noCash > 0.0) currentShop.noCash.toInt()
                        .toString() else "",
                    getCash = if (nextType != NO_CASH && currentShop.cash > 0.0) currentShop.cash.toInt()
                        .toString() else "",
                )
            }
        }
    }

    private fun switchStatePrice() {
        updateViewState { it.copy(isOldPrice = !viewState.value.isOldPrice) }
        calculateOrder()
    }

    private fun updateRequestValue(item: RequestModel, value: String, type: RequestUpdateType) {
        modifyRequestInCurrentShop(item) { req ->
            val v = value.trim().toIntOrNull() ?: 0
            val user = sharedViewModel.viewState.value.user
            val status =
                if (user != null) (!user.isSysOrAdmin() || req.status == true) else req.status
            when (type) {
                RequestUpdateType.COUNT -> req.copy(
                    count = v,
                    status = status,
                    statusServer = StatusModel.UN_SYNC.toStr()
                )

                RequestUpdateType.BONUS -> req.copy(
                    bonus = v,
                    status = status,
                    statusServer = StatusModel.UN_SYNC.toStr()
                )

                RequestUpdateType.EXCHANGE -> req.copy(
                    exchange = v,
                    statusServer = StatusModel.UN_SYNC.toStr()
                )
            }
        }
    }

    private suspend fun updateShop() {
        val user = sharedViewModel.viewState.value.user
        user?.let {
            val shop = viewState.value.shop
            if (shop != null) {
                if (isSameDay(shop.date, System.currentTimeMillis()) || user.isModOrAdminOrSys()) {
                    val typePay = viewState.value.typePay
                    val getCash = viewState.value.getCash
                    val getNoCash = viewState.value.getNoCash
                    val cash =
                        if (getCash.isEmpty() && typePay == NO_CASH) 0.0 else getCash.toDouble()
                    val noCash =
                        if (getNoCash.isEmpty() && typePay == CASH) 0.0 else getNoCash.toDouble()
                    val isOldPrice = viewState.value.isOldPrice
                    val updateShop = shop.copy(
                        statusServer = StatusModel.UN_SYNC.toStr(),
                        status = false,
                        typePay = typePay.getStringByTypePay(),
                        cash = cash,
                        noCash = noCash,
                        isOldPrice = isOldPrice
                    )
                    updateViewState { it.copy(shop = updateShop) }

                    room.shopDao().insertShop(
                        updateShop
                    )

                    updateShop.apply {
                        val shopRequest = UpdateShopRequest(
                            id = id,
                            idTrip = idTrip,
                            idFactory = idFactory,
                            arrears = arrears,
                            addSum = addSum,
                            status = true,
                            date = date,
                            typePay = typePay.getStringByTypePay(),
                            cash = cash,
                            counter = counter,
                            noCash = noCash,
                            isOldPrice = isOldPrice,
                            cord = cord,
                            nameShop = nameShop,
                            isChanged = isChanged
                        )
                        val response = shopApi.update(shopRequest)
                        if (!response.success) {
                            sharedViewModel.message(response.message)
                        } else {
                            updateShop.copy(statusServer = StatusModel.SYNC.toStr(), status = true)
                                .let { s ->
                                    room.shopDao().insertShop(s)
                                    updateViewState { it.copy(shop = s) }
                                }
                        }
                    }
                }
            } else {
                sharedViewModel.message(Constants.ERROR.RESRTRAINT)
            }
        }
    }


    private fun modifyRequestInCurrentShop(
        item: RequestModel,
        transform: (RequestModel) -> RequestModel
    ) {
        launchCoroutine {
            val requests = viewState.value.requests.map { it.copy() }.toMutableList()
            val reqIndex = requests.indexOfFirst { it.id == item.id }
            if (reqIndex == -1) return@launchCoroutine

            val newRequest = transform(item)
            requests[reqIndex] = newRequest

            updateViewState {
                it.copy(
                    requests = requests,
                )
            }

            calculateOrder()

            updateRequest(newRequest)
        }
    }

    private fun updateRequest(request: RequestModel) {
        launchCoroutine {
            val shop = viewState.value.shop ?: return@launchCoroutine
            val user = sharedViewModel.viewState.value.user ?: return@launchCoroutine

            if (isSameDay(shop.date, System.currentTimeMillis()) || user.isModOrAdminOrSys()) {

                room.requestDao()
                    .upsertRequest(request = request.copy(statusServer = StatusModel.UN_SYNC.toStr()))

                launchCoroutine {
                    val reqResponse = RequestShopRequest(
                        id = request.id,
                        idShop = request.idShop,
                        idTrip = request.idTrip,
                        idFactory = request.idFactory,
                        count = request.count,
                        bonus = request.bonus,
                        status = request.status,
                        exchange = request.exchange,
                        price = request.price,
                        oldPrice = request.oldPrice,
                        name = request.name,
                        counter = request.counter
                    )

                    val response = requestApi.update(reqResponse)

                    if (!response.success) {
                        sharedViewModel.message(response.message)
                    } else {
                        room.requestDao()
                            .upsertRequest(request = request.copy(statusServer = StatusModel.SYNC.toStr()))
                        val updatedList = viewState.value.requests.map { it.copy() }.toMutableList()
                        val index = updatedList.indexOfFirst { it.id == request.id }
                        updatedList[index] = request.copy(statusServer = StatusModel.SYNC.toStr())
                        updateViewState { it.copy(requests = updatedList) }
                        calculateOrder()
                    }
                }
            } else {
                sharedViewModel.message(Constants.ERROR.RESRTRAINT)
            }
        }
    }

    private suspend fun getMessages(id: Long): List<MessageModel> {
        val messages = messageApi.getMessages(idClient = id).obj
        return messages ?: emptyList()
    }

    private suspend fun getLocalData(shop: ShopModel) {
        val requests = room.requestDao().getRequests(idTrip = shop.idTrip, idShop = shop.id)
            .sortedBy { it.counter }
        updateViewState { it.copy(requests = requests) }
        calculateOrder()
    }

    private fun calculateOrder() {
        val list = viewState.value.requests
        val isOldPrice = viewState.value.isOldPrice

        var money = 0.0
        list.forEach {
            money += it.count * it.price - it.exchange * (if (isOldPrice) it.oldPrice else it.price)
        }
        updateViewState { it.copy(orderMoney = money) }
    }

    private fun deleteShop() {
        launchCoroutine {
            val shop = viewState.value.shop
            if (shop != null) {
                val response = shopApi.delete(id = shop.id, idTrip = shop.idTrip)
                if (response.success) {
                    room.requestDao().deleteRequestsByShopId(shopId = shop.id, tripId = shop.idTrip)
                    room.shopDao().deleteShop(shop)
                } else {
                    sharedViewModel.message(response.message)
                }
            } else {
                sharedViewModel.message(Constants.ERROR.GENERAL_ERROR)
            }
        }
    }
}