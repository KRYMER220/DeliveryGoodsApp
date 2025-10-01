package ru.krymer.delivery.ui.screens.shop

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.krymer.delivery.AppDatabase
import ru.krymer.delivery.common.EventHandler
import ru.krymer.delivery.data.api.ClientApi
import ru.krymer.delivery.data.api.LoggerApi
import ru.krymer.delivery.data.api.MessageApi
import ru.krymer.delivery.data.api.ProductApi
import ru.krymer.delivery.data.api.RequestApi
import ru.krymer.delivery.data.api.ShopApi
import ru.krymer.delivery.data.api.TripApi
import ru.krymer.delivery.data.model.ClientModel
import ru.krymer.delivery.data.model.MessageModel
import ru.krymer.delivery.data.model.ProductModel
import ru.krymer.delivery.data.model.RequestModel
import ru.krymer.delivery.data.model.ShopLocalModel
import ru.krymer.delivery.data.model.ShopModel
import ru.krymer.delivery.data.model.TripModel
import ru.krymer.delivery.data.model.toLocal
import ru.krymer.delivery.data.model.toModel
import ru.krymer.delivery.data.model.utilModel.StatusModel
import ru.krymer.delivery.data.model.utilModel.TypePayModel
import ru.krymer.delivery.data.model.utilModel.TypePayModel.ANOTHER
import ru.krymer.delivery.data.model.utilModel.TypePayModel.CASH
import ru.krymer.delivery.data.model.utilModel.TypePayModel.NO_CASH
import ru.krymer.delivery.data.model.utilModel.getStringByTypePay
import ru.krymer.delivery.data.model.utilModel.getTypePayByString
import ru.krymer.delivery.data.model.utilModel.toStatusModel
import ru.krymer.delivery.data.model.utilModel.toStr
import ru.krymer.delivery.data.request.ClientRequest
import ru.krymer.delivery.data.request.CreateMessage
import ru.krymer.delivery.data.request.CreateRequestShopRequest
import ru.krymer.delivery.data.request.CreateShopRequest
import ru.krymer.delivery.data.request.TripRequest
import ru.krymer.delivery.data.request.UpdateRequestShopRequest
import ru.krymer.delivery.data.request.UpdateShopRequest
import ru.krymer.delivery.ui.screens.client.models.ClientEvent
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent
import ru.krymer.delivery.ui.screens.shop.models.ShopViewState
import ru.krymer.delivery.utills.Constants
import ru.krymer.delivery.utills.copyToClipboard
import ru.krymer.delivery.utills.isSameDay
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

enum class FunShop { SAVE, UPDATE, COPY }
enum class RequestUpdateType { COUNT, BONUS, EXCHANGE }
private enum class ShopUpdateType { ARREARS, ADD_SUM }

