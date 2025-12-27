package ru.krymer.delivery.ui.screens.request

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.krymer.delivery.AppDatabase
import ru.krymer.delivery.common.EventHandler
import ru.krymer.delivery.data.api.ClientApi
import ru.krymer.delivery.data.api.MessageApi
import ru.krymer.delivery.data.api.RequestApi
import ru.krymer.delivery.data.api.ShopApi
import ru.krymer.delivery.data.model.MessageModel
import ru.krymer.delivery.data.model.RequestModel
import ru.krymer.delivery.data.model.ShopModel
import ru.krymer.delivery.data.model.ShopServerModel
import ru.krymer.delivery.data.model.toRequest
import ru.krymer.delivery.data.model.utilModel.StatusModel
import ru.krymer.delivery.data.model.utilModel.TypePayModel
import ru.krymer.delivery.data.model.utilModel.TypePayModel.ANOTHER
import ru.krymer.delivery.data.model.utilModel.TypePayModel.CASH
import ru.krymer.delivery.data.model.utilModel.TypePayModel.NO_CASH
import ru.krymer.delivery.data.model.utilModel.getStringByTypePay
import ru.krymer.delivery.data.model.utilModel.getTypePayByString
import ru.krymer.delivery.data.model.utilModel.toStr
import ru.krymer.delivery.data.request.ClientRequest
import ru.krymer.delivery.data.request.CreateMessage
import ru.krymer.delivery.data.request.RequestShopRequest
import ru.krymer.delivery.data.request.ShopRequest
import ru.krymer.delivery.data.response.BaseResponse
import ru.krymer.delivery.ui.screens.request.models.RequestEvent
import ru.krymer.delivery.ui.screens.request.models.RequestViewState
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import ru.krymer.delivery.ui.screens.shop.RequestUpdateType
import ru.krymer.delivery.utills.Constants
import ru.krymer.delivery.utills.isSameDay
import ru.krymer.delivery.utills.toSafeDouble
import java.io.IOException
import java.net.UnknownHostException
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

private enum class ShopUpdateType { ARREARS, ADD_SUM }