@HiltViewModel
class ShopViewModel @Inject constructor(
    private val shopApi: ShopApi,
    private val productApi: ProductApi,
    private val sharedViewModel: SharedViewModel,
    private val clientApi: ClientApi,
    private val tripApi: TripApi,
    private val requestApi: RequestApi,
    private val room: AppDatabase,
    private val messageApi: MessageApi,
    private val loggerApi: LoggerApi
) : ViewModel(), EventHandler<ShopEvent> {

    private val _viewState = MutableStateFlow(ShopViewState())
    val viewState = _viewState.asStateFlow()

    private fun updateViewState(update: (ShopViewState) -> ShopViewState) {
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

    override fun obtainEvent(event: ShopEvent) {
        when (event) {
            is ShopEvent.ShowAddDialogShopCurrentRoute -> openAddDialogCurrentClients()
            is ShopEvent.ShowAddDialogShopAllRoutes -> openAddDialogAllCurrentClients()
            is ShopEvent.DropDownSelectClient -> changeCurrentClient(event.shop)
            is ShopEvent.ChangeCountProduct -> changeRequestCountProduct(
                count = event.count, editProduct = event.product
            )
            is ShopEvent.ChangeCountBonusProduct -> changeRequestCountBonusProduct(
                bonus = event.bonus, editProduct = event.product
            )
            is ShopEvent.ShopAddAction -> initShopFunction(event = FunShop.SAVE)
            is ShopEvent.ShowDeleteDialog -> setState(delete = true)
            is ShopEvent.DismissDeleteDialog -> setState(delete = false)
            is ShopEvent.DeleteShop -> deleteShop()
            is ShopEvent.OpenGeoPoint -> openGeoPoint(event.context, event.cord)
            is ShopEvent.OpenRequest -> openRequest(event.shop)
            is ShopEvent.OpenMillageDialog -> {
                setState(millage = true)
                getDataForCourier()
                getListRequestsInfo()
            }
            is ShopEvent.MillageSaveAction -> saveMillage()
            is ShopEvent.ValueChangeMillage -> setValue(millage = event.millage)
            is ShopEvent.DismissRequestDialog -> setState(request = false)
            is ShopEvent.InitSaveRequestDialog -> initRequest()
            is ShopEvent.ShowRequestsInfoDialog -> {
                launchCoroutine {
                    setState(infoTrip = true)
                    getListRequestsInfo()
                }
            }
            is ShopEvent.DismissRequestInfoDialog -> setState(infoTrip = false)
            is ShopEvent.DismissAddSumDialog -> setState(addSum = false)
            is ShopEvent.OpenAddSumDialog -> setState(addSum = true)
            is ShopEvent.DismissAddDialog -> setState(add = false)
            is ShopEvent.ChangeAddSum -> updateShopValue(value = event.addSum, type = ShopUpdateType.ADD_SUM)
            is ShopEvent.ChangeArrears -> updateShopValue(value = event.arrears, type = ShopUpdateType.ARREARS)
            is ShopEvent.SaveAddSum -> {
                setState(addSum = false)
                val user = sharedViewModel.viewState.value.user
                user?.let {
                    if (user.isSys()) {
                        updateShopForAdmins()
                    }
                }
            }
            is ShopEvent.DismissMillageDialog -> setState(millage = false)
            is ShopEvent.UpdateInfo -> getListRequestsInfo()
            is ShopEvent.ShowDialogChangeArrears -> setState(arrears = true)
            is ShopEvent.DismissDialogChangeArrears -> setState(arrears = false)
            is ShopEvent.SaveArrears -> {
                setState(arrears = false)
                val user = sharedViewModel.viewState.value.user
                user?.let {
                    if (user.isSys()) {
                        updateShopForAdmins()
                    }
                }
            }
            is ShopEvent.SetArrearsInField -> updatePayment(amount = event.arrears)
            is ShopEvent.SetOrderInField -> updatePayment(amount = viewState.value.orderMoney)
            is ShopEvent.SetOrderAndArrearsSumInField -> updatePayment(amount = viewState.value.orderMoney + event.arrears)
            is ShopEvent.SetArrearsAndAddInField -> updatePayment(amount = event.sum)
            is ShopEvent.SetOrderAndAddInField -> updatePayment(amount = viewState.value.orderMoney + event.addSum)
            is ShopEvent.SetAddInField -> updatePayment(amount = event.addSum)
            is ShopEvent.SetOrderAndArrearsAndAddSumInField -> updatePayment(viewState.value.orderMoney + event.addSum + event.arrears)
            is ShopEvent.ValueChangeCash -> setValue(cash = event.money)
            is ShopEvent.ChangeTypePay -> showTypePayDialog()
            is ShopEvent.ChangeDropDownStateTypePayChanger -> setState(typePay = event.state)
            is ShopEvent.ValueChangeNoCashMoney -> setValue(noCash = event.money)
            is ShopEvent.ChangeExchangeRequest -> updateRequestValue(
                item = event.item,
                value = event.exchange,
                type = RequestUpdateType.EXCHANGE,
            )
            is ShopEvent.ChangeBonusRequest -> updateRequestValue(
                item = event.item,
                value = event.bonus,
                type = RequestUpdateType.BONUS,
            )
            is ShopEvent.ChangeCountRequest -> updateRequestValue(
                item = event.item,
                value = event.count,
                type = RequestUpdateType.COUNT,
            )
            is ShopEvent.RequestSaveAction -> {
                confirmSaveRequest()
                setState(confirmRequest = false)
            }

            is ShopEvent.DismissConfirmRequestDialog -> setState(confirmRequest = false)
            is ShopEvent.DismissInfoShopDialog -> {
                setState(infoShop = false)
            }
            is ShopEvent.OpenInfoShopDialog -> openInfoShopDialog(event.shop)
            is ShopEvent.DismissDialogAddRequest -> setState(addRequest = true)
            is ShopEvent.RequestAddAction -> initSaveOrUpdateRequest()
            is ShopEvent.ShowDialogAddRequest -> showAddRequestDialog()
            is ShopEvent.SwitchPrice -> switchStatePrice()
            is ShopEvent.CopyInfoData -> copyToClip(event.context)
            is ShopEvent.SwitchBonus -> switchBonus()
            is ShopEvent.ToggleMessageDialog -> setState(message = event.state)
            is ShopEvent.SendMessage -> sendMessage()
            is ShopEvent.ChangeMessage -> changeMessage(event.message)
            is ShopEvent.DeleteMessage -> deleteMessage(event.message)
            is ShopEvent.SwitchBonusState -> setState(bonus = !viewState.value.isBonusState)
            ShopEvent.ShowHideDialogAnalitic -> setState(analiticTrip = !viewState.value.toggleAnaliticOfTrip)
            is ShopEvent.CopyAndSaveShop -> {
                initShopFunction(event = FunShop.COPY)
            }
            is ShopEvent.ToggleLogsShopDialog -> toggleLogsShopDialog(shop = event.shop)
            ShopEvent.ChangeStateIsCopyDialog -> setState(copyAndSave = !viewState.value.isCopyAndSave)
            is ShopEvent.SelectShop -> {
                updateViewState { it.copy(currentShop = event.shop) }
            }
        }
    }

    private fun updateShopForAdmins() {
        launchCoroutine {
            val newShop = viewState.value.currentShop
            newShop?.apply {


                val list = viewState.value.listUIShop.map { it.copy() }.toMutableList()
                val index = list.indexOfFirst { it.id == newShop.id }
                list[index] = newShop.copy(statusServer = StatusModel.NOT_CHANGE, status = false)
                updateViewState { it.copy(listUIShop = list) }

                setState(request = false)

                room.shopDao().insertShop(
                    newShop.copy(statusServer = StatusModel.NOT_CHANGE, status = false).toLocal()
                )

                val shopRequest = UpdateShopRequest(
                    id = id,
                    idTrip = idTrip,
                    idFactory = idFactory,
                    arrears = arrears,
                    addSum = addSum,
                    status = false,
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
                }
            }
        }
    }

    private fun toggleLogsShopDialog(shop: ShopModel?) {
        setState(log = !viewState.value.toggleLogShop)
        if (viewState.value.toggleLogShop) shop?.let { getLogsShop(shop) }
        else updateViewState { it.copy(logShop = emptyList()) }
    }

    private fun setValue(
        cash: String = viewState.value.getCash,
        noCash: String = viewState.value.getNoCash,
        millage: Double = viewState.value.millage,
    ) {
        updateViewState {
            it.copy(
                getNoCash = if (noCash.isNotEmpty()) noCash else "",
                getCash = if (cash.isNotEmpty()) cash else "",
                millage = millage
            )
        }
    }

    private fun setState(
        add: Boolean = viewState.value.toggleAddDialog,
        delete: Boolean = viewState.value.toggleDeleteDialog,
        request: Boolean = viewState.value.toggleRequestDialog,
        millage: Boolean = viewState.value.toggleMillageDialog,
        infoShop: Boolean = viewState.value.toggleCurrentShopInfo,
        confirmRequest: Boolean = viewState.value.toggleConfirmRequestDialog,
        typePay: Boolean = viewState.value.toggleTypePayDialog,
        addSum: Boolean = viewState.value.toggleAddSumDialog,
        arrears: Boolean = viewState.value.toggleArrearsDialog,
        infoTrip: Boolean = viewState.value.toggleInfoTrip,
        addRequest: Boolean = viewState.value.toggleAddRequestDialog,
        message: Boolean = viewState.value.toggleMessageDialog,
        analiticTrip: Boolean = viewState.value.toggleAnaliticOfTrip,
        bonus: Boolean = viewState.value.isBonusState,
        log: Boolean = viewState.value.toggleLogShop,
        copyAndSave: Boolean = viewState.value.isCopyAndSave
    ) {
        updateViewState {
            it.copy(
                toggleAddDialog = add,
                toggleDeleteDialog = delete,
                toggleRequestDialog = request,
                toggleMillageDialog = millage,
                toggleCurrentShopInfo = infoShop,
                toggleConfirmRequestDialog = confirmRequest,
                toggleTypePayDialog = typePay,
                toggleAddSumDialog = addSum,
                toggleArrearsDialog = arrears,
                toggleInfoTrip = infoTrip,
                toggleAddRequestDialog = addRequest,
                toggleMessageDialog = message,
                toggleAnaliticOfTrip = analiticTrip,
                isBonusState = bonus,
                toggleLogShop = log,
                isCopyAndSave = copyAndSave
            )
        }

        if (infoShop) {
            updateViewState { it.copy(listCurrentShopInfo = emptyList()) }
        }
    }

    private fun getLogsShop(shop: ShopModel) {
        launchCoroutine {
            val logs = loggerApi.getLogsShop(idShop = shop.id, idTrip = shop.idTrip, idFactory = shop.idFactory).obj
            if (logs != null) {
                updateViewState { it.copy(logShop = logs) }
            }
        }
    }

    init {
        getDataProduct()
        initSettings()
    }

    private fun initSettings() {
        val value = sharedViewModel.viewState.value.lightVersion
        updateViewState { it.copy(lightVersion = value) }
    }

    fun initData(trip: TripModel) {
        launchCoroutine {
            val localTrip = room.tripDao().getTripById(trip.id)
            val user = sharedViewModel.viewState.value.user
            user?.let {
                if (localTrip != null) {
                    updateViewState { it.copy(currentTrip = localTrip) }
                    getLocalData(idTrip = trip.id)
                    if (user.id == trip.idCourier && localTrip.isLoaded) {
                        if (isSameDay(trip.date, System.currentTimeMillis())) {
                            updateShops(trip)
                            if (localTrip.millage > 0) {
                                val tripNew = tripApi.update(trip = TripRequest(
                                    id = localTrip.id,
                                    factoryId = localTrip.idFactory,
                                    date = localTrip.date,
                                    courierId = localTrip.idCourier,
                                    routeId = localTrip.idRoute,
                                    salary = localTrip.salary,
                                    percentCourier = localTrip.percentCourier,
                                    priceMillage = localTrip.priceMillage,
                                    millage = localTrip.millage,
                                    nameCourier = localTrip.nameCourier,
                                    nameRoute = localTrip.nameRoute,
                                    salaryCourier = localTrip.salaryCourier
                                )).obj
                                tripNew?.let {
                                    val updatedTrip = tripNew.copy(isLoaded = true)
                                    room.tripDao().upsertTrip(updatedTrip)
                                    updateViewState { it.copy(currentTrip = updatedTrip) }
                                    getDataShops()
                                }
                            } else {
                                val updatedTrip = trip.copy(isLoaded = true)
                                updateViewState { it.copy(currentTrip = updatedTrip) }
                                getDataShops()
                            }
                        } else {
                            val updatedTrip = localTrip.copy(isLoaded = true)
                            updateViewState { it.copy(currentTrip = updatedTrip) }
                            getDataShops()
                        }
                    } else {
                        room.tripDao().upsertTrip(trip)
                        getDataShops()
                        val response = tripApi.getTrip(trip.id)
                        val tripResponse = response.obj
                        if (response.success && tripResponse != null) {
                            room.tripDao().upsertTrip(tripResponse)
                            updateViewState { it.copy(currentTrip = tripResponse) }
                        }
                    }
                } else {
                    room.tripDao().upsertTrip(trip)
                    updateViewState { it.copy(currentTrip = trip) }
                    getDataShops()
                    if (user.id == trip.idCourier) {
                        val updatedTrip = trip.copy(isLoaded = true)
                        room.tripDao().upsertTrip(updatedTrip)
                        updateViewState { it.copy(currentTrip = updatedTrip) }
                    } else {
                        val response = tripApi.getTrip(trip.id)
                        val tripResponse = response.obj
                        if (response.success && tripResponse != null) {
                            room.tripDao().upsertTrip(tripResponse)
                            updateViewState { it.copy(currentTrip = tripResponse) }
                        }
                    }
                }
            }

        }
    }

    private suspend fun updateShops(trip: TripModel) {
        coroutineScope {
            ensureActive()
            val user = sharedViewModel.viewState.value.user ?: return@coroutineScope
            if (user.id != trip.idCourier) return@coroutineScope
            val shops = room.shopDao().getShops(idTrip = trip.id)
            if (shops.isEmpty()) return@coroutineScope

            for (local in shops) {
                ensureActive()
                var shop = local.toModel()
                if (shop.statusServer == StatusModel.UN_SYNC) {
                    val shopReq = UpdateShopRequest(
                        id = shop.id,
                        idTrip = shop.idTrip,
                        idFactory = shop.idFactory,
                        arrears = shop.arrears,
                        addSum = shop.addSum,
                        status = true,
                        date = shop.date,
                        typePay = shop.typePay.getStringByTypePay(),
                        cash = shop.cash,
                        counter = shop.counter,
                        noCash = shop.noCash,
                        isOldPrice = shop.isOldPrice,
                        cord = shop.cord,
                        nameShop = shop.nameShop,
                        isChanged = shop.isChanged
                    )

                    val shopResp = shopApi.update(shopReq)

                    if (shopResp.success) {
                        shop = shop.copy(status = true, statusServer = StatusModel.SYNC)
                        room.shopDao().insertShop(shop.toLocal())
                    } else {
                        sharedViewModel.message(shopResp.message)
                    }
                }

                val requests = room.requestDao().getRequests(
                    idShop = shop.id,
                    idTrip = shop.idTrip
                )

                for (req in requests) {
                    ensureActive()
                    if (req.statusServer.toStatusModel() == StatusModel.UN_SYNC) {
                        val reqReq = UpdateRequestShopRequest(
                            id = req.id,
                            idShop = req.idShop,
                            idTrip = req.idTrip,
                            idFactory = req.idFactory,
                            count = req.count,
                            exchange = req.exchange,
                            bonus = req.bonus,
                            status = req.status,
                            price = req.price,
                            oldPrice = req.oldPrice,
                            name = req.name,
                            counter = req.counter
                        )
                        val reqResp = requestApi.update(reqReq)
                        if (reqResp.success) {
                            room.requestDao()
                                .upsertRequest(req.copy(statusServer = StatusModel.SYNC.toStr()))
                        } else {
                            sharedViewModel.message(reqResp.message)
                        }
                    }
                }

                if (isSameDay(trip.date, System.currentTimeMillis()) && shop.status) {
                    val sumOrder =
                        requests.sumOf { it.count * it.price - it.exchange * (if (shop.isOldPrice) it.oldPrice else it.price) }
                    val arrear = (sumOrder + shop.arrears + shop.addSum) - (shop.cash + shop.noCash)
                    updateClient(shop.copy(arrears = arrear))
                }

                getLocalData(trip.id)
            }
        }
    }

    private fun deleteMessage(message: MessageModel) {
        launchCoroutine {
            messageApi.delete(message.id)
            val list = viewState.value.messages - message
            updateViewState { it.copy(messages = list) }
        }
    }

    private fun changeMessage(message: String = "") {
        updateViewState { it.copy(message = message) }
    }

    private fun sendMessage() {
        launchCoroutine {
            viewState.value.currentShop?.let {
                val message = viewState.value.message
                val obj = CreateMessage(
                    idClient = it.id, text = message, date = System.currentTimeMillis()
                )
                messageApi.add(message = obj)
                setState(message = false)
                val messages = getMessages(it.id)
                updateViewState { it.copy(messages = messages) }
            }
        }
    }

    private fun switchBonus() {
        val shop = viewState.value.currentShop
        if (shop != null) {
            updateViewState { it.copy(currentShop = shop.copy(isBonus = !shop.isBonus)) }
        }
    }

    private suspend fun getLocalData(idTrip: Long) {
        val shopsLocal = room.shopDao().getShops(idTrip = idTrip)
        val shops = mutableListOf<ShopModel>()
        shopsLocal.forEach { s ->
            val requests = room.requestDao().getRequests(idTrip = s.idTrip, idShop = s.id).sortedBy { it.counter }

            val shop = ShopModel(
                id = s.id,
                idTrip = s.idTrip,
                idFactory = s.idFactory,
                nameShop = s.nameShop,
                arrears = s.arrears,
                addSum = s.addSum,
                status = s.status,
                date = s.date,
                typePay = s.typePay.getTypePayByString(),
                cash = s.cash,
                counter = s.counter,
                noCash = s.noCash,
                listRequest = requests,
                isOldPrice = s.isOldPrice,
                cord = s.cord,
                isBonus = s.isBonus,
                isChanged = s.isChanged,
                statusServer = s.statusServer.toStatusModel(),
            )
            shops.add(shop)
        }
        updateViewState {
            it.copy(
                listUIShop = shops.sortedBy { it.counter },
            )
        }
    }

    private fun copyToClip(context: Context) {
        var text = ""
        var counter = 0
        viewState.value.listInfoRequests.forEach {
            val count = it.count + it.bonus
            counter += count
            text += "${it.name}: $count \n"
        }
        text += "Всего: $counter"
        copyToClipboard(context = context, text = text)
    }

    private fun switchStatePrice() {
        val isOldPrice = !viewState.value.stateSwitchPrice
        updateViewState { it.copy(stateSwitchPrice = isOldPrice) }
        calculateOrder()
    }

    private fun showAddRequestDialog() {
        val list = viewState.value.listDataRequests.map { it.copy() }
        setState(addRequest = true)
        updateViewState {
            it.copy(
                listProductRequest = viewState.value.listProduct
                    .filter { p -> p.isActive }
                    .filterNot { p -> list.any { pr -> pr.id == p.id } }
                    .map { p -> p.copy() }
            )
        }
    }

    private fun initSaveOrUpdateRequest() {
        val shop = viewState.value.currentShop
        if (shop != null) {
            saveRequest(
                shop = shop
            )
        } else {
            sharedViewModel.message(Constants.ERROR.AGAIN)
        }
    }

    private fun getAllDataClient() {
        launchCoroutine {
            val trip = viewState.value.currentTrip
            val list = viewState.value.listUIShop.map { it.copy() }
            if (trip != null) {
                val response = clientApi.getClientsByFactory(
                    idFactory = trip.idFactory
                )
                if (response.success) {
                    val clients =
                        response.obj?.filterNot { p -> list.any { pr -> pr.id == p.id } }
                            ?.sortedBy { s -> s.name }
                    if (clients != null) {
                        updateViewState {
                            it.copy(
                                listClient = clients,
                                currentClient = clients[0]
                            )
                        }
                        changeCurrentClient(client = clients[0])
                    }
                } else {
                    sharedViewModel.message(response.message)
                }
            }
        }
    }

    private fun getDataClientCurrentRoute() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val trip = viewState.value.currentTrip
                val list = viewState.value.listUIShop.map { it.copy() }
                if (trip != null) {
                    val response = clientApi.getClientsByRoute(
                        idRoute = trip.idRoute
                    )
                    if (response.success) {
                        val clients =
                            response.obj?.filterNot { p -> list.any { pr -> pr.id == p.id } }
                                ?.sortedBy { s -> s.counter }
                        if (clients != null) {
                            updateViewState {
                                it.copy(
                                    listClient = clients,
                                    currentClient = clients[0]
                                )
                            }
                            changeCurrentClient(client = clients[0])
                        }
                    } else {
                        sharedViewModel.message(response.message)
                    }

                }
            } catch (e: Exception) {
                sharedViewModel.message(e.message)
            }
        }
    }

    private fun getDataProduct() {
        launchCoroutine {
            val user = sharedViewModel.viewState.value.user
            if (user != null) {
                val response = productApi.getProducts(idFactory = user.idFactory)
                if (response.success) {
                    val products = response.obj
                    if (!products.isNullOrEmpty()) {
                        updateViewState {
                            it.copy(
                                listProduct = products,
                            )
                        }
                    }
                } else {
                    sharedViewModel.message(response.message)
                }
            }
        }
    }

    private suspend fun getDataShops() {
        val trip = _viewState.value.currentTrip
        if (trip != null) {
            val response = shopApi.getShopsByTrip(idTrip = trip.id)
            if (response.success) {
                val shops = response.obj
                if (!shops.isNullOrEmpty()) {
                    databaseInit(shops.map { it.copy(statusServer = StatusModel.NOT_CHANGE) }, trip.id)
                } else {
                    clearLocalShopsForTrip(trip.id)
                    updateViewState { it.copy(listUIShop = emptyList()) }
                    sharedViewModel.message(Constants.ERROR.LIST_EMPTY)
                }
            } else {
                sharedViewModel.message(response.message)
            }
        } else {
            sharedViewModel.message("Рейс не найден")
        }
    }

    private suspend fun clearLocalShopsForTrip(idTrip: Long) {
        room.withTransaction {
            room.requestDao().deleteRequestsByTrip(idTrip)
            room.shopDao().deleteShopsByTrip(idTrip)
        }
    }

    private suspend fun databaseInit(shops: List<ShopModel>, idTrip: Long) {
        room.withTransaction {
            val localShops = room.shopDao().getShops(idTrip = idTrip)
            val localIds = localShops.map { it.id }.toSet()
            val serverIds = shops.map { it.id }.toSet()

            val toDeleteIds = (localIds - serverIds)
                .filter { id ->
                    val status = room.shopDao().getShopStatus(id)
                    status.toStatusModel() == StatusModel.SYNC
                }

            if (toDeleteIds.isNotEmpty()) {
                room.requestDao().deleteRequestsByShopIds(toDeleteIds)
                room.shopDao().deleteShopsByIds(toDeleteIds)
            }

            val shopsToUpdate = mutableListOf<ShopLocalModel>()
            val shopsToInsert = mutableListOf<ShopLocalModel>()
            val requestsToInsert = mutableListOf<RequestModel>()
            val requestsToUpdate = mutableListOf<RequestModel>()

            for (serverShop in shops) {
                val localShop = localShops.find { it.id == serverShop.id }

                if (localShop != null) {
                    if (serverShop.status) {
                        shopsToUpdate.add(serverShop.toLocal().copy(
                            statusServer = StatusModel.SYNC.toStr(),
                        ))
                    } else {
                        if (localShop.statusServer.toStatusModel() == StatusModel.NOT_CHANGE) {
                            shopsToUpdate.add(
                                serverShop.toLocal().copy(
                                    statusServer = StatusModel.NOT_CHANGE.toStr(),
                                )
                            )
                        }
                        if (localShop.statusServer.toStatusModel() == StatusModel.SYNC) {
                            shopsToUpdate.add(
                                serverShop.toLocal().copy(
                                    statusServer = StatusModel.SYNC.toStr(),
                                )
                            )
                        }
                    }
                } else {
                    shopsToInsert.add(serverShop.toLocal())
                }

                val localRequests = room.requestDao().getRequests(
                    idShop = serverShop.id,
                    idTrip = serverShop.idTrip
                )
                val serverRequests = serverShop.listRequest.map { it.copy(statusServer = StatusModel.NOT_CHANGE.toStr()) }
                val localRequestIds = localRequests.map { it.id }.toSet()
                val serverRequestIds = serverRequests.map { it.id }.toSet()

                val requestsToDelete = (localRequestIds - serverRequestIds)
                    .filter { requestId ->
                        localRequests.find { it.id == requestId }?.statusServer?.toStatusModel() == StatusModel.SYNC
                    }

                if (requestsToDelete.isNotEmpty()) {
                    room.requestDao().deleteRequestsByIds(requestsToDelete.toList())
                }

                for (serverRequest in serverRequests) {
                    val localRequest = localRequests.find { it.id == serverRequest.id }

                    if (localRequest != null) {
                        if (localRequest.statusServer.toStatusModel() != StatusModel.UN_SYNC) {
                            requestsToUpdate.add(serverRequest.copy(
                                statusServer = when(localRequest.statusServer.toStatusModel()) {
                                    StatusModel.NOT_CHANGE -> StatusModel.NOT_CHANGE.toStr()
                                    StatusModel.SYNC -> StatusModel.SYNC.toStr()
                                    StatusModel.UN_SYNC -> StatusModel.UN_SYNC.toStr()
                                }
                            ))
                        }

                    } else {
                        requestsToInsert.add(serverRequest)
                    }
                }
            }

            if (shopsToUpdate.isNotEmpty()) {
                room.shopDao().updateShops(shopsToUpdate)
            }
            if (shopsToInsert.isNotEmpty()) {
                room.shopDao().insertShops(shopsToInsert)
            }

            if (requestsToUpdate.isNotEmpty()) {
                room.requestDao().updateRequests(requestsToUpdate)
            }
            if (requestsToInsert.isNotEmpty()) {
                room.requestDao().insertRequests(requestsToInsert)
            }

            getLocalData(idTrip)
        }
    }

    private fun openAddDialogCurrentClients() {
        getDataClientCurrentRoute()
        setState(add = true)
        updateViewState {
            it.copy(
                isShowDialogWithListCurrentClients = true,
                listProductRequest = viewState.value.listProduct.filter { p -> p.isActive }
                    .map { p -> p.copy() },
            )
        }
    }

    private fun openAddDialogAllCurrentClients() {
        getAllDataClient()
        setState(add = true)
        updateViewState {
            it.copy(
                isShowDialogWithListAllClients = true,
                listProductRequest = viewState.value.listProduct.filter { p -> p.isActive }
                    .map { p -> p.copy() },
            )
        }
    }

    private fun changeCurrentClient(client: ClientModel) {
        launchCoroutine {
            updateViewState { it.copy(currentClient = client) }
            val shops = room.shopDao().getShopsById(id = client.id).map { shop ->
                return@map shop.let {
                    val requests =
                        room.requestDao().getRequests(idTrip = shop.idTrip, idShop = shop.id)
                    ShopModel(
                        id = shop.id,
                        idTrip = shop.idTrip,
                        idFactory = shop.idFactory,
                        nameShop = shop.nameShop,
                        arrears = shop.arrears,
                        addSum = shop.addSum,
                        status = shop.status,
                        date = shop.date,
                        typePay = shop.typePay.getTypePayByString(),
                        cash = shop.cash,
                        counter = shop.counter,
                        noCash = shop.noCash,
                        listRequest = requests,
                        isOldPrice = shop.isOldPrice,
                        cord = shop.cord,
                        isBonus = shop.isBonus,
                        isChanged = shop.isChanged
                    )
                }
            }
            val messages = getMessages(client.id)
            updateViewState {
                it.copy(
                    listCurrentShopInfo = shops.sortedByDescending { it.date },
                    messages = messages
                )
            }
        }
    }

    private suspend fun getMessages(id: Long): List<MessageModel> {
        val messages = messageApi.getMessages(idClient = id).obj
        return messages ?: emptyList()
    }

    private fun updateProductRequest(
        editProduct: ProductModel,
        transform: (ProductModel) -> ProductModel
    ) {
        val list = viewState.value.listProductRequest.toMutableList()
        val index = list.indexOfFirst { it.id == editProduct.id }
        if (index == -1) return
        list[index] = transform(editProduct)
        updateViewState { it.copy(listProductRequest = list) }
    }

    private fun changeRequestCountProduct(count: String, editProduct: ProductModel) =
        updateProductRequest(editProduct) { it.copy(count = count.trim().toIntOrNull() ?: 0) }

    private fun changeRequestCountBonusProduct(bonus: String, editProduct: ProductModel) =
        updateProductRequest(editProduct) { it.copy(addCount = bonus.trim().toIntOrNull() ?: 0) }

    private fun initShopFunction(event: FunShop) {
        when(event) {
            FunShop.SAVE -> {
                saveShop(sendShop =  {
                    saveRequest(
                        shop = it
                    )
                })
            }

            FunShop.UPDATE -> {
                updateShop()
            }

            FunShop.COPY -> {
                launchCoroutine {
                    val shop = viewState.value.currentShop
                    if (shop != null) {
                        val client = clientApi.getClientById(shop.id).obj
                        if (client != null) {
                            setState(copyAndSave = false)
                            saveShop()
                            saveCopyRequest(shop = shop, client = client)
                        }
                    }
                }
            }
        }
    }

    private fun saveShop(sendShop: (ShopModel) -> Unit = {}) {
        launchCoroutine {
            val client = viewState.value.currentClient
            val trip = viewState.value.currentTrip
            if (client != null && trip != null) {
                val shopRequest = CreateShopRequest(
                    id = client.id,
                    idTrip = trip.id,
                    idFactory = trip.idFactory,
                    arrears = client.arrears,
                    date = trip.date,
                    counter = client.counter,
                    isOldPrice = false,
                    nameShop = client.name,
                    cord = client.cord,
                    addSum = viewState.value.add,
                    cash = viewState.value.dept,
                    status = false,
                    isChanged = false
                )
                val response = shopApi.add(shop = shopRequest)
                if (response.success) {
                    val shop = response.obj
                    shop?.let {
                        shop.listRequest = emptyList<RequestModel>()
                        setState(add = false)
                        sendShop(shop.copy(statusServer = StatusModel.NOT_CHANGE))
                        updateViewState {
                            it.copy(
                                listClient = it.listClient - client,
                                isBonusState = false,
                                isShowDialogWithListCurrentClients = false,
                                isShowDialogWithListAllClients = false,
                                currentClient = null,
                            )
                        }
                    }
                } else {
                    sharedViewModel.message(message = response.message)
                }
            }
        }
    }

    private fun saveCopyRequest(shop: ShopModel, client: ClientModel) {
        launchCoroutine {
            val trip = viewState.value.currentTrip
            trip?.let { trip ->
                val listRequest = shop.listRequest.map { it.copy(exchange = 0, status = false) }
                val hasBonus = listRequest.any { it.bonus > 0 }
                val shops = viewState.value.listUIShop.map { it.copy() }.toMutableList()
                shop.listRequest = listRequest.sortedBy { it.counter }
                shop.isBonus = hasBonus
                shop.status = false
                shop.date = trip.date
                shop.arrears = client.arrears
                shop.isOldPrice = false
                shop.noCash = 0.0
                shop.cash = 0.0
                shop.addSum = 0.0
                shop.typePay = CASH
                shop.idTrip = trip.id
                listRequest.forEach { product ->
                    product.apply {
                        val reqResponse = CreateRequestShopRequest(
                            id = id,
                            idShop = shop.id,
                            idTrip = shop.idTrip,
                            idFactory = product.idFactory,
                            count = product.count,
                            bonus = product.bonus,
                            status = false,
                            exchange = product.exchange,
                            price = product.price,
                            oldPrice = product.oldPrice,
                            name = product.name,
                            counter = product.counter
                        )
                        val response = requestApi.add(reqResponse)
                        if (response.success) {
                            val requestModel = RequestModel(
                                id = product.id,
                                idShop = shop.id,
                                idTrip = shop.idTrip,
                                idFactory = shop.idFactory,
                                count = product.count,
                                bonus = product.bonus,
                                status = false,
                                exchange = product.exchange,
                                price = product.price,
                                oldPrice = product.oldPrice,
                                name = product.name,
                                counter = product.counter
                            )
                            room.requestDao().upsertRequest(requestModel)
                        }
                    }
                }
                val existingIndex = shops.indexOfFirst { it.id == shop.id }
                if (existingIndex != -1) {
                    shops[existingIndex] = shop
                } else {
                    shops.add(shop)
                }
                updateViewState {
                    it.copy(
                        listUIShop = shops.sortedBy { s -> s.counter },
                        listDataRequests = listRequest.sortedBy { it.counter }
                    )
                }
            }
        }
    }

    private fun saveRequest(shop: ShopModel) {
        launchCoroutine {
            val listProduct = viewState.value.listProductRequest.map { it.copy() }
            val listRequest = shop.listRequest.toMutableList()
            listProduct.forEach { product ->
                val reqResponse = CreateRequestShopRequest(
                    id = product.id,
                    idShop = shop.id,
                    idTrip = shop.idTrip,
                    idFactory = shop.idFactory,
                    count = product.count,
                    bonus = product.addCount,
                    status = false,
                    exchange = product.exchange ?: 0,
                    price = product.price,
                    oldPrice = product.oldPrice,
                    name = product.name,
                    counter = product.counter
                )
                val response = requestApi.add(reqResponse)
                if (response.success) {
                    val requestModel = RequestModel(
                        id = product.id,
                        idShop = shop.id,
                        idTrip = shop.idTrip,
                        idFactory = shop.idFactory,
                        count = product.count,
                        bonus = product.addCount,
                        status = false,
                        exchange = product.exchange ?: 0,
                        price = product.price,
                        oldPrice = product.oldPrice,
                        name = product.name,
                        counter = product.counter
                    )
                    room.requestDao().upsertRequest(requestModel)
                    listRequest.add(requestModel.copy(statusServer = StatusModel.NOT_CHANGE.toStr()))
                }
            }
            val hasBonus = listRequest.any { it.bonus > 0 }
            val shops = viewState.value.listUIShop.map { it.copy() }.toMutableList()
            shop.listRequest = listRequest.sortedBy { it.counter }
            shop.isBonus = hasBonus
            val existingIndex = shops.indexOfFirst { it.id == shop.id }
            if (existingIndex != -1) {
                shops[existingIndex] = shop
            } else {
                shops.add(shop)
            }
            updateViewState {
                it.copy(
                    listUIShop = shops.sortedBy { s -> s.counter },
                    listDataRequests = listRequest.sortedBy { it.counter }
                )
            }
            calculateOrder()
            setState(addRequest = false)
        }
    }

    private fun deleteShop() {
        launchCoroutine {
            val shop = viewState.value.currentShop
            if (shop != null) {
                shop.listRequest.forEach { r ->
                    room.requestDao().deleteRequest(request = r)
                }
                val response = shopApi.delete(id = shop.id, idTrip = shop.idTrip)
                room.shopDao().deleteShop(shop.toLocal())
                if (response.success) {
                    val list = viewState.value.listUIShop.map { it.copy() }.toMutableList()
                    val item = list.first { it.id == shop.id }
                    val listNew = (list - item).sortedBy { it.counter }
                    updateViewState { it.copy(listUIShop = listNew) }
                    setState(request = false, delete = false)
                } else {
                    sharedViewModel.message(response.message)
                }
            } else {
                sharedViewModel.message(Constants.ERROR.GENERAL_ERROR)
            }
        }
    }

    private fun openGeoPoint(context: Context, cord: String) {
        launchCoroutine {
            if (cord.isNotEmpty()) {
                val cords = cord.split(",")
                if (cords[0].isNotEmpty() && cords[1].isNotEmpty()) {
                    val url =
                        "https://yandex.ru/maps/?ll=${cords[1]},${cords[0]}&z=12&pt=${cords[1]},${cords[0]},pm2"
                    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                    context.startActivity(intent)
                }
            } else {
                sharedViewModel.message(Constants.ERROR.MISSING_CORDS)
            }
        }
    }

    private fun openRequest(shop: ShopModel) {
        launchCoroutine {
            setState(request = true)
            updateViewState {
                it.copy(
                    currentShop = shop,
                    typePay = shop.typePay,
                    getCash = if (shop.cash > 0.0) shop.cash.toInt().toString() else "",
                    getNoCash = if (shop.noCash > 0.0) shop.noCash.toInt().toString() else "",
                    stateSwitchPrice = shop.isOldPrice,
                    listDataRequests = shop.listRequest
                )
            }
            calculateOrder()
            val messages = getMessages(shop.id)
            updateViewState { it.copy(messages = messages) }
        }
    }

    private fun getDataForCourier() {
        launchCoroutine {
            val trip = _viewState.value.currentTrip
            if (trip != null) {
                val response =
                    tripApi.getDataAboutTrip(idTrip = trip.id, idFactory = trip.idFactory)
                if (response.success) {
                    val getData = response.obj
                    if (getData != null) {
                        updateViewState {
                            it.copy(
                                millage = trip.millage,
                                noCash = getData.noCash,
                                salary = if (trip.millage > 0.0) getData.salary else 0.0,
                                cash = getData.cash,
                                allMoney = getData.allMoney,
                                remains = if (trip.millage > 0.0) getData.remainCash else 0.0,
                                salaryFix = trip.salary,
                                isDataShopForCourierLoad = true
                            )
                        }
                    }
                } else {
                    sharedViewModel.message(response.message)
                }
            }
        }
    }

    private fun saveMillage() {
        launchCoroutine {
            val millage = viewState.value.millage
            val trip = viewState.value.currentTrip
            val user = sharedViewModel.viewState.value.user
            if (trip != null && user != null) {
                if (user.id == trip.idCourier || user.isModOrAdminOrSys()) {
                    val newTrip = trip.copy(millage = millage)
                    room.tripDao().upsertTrip(newTrip)
                    updateViewState { it.copy(currentTrip = newTrip) }
                    val request = TripRequest(
                        id = trip.id,
                        factoryId = trip.idFactory,
                        date = trip.date,
                        courierId = trip.idCourier,
                        routeId = trip.idRoute,
                        salary = trip.salary,
                        percentCourier = trip.percentCourier,
                        priceMillage = trip.priceMillage,
                        millage = millage,
                        nameRoute = trip.nameRoute,
                        nameCourier = trip.nameCourier,
                    )
                    val response = tripApi.update(request)
                    if (response.success) {
                        getDataForCourier()
                    } else {
                        sharedViewModel.message(response.message)
                        getDataForCourier()
                    }
                } else {
                    sharedViewModel.message(Constants.ERROR.RESRTRAINT)
                    setState(millage = false)
                }
            }
            getDataForCourier()
        }
    }

    private fun calculateOrder() {
        val list = viewState.value.listDataRequests
        val isOldPrice = viewState.value.stateSwitchPrice

        var money = 0.0
        list.forEach {
            money += it.count * it.price - it.exchange * (if (isOldPrice) it.oldPrice else it.price)
        }
        updateViewState { it.copy(orderMoney = money) }
    }

    private fun getListRequestsInfo() {
        launchCoroutine {
            val trip = viewState.value.currentTrip
            if (trip != null) {
                val response = requestApi.getRequestsByTrip(
                    idTrip = trip.id, idFactory = trip.idFactory
                )
                if (response.success) {
                    val requests = response.obj?.sortedBy { it.price }
                    if (requests != null) {
                        var count = 0
                        var exchange = 0
                        requests.forEach {
                            count += it.count
                            count += it.bonus
                            exchange += it.exchange
                        }
                        updateViewState {
                            it.copy(
                                isLoadDataRequestsInfoDialog = true,
                                listInfoRequests = requests,
                                allCountRequestsInfo = count,
                                allExchangeRequestsInfo = exchange
                            )
                        }
                    } else {
                        sharedViewModel.message(Constants.EMPTY.EMPTY_LIST)
                    }
                } else {
                    sharedViewModel.message(message = response.message)
                }
            }
        }
    }

    private fun updateShopValue(value: String, type: ShopUpdateType) {
        val shop = viewState.value.currentShop
        val user = sharedViewModel.viewState.value.user
        if (shop != null && user != null) {
            val num = value.toDoubleOrNull() ?: 0.0
            val newShop = when (type) {
                ShopUpdateType.ARREARS -> shop.copy(arrears = num, isChanged = !user.isSysOrAdmin())
                ShopUpdateType.ADD_SUM -> shop.copy(addSum = num, isChanged = !user.isSysOrAdmin())
            }
            updateViewState { it.copy(currentShop = newShop) }
        }
    }

    private fun updatePayment(amount: Double) {
        when (viewState.value.typePay) {
            CASH -> updateViewState { it.copy(getCash = amount.toInt().toString()) }
            NO_CASH -> updateViewState { it.copy(getNoCash = amount.toInt().toString()) }
            ANOTHER -> {}
        }
    }

    private fun modifyRequestInCurrentShop(
        item: RequestModel,
        transform: (RequestModel) -> RequestModel
    ) {
        launchCoroutine {
            val shop = viewState.value.currentShop ?: return@launchCoroutine
            val requests = viewState.value.listDataRequests.map { it.copy() }.toMutableList()
            val reqIndex = requests.indexOfFirst { it.id == item.id }
            if (reqIndex == -1) return@launchCoroutine

            val newRequest = transform(item)
            requests[reqIndex] = newRequest



            val shops = viewState.value.listUIShop.map { it.copy() }.toMutableList()
            val shopIndex = shops.indexOfFirst { it.id == shop.id }
            val updatedShop = shop.copy(listRequest = requests)
            if (shopIndex != -1) shops[shopIndex] = updatedShop

            updateViewState {
                it.copy(
                    listDataRequests = requests,
                    listUIShop = shops
                )
            }

            calculateOrder()

            updateRequest(newRequest)
        }
    }

    private fun updateRequestValue(item: RequestModel, value: String, type: RequestUpdateType) {
        modifyRequestInCurrentShop(item) { req ->
            val v = value.trim().toIntOrNull() ?: 0
            val user = sharedViewModel.viewState.value.user
            val status = if (user != null) (!user.isSysOrAdmin() || req.status == true) else req.status
            when (type) {
                RequestUpdateType.COUNT -> req.copy(count = v, status = status, statusServer = StatusModel.UN_SYNC.toStr())
                RequestUpdateType.BONUS -> req.copy(bonus = v, status = status, statusServer = StatusModel.UN_SYNC.toStr())
                RequestUpdateType.EXCHANGE -> req.copy(exchange = v, statusServer = StatusModel.UN_SYNC.toStr())
            }
        }
    }

    private fun showTypePayDialog() {
        val currentType = _viewState.value.typePay
        val allTypes = TypePayModel.entries
        val currentIndex = allTypes.indexOf(currentType)
        val nextIndex = (currentIndex + 1) % allTypes.size
        val nextType = allTypes[nextIndex]
        val currentShop = viewState.value.currentShop
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

    private fun initRequest() {
        val shop = viewState.value.currentShop
        if (shop != null) if (shop.status) {
            _viewState.value = viewState.value.copy(toggleConfirmRequestDialog = true)
        } else {
            confirmSaveRequest()
        }
    }

    private fun confirmSaveRequest() {
        launchCoroutine {
            val trip = viewState.value.currentTrip
            if (trip != null) {
                val user = sharedViewModel.viewState.value.user
                user?.let {
                    if (isSameDay(trip.date, System.currentTimeMillis()) || user.isModOrAdminOrSys()) {
                        val shop = viewState.value.currentShop
                        if (shop != null) {
                            val typePay = viewState.value.typePay
                            val getCash = viewState.value.getCash
                            val getNoCash = viewState.value.getNoCash
                            val cash = if (getCash.isEmpty() && typePay == NO_CASH) 0.0 else getCash.toDouble()
                            val noCash = if (getNoCash.isEmpty() && typePay == CASH) 0.0 else getNoCash.toDouble()
                            val arrear = shop.arrears
                            val addSum = shop.addSum
                            val order = viewState.value.orderMoney
                            val newArrear = (order + arrear + addSum) - (cash + noCash)

                            updateClient(shop.copy(arrears = newArrear))

                            val dataShop = shop.copy(
                                addSum = addSum,
                                cash = cash,
                                status = false,
                                noCash = noCash,
                                typePay = typePay,
                                isOldPrice = viewState.value.stateSwitchPrice,
                            )
                            updateViewState { it.copy(currentShop = dataShop) }
                            initShopFunction(event = FunShop.UPDATE)
                        }
                    } else {
                        setState(request = false)
                        sharedViewModel.message(Constants.ERROR.RESRTRAINT)
                    }
                }
            }
        }
    }

    private fun updateClient(shop: ShopModel) {
        launchCoroutine {
            shop.apply {
                val responseGetClient = clientApi.getClientById(id = id)
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
                            arrears = arrears,
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
    }

    private fun updateShop() {
        launchCoroutine {
            val newShop = viewState.value.currentShop
            val trip = viewState.value.currentTrip
            newShop?.apply {

                val list = viewState.value.listUIShop.map { it.copy() }.toMutableList()
                val index = list.indexOfFirst { it.id == newShop.id }
                list[index] = newShop.copy(statusServer = StatusModel.UN_SYNC, status = false)
                updateViewState { it.copy(listUIShop = list) }

                setState(request = false)

                room.shopDao().insertShop(
                    newShop.copy(statusServer = StatusModel.UN_SYNC, status = false).toLocal()
                )

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
                    room.shopDao().insertShop(
                        newShop.copy(statusServer = StatusModel.SYNC, status = true).toLocal()
                    )
                    val updatedList = list.toMutableList()
                    updatedList[index] =
                        newShop.copy(statusServer = StatusModel.SYNC, status = true)
                    updateViewState { it.copy(listUIShop = updatedList) }
                }
                if (trip != null) {
                    updateShops(trip = trip)
                }
            }
        }
    }

    private fun updateRequest(request: RequestModel) {
        launchCoroutine {
            val trip = viewState.value.currentTrip ?: return@launchCoroutine
            val user = sharedViewModel.viewState.value.user ?: return@launchCoroutine

            if (isSameDay(trip.date, System.currentTimeMillis()) || user.isModOrAdminOrSys()) {

                room.requestDao()
                    .upsertRequest(request = request.copy(statusServer = StatusModel.UN_SYNC.toStr()))

                launchCoroutine {
                    val reqResponse = UpdateRequestShopRequest(
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
                        val updatedList = viewState.value.listDataRequests.map { it.copy() }.toMutableList()
                        val index = updatedList.indexOfFirst { it.id == request.id }
                        updatedList[index] = request.copy(statusServer = StatusModel.SYNC.toStr())
                        updateViewState { it.copy(listDataRequests = updatedList) }
                    }
                }
            } else {
                sharedViewModel.message(Constants.ERROR.RESRTRAINT)
                setState(request = false)
            }
        }
    }

    private fun openInfoShopDialog(shop: ShopModel) {
        setState(infoShop = true)
        updateViewState { it.copy(currentShop = shop) }
        getDataInfoShop(shop)
    }

    private fun getDataInfoShop(curShop: ShopModel) {
        launchCoroutine {
            val localList = room.shopDao().getShopsById(id = curShop.id).map { it.toModel() }
                .sortedByDescending { it.date }
            updateViewState { it.copy(listCurrentShopInfo = localList) }
            val response = shopApi.getCurrentShopsByFactory(
                id = curShop.id,
                idFactory = curShop.idFactory
            )
            if (response.success) {
                val list = response.obj
                if (list != null) {
                    updateViewState {
                        it.copy(
                            listCurrentShopInfo = list
                        )
                    }
                } else {
                    sharedViewModel.message(Constants.ERROR.LIST_EMPTY)
                }
            } else {
                sharedViewModel.message(response.message)
            }
        }
    }
}