@HiltViewModel
class RequestViewModel @Inject constructor(
    private val sharedViewModel: SharedViewModel,
    private val requestApi: RequestApi,
    private val shopApi: ShopApi,
    private val room: AppDatabase,
    private val clientApi: ClientApi,
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
            }
            catch (_: UnknownHostException) {
                sharedViewModel.message("Отсутствует интернет соединение")
            } catch (e: IOException) {
                sharedViewModel.message("Ошибка сети: ${e.message}")
            } catch (e: CancellationException) {
                sharedViewModel.message(Constants.ERROR.CANCEL_OPERATION)
                throw e
            } catch (e: TimeoutCancellationException) {
                sharedViewModel.message(Constants.ERROR.TIMEOUT)
                throw e
            } catch (e: Exception) {
                sharedViewModel.message(e.message)
                throw e
            }
        }
    }

    override fun obtainEvent(event: RequestEvent) {
        when (event) {
            is RequestEvent.ChangeBonusRequest -> {
                launchCoroutine {
                    updateRequestValue(
                        requestToModify = event.request,
                        value = event.bonus,
                        type = RequestUpdateType.BONUS,
                    )
                }
            }

            is RequestEvent.ChangeCountRequest -> {
                launchCoroutine {
                    updateRequestValue(
                        requestToModify = event.request,
                        value = event.count,
                        type = RequestUpdateType.COUNT,
                    )
                }
            }

            is RequestEvent.ChangeExchangeRequest -> {
                launchCoroutine {
                    updateRequestValue(
                        requestToModify = event.request,
                        value = event.exchange,
                        type = RequestUpdateType.EXCHANGE,
                    )
                }
            }

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
            RequestEvent.ToggleListShops -> {
                launchCoroutine {
                    getLocalShops()
                    updateViewState { it.copy(toggleSwitchListReq = !it.toggleSwitchListReq) }
                }
            }

            RequestEvent.PreSaveShop -> {
                launchCoroutine {
                    val shop = viewState.value.shop
                    shop?.let {
                        room.shopDao().insertShop(shop = shop)
                        updaterShop(shop = shop, status = false)
                    } ?: run {
                        sharedViewModel.message(message = "Ошибка сохранения магазина!")
                    }
                }
            }

            RequestEvent.ToggleStatusShop -> {
                launchCoroutine {
                    val user = sharedViewModel.viewState.value.user ?: return@launchCoroutine
                    if (!user.isModOrAdminOrSys()) return@launchCoroutine
                    val shop = viewState.value.shop?.copy(status = false, statusServer = StatusModel.NOT_CHANGE.toStr()) ?: return@launchCoroutine
                    val response = updaterShop(shop = shop, status = false)
                    if (response.success) {
                        room.shopDao().insertShop(shop = shop)
                        updateViewState { it.copy(shop = shop) }
                    } else {
                        sharedViewModel.message(response.message)
                    }
                }
            }
        }
    }

    private suspend fun getLocalShops() {
        val shops = viewState.value.shops
        if (shops.isEmpty()) return else {
            val shopsUi = mutableListOf<ShopServerModel>()
            shops.forEach { localShop ->
                val localReq = room.requestDao().getRequests(idShop = localShop.id, idTrip = localShop.idTrip)
                localShop.apply {
                    val shopUi = ShopServerModel(
                        id = id,
                        idTrip = idTrip,
                        idFactory = idFactory,
                        nameShop = nameShop,
                        arrears = arrears,
                        addSum = addSum,
                        status = status,
                        date = date,
                        typePay = typePay.getTypePayByString(),
                        cash = cash,
                        counter = counter,
                        noCash = noCash,
                        isOldPrice = isOldPrice,
                        cord = cord,
                        listRequest = localReq,
                        isBonus = isBonus,
                        isChanged = isChanged
                    )
                    shopsUi.add(shopUi)
                }
            }
            updateViewState { it.copy(shopsUI = shopsUi) }
        }
    }

    fun initData(shop: ShopModel, shops: List<ShopModel>) {
        launchCoroutine {
            updateViewState {
                it.copy(
                    shop = shop,
                    typePay = shop.typePay.getTypePayByString(),
                    getCash = shop.cash.toInt().toString(),
                    getNoCash = shop.noCash.toInt().toString(),
                    isOldPrice = shop.isOldPrice,
                    shops = shops
                )
            }
            getLocalData(shop = shop)
            val messages = getMessages(shop.id)
            updateViewState { it.copy(messages = messages) }
        }
    }

    private suspend fun sendMessage() {
        viewState.value.shop?.let { shop ->
            val message = viewState.value.message
            val obj = CreateMessage(
                idClient = shop.id, text = message, date = System.currentTimeMillis()
            )
            messageApi.add(message = obj)
            val messages = getMessages(shop.id)
            updateViewState { it.copy(messages = messages) }
        }
    }

    private suspend fun deleteMessage(message: MessageModel) {
        messageApi.delete(message.id)
        val list = viewState.value.messages - message
        updateViewState { it.copy(messages = list) }
    }

    private fun updateShopValue(value: String, type: ShopUpdateType) {
        launchCoroutine {
            val shop = viewState.value.shop
            val user = sharedViewModel.viewState.value.user
            if (shop != null && user != null) {
                val num = value.toSafeDouble()
                val newShop = when (type) {
                    ShopUpdateType.ARREARS -> {
                        shop.copy(arrears = num, isChanged = !user.isSysOrAdmin())
                    }
                    ShopUpdateType.ADD_SUM -> {
                        shop.copy(addSum = num, isChanged = !user.isSysOrAdmin())
                    }
                }
                updateViewState { it.copy(shop = newShop) }
            }
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
            CASH -> {
                updateViewState { it.copy(getCash = amount.toInt().toString()) }
            }
            NO_CASH -> {
                updateViewState { it.copy(getNoCash = amount.toInt().toString()) }
            }
            ANOTHER -> {}
        }
    }

    private fun initRequest() {
        launchCoroutine {
            val shop = viewState.value.shop
            if (shop != null) if (shop.status || shop.statusServer == StatusModel.UN_SYNC.toStr() || shop.statusServer == StatusModel.SYNC_FAILED.toStr()) {
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
                    getNoCash = if (nextType != CASH) currentShop.noCash.toInt()
                        .toString() else "",
                    getCash = if (nextType != NO_CASH) currentShop.cash.toInt()
                        .toString() else "",
                )
            }
        }
    }

    private fun switchStatePrice() {
        updateViewState { it.copy(isOldPrice = !viewState.value.isOldPrice) }
        val requests = viewState.value.requests
        calculateOrder(requests = requests)
    }

    private suspend fun updateRequestValue(requestToModify: RequestModel, value: String, type: RequestUpdateType) {
        modifyRequestInCurrentShop(requestToModify) { request ->
            val v = value.trim().toIntOrNull() ?: 0
            val user = sharedViewModel.viewState.value.user
            val status = when {
                user == null -> request.status
                user.isSysOrAdmin() -> request.status
                else -> false
            }
            when (type) {
                RequestUpdateType.COUNT -> request.copy(
                    count = v,
                    status = status,
                    statusServer = StatusModel.UN_SYNC.toStr()
                )

                RequestUpdateType.BONUS -> request.copy(
                    bonus = v,
                    status = status,
                    statusServer = StatusModel.UN_SYNC.toStr()
                )

                RequestUpdateType.EXCHANGE -> request.copy(
                    exchange = v,
                    statusServer = StatusModel.UN_SYNC.toStr()
                )
            }
        }
    }

    private fun canModifyData(): Boolean {
        val shop = viewState.value.shop ?: return false
        val user = sharedViewModel.viewState.value.user ?: return false
        return isSameDay(shop.date, System.currentTimeMillis()) || user.isModOrAdminOrSys()
    }

    private suspend fun updateShop() {
        if (!canModifyData()) {
            sharedViewModel.message(Constants.ERROR.RESRTRAINT)
            return
        }

        val originalShop = viewState.value.shop ?: return


        try {
            val currentChoiceTypePay = viewState.value.typePay
            val getCash = viewState.value.getCash
            val getNoCash = viewState.value.getNoCash
            val moneyCash =
                if (getCash.isEmpty() && currentChoiceTypePay == NO_CASH) 0.0 else getCash.toDouble()
            val moneyNoCash =
                if (getNoCash.isEmpty() && currentChoiceTypePay == CASH) 0.0 else getNoCash.toDouble()
            val isOldPriceProduct = viewState.value.isOldPrice
            val updateShop = originalShop.copy(
                statusServer = StatusModel.UN_SYNC.toStr(),
                status = false,
                typePay = currentChoiceTypePay.getStringByTypePay(),
                cash = moneyCash,
                noCash = moneyNoCash,
                isOldPrice = isOldPriceProduct
            )

            updateViewState { it.copy(shop = updateShop, toggleConfirmSaveShopDialog = false) }

            room.shopDao().insertShop(
                updateShop
            )

            val response = updaterShop(shop = updateShop, status = true)

            if (response.success) {
                val finalShop = updateShop.copy(
                    statusServer = StatusModel.SYNC.toStr(),
                    status = true
                )
                room.shopDao().insertShop(shop = finalShop)
                updateViewState { it.copy(shop = finalShop) }
            } else {
                sharedViewModel.message(response.message)
            }

            val requests = room.requestDao().getRequests(idShop = updateShop.id, idTrip = updateShop.idTrip)
            val sumOrder = requests.sumOf {
                it.count * it.price - it.exchange * (if (updateShop.isOldPrice) it.oldPrice else it.price)
            }
            val arrear = (sumOrder + updateShop.arrears + updateShop.addSum) - (updateShop.cash + updateShop.noCash)
            updateClient(updateShop.copy(arrears = arrear))
        } catch (e: Exception) {
            sharedViewModel.message("Ошибка доставки на сервер данных магазина!")
            throw e
        } finally {
            sharedViewModel.backFromScreen()
        }

    }

    private suspend fun updaterShop(shop: ShopModel, status: Boolean): BaseResponse<ShopModel> {
          shop.apply {
              val shopRequest = ShopRequest(
                  id = id,
                  idTrip = idTrip,
                  idFactory = idFactory,
                  arrears = arrears,
                  addSum = addSum,
                  status = status,
                  date = date,
                  typePay = typePay,
                  cash = cash,
                  counter = counter,
                  noCash = noCash,
                  isOldPrice = isOldPrice,
                  cord = cord,
                  nameShop = nameShop,
                  isChanged = isChanged
              )
              return shopApi.update(shopRequest)
          }
    }

    private suspend fun updateClient(shop: ShopModel) {
        shop.let {
            val responseGetClient = clientApi.getClientById(id = shop.id)
            if (responseGetClient.success) {
                val client = responseGetClient.obj
                if (client != null ) {
                    val request = ClientRequest(
                        id = client.id,
                        idRoute = client.idRoute,
                        idFactory = client.idFactory,
                        name = client.name,
                        phone = client.phone,
                        cord = client.cord,
                        counter = client.counter,
                        arrears = shop.arrears,
                        date = client.date
                    )
                    val response = clientApi.update(request)
                    if (!response.success) {
                        sharedViewModel.message(response.message)
                    }
                }
            }
        }
    }


    private suspend fun modifyRequestInCurrentShop(
        requestToModify: RequestModel,
        transform: (RequestModel) -> RequestModel
    ) {
        val requests = viewState.value.requests
        val reqIndex = requests.indexOfFirst { it.id == requestToModify.id }
        if (reqIndex == -1) {
            sharedViewModel.message("Заявка не найдена")
            return
        }

        val currentRequest = requests[reqIndex]

        val newRequest = transform(currentRequest)

        val newRequests = requests.toMutableList().apply {
            set(reqIndex, newRequest)
        }

        updateViewState {
            it.copy(
                requests = newRequests,
            )
        }

        calculateOrder(requests = newRequests)
        updateRequest(newRequest)
    }

    private suspend fun updateRequest(request: RequestModel) {
        val shop = viewState.value.shop ?: return sharedViewModel.message(message = "Ошибка, магазин не найден!")
        val user = sharedViewModel.viewState.value.user ?: return sharedViewModel.message(message = "Ошибка, пользователь не найден!")

        val isAllowed = isSameDay(shop.date, System.currentTimeMillis()) || user.isModOrAdminOrSys()
        if (!isAllowed) {
            sharedViewModel.message(Constants.ERROR.RESRTRAINT)
            return
        }

        val currentRequests = viewState.value.requests
        val currentRequest = currentRequests.find { it.id == request.id }
            ?: return sharedViewModel.message("Заявка не найдена")

        val updatingRequest = currentRequest.copy(statusServer = StatusModel.UN_SYNC.toStr())
        room.requestDao().upsertRequest(request = updatingRequest)


        try {
            val response = requestApi.update(updatingRequest.toRequest())
            if (response.success) {
                val syncedRequest = updatingRequest.copy(statusServer = StatusModel.SYNC.toStr())
                room.requestDao().upsertRequest(syncedRequest)

                updateViewState { currentState ->
                    val updatedRequests = currentState.requests.map { req ->
                        if (req.id == syncedRequest.id) syncedRequest else req
                    }
                    currentState.copy(requests = updatedRequests)
                }
            } else {
                val failedRequest = updatingRequest.copy(statusServer = StatusModel.SYNC_FAILED.toStr())
                room.requestDao().upsertRequest(failedRequest)
                sharedViewModel.message(response.message)
            }
        } catch (e: Exception) {
            val failedRequest = updatingRequest.copy(statusServer = StatusModel.SYNC_FAILED.toStr())
            room.requestDao().upsertRequest(failedRequest)
            sharedViewModel.message("Сетевая ошибка!")
            throw e
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
        calculateOrder(requests = requests)
    }

    private fun calculateOrder(requests: List<RequestModel>) {
        val isOldPrice = viewState.value.isOldPrice

        var money = 0.0
        requests.forEach {
            money += it.count * it.price - it.exchange * (if (isOldPrice) it.oldPrice else it.price)
        }
        updateViewState { it.copy(orderMoney = money) }
    }

    private fun deleteShop() {
        launchCoroutine {
            val shop = viewState.value.shop
            if (shop != null) {
                val response = shopApi.delete(id = shop.id, idTrip = shop.idTrip)

                if (!response.success) {
                    sharedViewModel.message(response.message)
                } else {
                    room.requestDao().deleteRequestsByShopId(shopId = shop.id, tripId = shop.idTrip)
                    room.shopDao().deleteShop(shop)
                    updateViewState {
                        it.copy(toggleDeleteShop = false)
                    }
                    sharedViewModel.backFromScreen()
                }
            } else {
                sharedViewModel.message(Constants.ERROR.GENERAL_ERROR)
            }
        }
    }
}