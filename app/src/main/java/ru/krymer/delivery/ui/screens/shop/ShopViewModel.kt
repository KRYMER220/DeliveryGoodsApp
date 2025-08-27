package ru.krymer.delivery.ui.screens.shop

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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
import ru.krymer.delivery.data.model.ShopModel
import ru.krymer.delivery.data.model.TripModel
import ru.krymer.delivery.data.model.toLocal
import ru.krymer.delivery.data.model.toModel
import ru.krymer.delivery.data.model.utilModel.TypeMessageModel
import ru.krymer.delivery.data.model.utilModel.TypePayModel
import ru.krymer.delivery.data.model.utilModel.TypePayModel.ANOTHER
import ru.krymer.delivery.data.model.utilModel.TypePayModel.CASH
import ru.krymer.delivery.data.model.utilModel.TypePayModel.NO_CASH
import ru.krymer.delivery.data.model.utilModel.getStringByTypePay
import ru.krymer.delivery.data.model.utilModel.getTypePayByString
import ru.krymer.delivery.data.request.ClientRequest
import ru.krymer.delivery.data.request.CreateMessage
import ru.krymer.delivery.data.request.CreateRequestShopRequest
import ru.krymer.delivery.data.request.CreateShopRequest
import ru.krymer.delivery.data.request.UpdateRequestShopRequest
import ru.krymer.delivery.data.request.UpdateShopRequest
import ru.krymer.delivery.data.request.UpdateTripRequest
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import ru.krymer.delivery.ui.screens.shop.models.ShopAction
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent
import ru.krymer.delivery.ui.screens.shop.models.ShopViewState
import ru.krymer.delivery.utills.Constants
import ru.krymer.delivery.utills.copyToClipboard
import ru.krymer.delivery.utills.isSameDay
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

enum class FunShop {
    SAVE, UPDATE, COPY
}

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
                sharedViewModel.message(Constants.ERROR.CANCEL_OPERATION, type = TypeMessageModel.ERROR)
            } catch (e: Exception) {
                    sharedViewModel.message(e.message)
            }
        }
    }

    override fun obtainEvent(event: ShopEvent) {
        when (event) {
            is ShopEvent.ShowAddDialogShopCurrentRoute -> openAddDialogCurrentClients()
            is ShopEvent.ShowAddDialogShopAllRoutes -> openAddDialogAllCurrentClients()
            is ShopEvent.DismissSelectorClientInAddDialog -> dismissSelectorClientInAddDialog()
            is ShopEvent.DropDownSelectClient -> changeCurrentClient(event.shop)
            is ShopEvent.ChangeCountProduct -> changeRequestCountProduct(
                count = event.count, editProduct = event.product
            )

            is ShopEvent.ChangeCountBonusProduct -> changeRequestCountBonusProduct(
                bonus = event.bonus, editProduct = event.product
            )

            is ShopEvent.ShopAddAction -> initShopFunction(event = FunShop.SAVE)
            is ShopEvent.ShowDeleteDialog -> showDeleteDialog()
            is ShopEvent.DismissDeleteDialog -> dismissDeleteDialog()
            is ShopEvent.DeleteShop -> deleteShop()
            is ShopEvent.OpenGeoPoint -> openGeoPoint(event.context, event.cord)
            is ShopEvent.ShopActionInvoked -> shopActionInvoked()
            is ShopEvent.OpenRequest -> openRequest(event.shop)
            is ShopEvent.ShowSelectorClientInAddDialog -> showSelectorClientInAddDialog()
            is ShopEvent.OpenMillageDialog -> showDialogMillage()
            is ShopEvent.MillageSaveAction -> saveMillage()
            is ShopEvent.ValueChangeMillage -> changeMillage(event.millage)
            is ShopEvent.DismissRequestDialog -> dismissRequestDialog()
            is ShopEvent.InitSaveRequestDialog -> initRequest()
            is ShopEvent.ShowRequestsInfoDialog -> showInfoRequestsDialog()
            is ShopEvent.DismissRequestInfoDialog -> dismissInfoDialog()
            is ShopEvent.DismissAddSumDialog -> dismissAddSumDialog()
            is ShopEvent.OpenAddSumDialog -> openAddSumDialog()
            is ShopEvent.DismissAddDialog -> dismissAddDialog()
            is ShopEvent.ChangeAddSum -> changeAddSum(event.addSum)
            is ShopEvent.ChangeArrears -> changeArrears(event.arrears)
            is ShopEvent.SaveAddSum -> dismissAddSumDialog()
            is ShopEvent.DismissMillageDialog -> dismissMillageDialog()
            is ShopEvent.UpdateInfo -> getListRequestsInfo()
            is ShopEvent.ShowDialogChangeArrears -> showChangeArrearsDialog()
            is ShopEvent.DismissDialogChangeArrears -> dismissChangeArrearsDialog()
            is ShopEvent.SaveArrears -> dismissChangeArrearsDialog()
            is ShopEvent.SetArrearsInField -> updatePayment(amount = event.arrears)
            is ShopEvent.SetOrderInField -> updatePayment(amount = viewState.value.orderMoney)
            is ShopEvent.SetOrderAndArrearsSumInField -> updatePayment(amount = viewState.value.orderMoney + event.arrears)
            is ShopEvent.SetArrearsAndAddInField -> updatePayment(amount = event.sum)
            is ShopEvent.SetOrderAndAddInField -> updatePayment(amount = viewState.value.orderMoney + event.addSum)
            is ShopEvent.SetAddInField -> updatePayment(amount = event.addSum)
            is ShopEvent.SetOrderAndArrearsAndAddSumInField -> updatePayment(viewState.value.orderMoney + event.addSum + event.arrears)
            is ShopEvent.ValueChangeCash -> changeMoney(event.money)
            is ShopEvent.ChangeTypePay -> showTypePayDialog()
            is ShopEvent.ChangeDropDownStateTypePayChanger -> changeStateDropDownTypePay(event.state)
            is ShopEvent.ValueChangeNoCashMoney -> changeNoCashMoney(event.money)
            is ShopEvent.ChangeExchangeRequest -> changeExchangeRequest(event.item, event.exchange)
            is ShopEvent.ChangeBonusRequest -> changeBonusRequest(
                item = event.item, bonus = event.bonus
            )
            is ShopEvent.ChangeCountRequest -> changeCountRequest(event.item, event.count)
            is ShopEvent.RequestSaveAction -> {
                confirmSaveRequest()
                dismissConfirmDialog()
            }
            is ShopEvent.DismissConfirmRequestDialog -> dismissConfirmDialog()
            is ShopEvent.DismissInfoShopDialog -> dismissInfoShopDialog()
            is ShopEvent.OpenInfoShopDialog -> openInfoShopDialog(event.shop)
            is ShopEvent.DismissDialogAddRequest -> dismissAddRequestDialog()
            is ShopEvent.RequestAddAction -> initSaveOrUpdateRequest()
            is ShopEvent.ShowDialogAddRequest -> showAddRequestDialog()
            is ShopEvent.SwitchPrice -> switchStatePrice()
            is ShopEvent.CopyInfoData -> copyToClip(event.context)
            is ShopEvent.ChangeAdd -> changeAdd(event.add)
            is ShopEvent.ChangeArrear -> changeArrear(arrear = event.arrear)
            is ShopEvent.ChangeDept -> changeDept(dept = event.dept)
            is ShopEvent.ChangeAddStatusProduct -> changeAddStatusProduct(event.product)
            is ShopEvent.SwitchBonus -> switchBonus()
            is ShopEvent.ToggleMessageDialog -> toggleMessageDialog()
            is ShopEvent.SendMessage -> sendMessage()
            is ShopEvent.ChangeMessage -> changeMessage(event.message)
            is ShopEvent.DeleteMessage -> deleteMessage(event.message)
            is ShopEvent.SwitchBonusState -> switchStateBonus()
            ShopEvent.ShowHideDialogAnalitic -> showHideDialogAnalitic()
            is ShopEvent.CopyAndSaveShop -> {
                initShopFunction(event = FunShop.COPY)
            }
            is ShopEvent.ToggleLogsShopDialog -> toggleLogsShopDialog(shop = event.shop)
            ShopEvent.ChangeStateIsCopyDialog -> changeStateCopyDialog()
            is ShopEvent.SelectShop -> {
                updateViewState { it.copy(currentShop = event.shop) }
            }
        }
    }

    private fun changeStateCopyDialog() {
        updateViewState { it.copy(isCopyAndSave = !it.isCopyAndSave) }
    }

    private fun toggleLogsShopDialog(shop: ShopModel?) {
        updateViewState { it.copy(isShowInfoShop = !it.isShowInfoShop) }
        if (viewState.value.isShowInfoShop)
            shop?.let {
                getLogsShop(shop)
            }
        else
            updateViewState { it.copy(logShop = emptyList()) }
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
                        }
                        val updatedTrip = trip.copy(isLoaded = true)
                        room.tripDao().updateTrip(updatedTrip)
                        updateViewState { it.copy(currentTrip = updatedTrip) }
                        getDataShops()
                    } else {
                        room.tripDao().updateTrip(trip)
                        getDataShops()
                    }
                } else {
                    room.tripDao().insertTrip(trip)
                    updateViewState { it.copy(currentTrip = trip) }
                    getDataShops()
                    if (user.id == trip.idCourier) {
                        val updatedTrip = trip.copy(isLoaded = true)
                        room.tripDao().updateTrip(updatedTrip)
                        updateViewState { it.copy(currentTrip = updatedTrip) }
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

            val shopsLocal = room.shopDao().getShops(idTrip = trip.id)
            if (shopsLocal.isEmpty()) return@coroutineScope

            for (local in shopsLocal) {
                ensureActive()

                val shop = local.toModel()
                if (!shop.status) continue

                try {
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
                    if (!shopResp.success) {
                        sharedViewModel.message(shopResp.message)
                    }

                    val requests = room.requestDao().getRequests(idShop = shop.id, idTrip = shop.idTrip)
                    if (requests.isNotEmpty()) {
                        for (req in requests) {
                            ensureActive()
                            try {
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
                                if (!reqResp.success) {
                                    sharedViewModel.message(reqResp.message)
                                }
                            } catch (ex: CancellationException) {
                                throw ex
                            } catch (ex: Exception) {
                                sharedViewModel.message(ex.message ?: Constants.ERROR.ERROR)
                            }
                        }

                        val sum = requests.sumOf { it.count * it.price - it.exchange * (if (shop.isOldPrice) it.oldPrice else it.price) }
                        if (isSameDay(trip.date, System.currentTimeMillis())) {
                            updateClient(shop.copy(arrears = (sum + shop.arrears + shop.addSum) - (shop.cash + shop.noCash)))
                        }
                    }
                } catch (ex: CancellationException) {
                    throw ex
                } catch (ex: Exception) {
                    sharedViewModel.message(ex.message ?: Constants.ERROR.ERROR)
                }
            }
        }
    }


    private fun showHideDialogAnalitic() {
        updateViewState { it.copy(isShowAnaliticTrip = !it.isShowAnaliticTrip) }
    }

    private fun switchStateBonus() {
        updateViewState { it.copy(isBonusState = !it.isBonusState) }
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
                toggleMessageDialog()
                val messages = getMessages(it.id)
                updateViewState { it.copy(messages = messages) }
            }
        }
    }

    private fun toggleMessageDialog() {
        updateViewState { it.copy(isShowMessageDialog = !_viewState.value.isShowMessageDialog) }
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
            s.let {
                val requests =
                    room.requestDao().getRequests(idTrip = s.idTrip, idShop = s.id).sortedBy { it.counter }
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
                    isChanged = s.isChanged
                )
                shops.add(shop)
            }
        }
        updateViewState { it.copy(listUIShop = shops.sortedBy { it.counter }) }
    }

    private fun changeDept(dept: String) {
        updateViewState { it.copy(dept = if (dept.isNotEmpty()) dept.toDouble() else 0.0) }
    }

    private fun changeAdd(add: String) {
        updateViewState { it.copy(add = if (add.isNotEmpty()) add.toDouble() else 0.0) }
    }

    private fun changeArrear(arrear: String) {
        updateViewState { it.copy(arrear = if (arrear.isNotEmpty()) arrear.toDouble() else 0.0) }
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

    private fun dismissAddRequestDialog() {
        updateViewState { it.copy(showDialogAddRequest = false) }
    }

    private fun showAddRequestDialog() {
        val list = viewState.value.listDataRequests.map { it.copy() }
        updateViewState {
            it.copy(
                listProductRequest = viewState.value.listProduct
                    .filter { p -> p.isActive }
                    .filterNot { p -> list.any { pr -> pr.id == p.id } }
                    .map { p -> p.copy() },
                showDialogAddRequest = true,
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
                    databaseInit(shops, trip.id)
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
        val shopsToDelete = room.shopDao().getShops(idTrip = idTrip)
        shopsToDelete.forEach { shop ->
            room.shopDao().deleteShop(shop)
            val requestToDelete = room.requestDao().getRequests(idShop = shop.id, idTrip = shop.idTrip)
            requestToDelete.forEach { req ->
                room.requestDao().deleteRequest(req)
            }
        }
    }

    private suspend fun databaseInit(shops: List<ShopModel>, idTrip: Long) {
        val localShops = room.shopDao().getShops(idTrip = idTrip)
        val serverShopIds = shops.map { it.id }
        val localShopIds = localShops.map { it.id }

        localShops.forEach { localShop ->
            if (!serverShopIds.contains(localShop.id)) {
                room.shopDao().deleteShop(localShop)
                val requestToDelete = room.requestDao().getRequests(idShop = localShop.id, idTrip = localShop.idTrip)
                requestToDelete.forEach { req ->
                    room.requestDao().deleteRequest(req)
                }
            }
        }

        shops.forEach { shop ->
            shop.let {
                if (localShopIds.contains(shop.id)) {
                    room.shopDao().updateShop(shop.toLocal())
                    shop.listRequest.forEach { request ->
                        room.requestDao().updateRequest(request)
                    }
                } else {
                    room.shopDao().insertShop(shop.toLocal())
                    shop.listRequest.forEach { request ->
                        room.requestDao().insertRequest(request)
                    }
                }
            }
        }

        getLocalData(idTrip = idTrip)
    }

    private fun openAddDialogCurrentClients() {
        getDataClientCurrentRoute()
        updateViewState {
            it.copy(
                isShowDialogWithListCurrentClients = true,
                stateAddDialog = true,
                listProductRequest = viewState.value.listProduct.filter { p -> p.isActive }
                    .map { p -> p.copy() },
            )
        }
    }

    private fun openAddDialogAllCurrentClients() {
        getAllDataClient()
        updateViewState {
            it.copy(
                isShowDialogWithListAllClients = true,
                stateAddDialog = true,
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
                    listInfoShop = shops.sortedByDescending { it.date },
                    messages = messages
                )
            }
        }
    }

    private suspend fun getMessages(id: Long): List<MessageModel> {
        val messages = messageApi.getMessages(idClient = id).obj
        return messages ?: emptyList()
    }

    private fun dismissAddDialog() {
        updateViewState {
            it.copy(
                stateAddDialog = false,
                isShowDialogWithListCurrentClients = false,
                isShowDialogWithListAllClients = false,
                currentClient = null,
            )
        }
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

    private fun changeAddStatusProduct(editProduct: ProductModel) =
        updateProductRequest(editProduct) { it.copy(isAdd = !it.isAdd) }

    private fun changeRequestCountProduct(count: String, editProduct: ProductModel) =
        updateProductRequest(editProduct) { it.copy(count = count.trim().toIntOrNull() ?: 0) }

    private fun changeRequestCountBonusProduct(bonus: String, editProduct: ProductModel) =
        updateProductRequest(editProduct) { it.copy(addCount = bonus.trim().toIntOrNull() ?: 0) }

    private fun dismissSelectorClientInAddDialog() {
        updateViewState { it.copy(isShowSelectorClientInAddDialog = false) }
    }

    private fun showSelectorClientInAddDialog() {
        updateViewState { it.copy(isShowSelectorClientInAddDialog = true) }
    }

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
                            changeStateCopyDialog()
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
                        dismissAddDialog()
                        sendShop(shop)
                        updateViewState { it.copy(listClient = it.listClient - client, isBonusState = false) }
                    }
                } else {
                    sharedViewModel.message(message = response.message)
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
                    room.requestDao().insertRequest(requestModel)
                    listRequest.add(requestModel)
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
            dismissAddRequestDialog()
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
                              room.requestDao().insertRequest(requestModel)
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

    private fun showDeleteDialog() {
        updateViewState {
            it.copy(
                showDeleteDialog = true
            )
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
                    dismissDeleteDialog()
                    dismissRequestDialog()
                } else {
                    sharedViewModel.message(response.message)
                }
            } else {
                sharedViewModel.message(Constants.ERROR.GENERAL_ERROR)
            }
        }
    }

    private fun dismissDeleteDialog() {
        updateViewState {
            it.copy(
                showDeleteDialog = false, currentShop = null
            )
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

    private fun shopActionInvoked() {
        updateViewState { it.copy(shopAction = ShopAction.None) }
    }

    private fun openRequest(shop: ShopModel) {
        launchCoroutine {
            updateViewState {
                it.copy(
                    currentShop = shop,
                    showRequestDialog = true, isShowAddSumView = shop.addSum > 0.0,
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


    private fun showDialogMillage() {
        updateViewState { it.copy(isShowMillageDialog = true) }
        getDataForCourier()
        getListRequestsInfo()
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
                                isDataShopForCourierLoad = true,


                                )
                        }
                    }
                } else {
                    sharedViewModel.message(response.message)
                }
            }
        }
    }

    private fun dismissMillageDialog() {
        updateViewState { it.copy(isShowMillageDialog = false) }
    }


    private fun saveMillage() {
        launchCoroutine {
            val millage = viewState.value.millage
            val trip = viewState.value.currentTrip
            val user = sharedViewModel.viewState.value.user
            if (trip != null && user != null) {
                if (isSameDay(trip.date, System.currentTimeMillis()) || user.isModOrAdminOrSys()) {
                    val request = UpdateTripRequest(
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
                    val newTrip = trip.copy(millage = millage)
                    room.tripDao().updateTrip(newTrip)
                    updateViewState { it.copy(currentTrip = newTrip) }
                    updateShops(trip)
                    if (response.success) {
                        getDataForCourier()
                    } else {
                        sharedViewModel.message(response.message)
                    }
                } else {
                    sharedViewModel.message(Constants.ERROR.RESRTRAINT)
                    dismissMillageDialog()
                }
            }
        }
    }

    private fun changeMillage(millage: String) {
        updateViewState {
            it.copy(
                millage = if (millage.isEmpty()) 0.0 else millage.toDouble()
            )
        }
    }

    private fun dismissRequestDialog() {
        updateViewState {
            it.copy(
                showRequestDialog = false,
                currentShop = null,
                listDataRequests = emptyList(),
                orderMoney = 0.0,
                getCash = "",
                getNoCash = ""
            )
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

    private fun showInfoRequestsDialog() {
        updateViewState { it.copy(stateInfoDialog = true) }
        getListRequestsInfo()
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

    private fun dismissInfoDialog() {
        updateViewState { it.copy(stateInfoDialog = false) }
    }

    private fun changeArrears(arrears: String) {
        val shop = viewState.value.currentShop
        val user = sharedViewModel.viewState.value.user
        if (shop != null && user != null) {
            val newShop = shop.copy(arrears = if (arrears.isNotEmpty()) arrears.toDouble() else 0.0, isChanged = !user.isSysOrAdmin())
            updateViewState { it.copy(currentShop = newShop) }
        }
    }

    private fun showChangeArrearsDialog() {
        updateViewState { it.copy(isShowDialogArrears = true) }
    }

    private fun openAddSumDialog() {
        updateViewState { it.copy(isShowAddSumDialog = true, isShowAddSumView = true) }
    }

    private fun changeAddSum(add: String) {
        val shop = viewState.value.currentShop
        val user = sharedViewModel.viewState.value.user
        if (shop != null && user != null) {
            val newShop = shop.copy(addSum = if (add.isNotEmpty()) add.toDouble() else 0.0, isChanged = !user.isSysOrAdmin())
            updateViewState { it.copy(currentShop = newShop) }
        }
    }

    private fun dismissAddSumDialog() {
        updateViewState {
            it.copy(
                isShowAddSumDialog = false
            )
        }
    }

    private fun dismissChangeArrearsDialog() {
        updateViewState {
            it.copy(
                isShowDialogArrears = false
            )
        }
    }

    private fun changeNoCashMoney(money: String) {
        updateViewState {
            it.copy(
                getNoCash = if (money.isNotEmpty()) money else ""
            )
        }
    }

    private fun changeMoney(money: String) {
        updateViewState {
            it.copy(
                getCash = if (money.isNotEmpty()) money else ""
            )
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

            updateRequest(newRequest)

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
        }
    }


    private fun changeCountRequest(item: RequestModel, count: String) {
        modifyRequestInCurrentShop(item) { req ->
            val user = sharedViewModel.viewState.value.user
            val c = count.trim().toIntOrNull() ?: 0
            val status = if (user != null) (!user.isSysOrAdmin() || req.status == true) else req.status
            req.copy(count = c, status = status)
        }
    }

    private fun changeExchangeRequest(item: RequestModel, exchange: String) {
        modifyRequestInCurrentShop(item) { req ->
            val e = exchange.trim().toIntOrNull() ?: 0
            req.copy(exchange = e)
        }
    }

    private fun changeBonusRequest(item: RequestModel, bonus: String) {
        modifyRequestInCurrentShop(item) { req ->
            val user = sharedViewModel.viewState.value.user
            val b = bonus.trim().toIntOrNull() ?: 0
            val status = if (user != null) (!user.isSysOrAdmin() || req.status == true) else req.status
            req.copy(bonus = b, status = status)
        }
    }

    private fun changeStateDropDownTypePay(state: Boolean) {
        updateViewState { it.copy(isShowDropDownTypePay = state) }
    }


    private fun showTypePayDialog() {
        val currentType = _viewState.value.typePay
        val allTypes = TypePayModel.entries
        val currentIndex = allTypes.indexOf(currentType)
        val nextIndex = (currentIndex + 1) % allTypes.size
        val nextType = allTypes[nextIndex]
        updateViewState {
            it.copy(
                typePay = nextType,
                getNoCash = "",
                getCash = ""
            )
        }
    }

    private fun initRequest() {
        val shop = viewState.value.currentShop
        if (shop != null) if (shop.status) {
            _viewState.value = viewState.value.copy(stateConfirmRequestDialog = true)
        } else {
            confirmSaveRequest()
        }
    }

    private fun dismissConfirmDialog() {
        updateViewState { it.copy(stateConfirmRequestDialog = false) }
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
                            val getCash = viewState.value.getCash
                            val getNoCash = viewState.value.getNoCash
                            val cash = if (getCash.isEmpty()) 0.0 else getCash.toDouble()
                            val noCash = if (getNoCash.isEmpty()) 0.0 else getNoCash.toDouble()
                            val arrear = shop.arrears
                            val addSum = shop.addSum
                            val typePay = viewState.value.typePay
                            val order = viewState.value.orderMoney
                            val newArrear = (order + arrear + addSum) - (cash + noCash)
                            if (isSameDay(
                                    trip.date, System.currentTimeMillis()
                                ) || user.isModOrAdminOrSys()
                            ) {
                                updateClient(shop.copy(arrears = newArrear))
                            }
                            val dataShop = shop.copy(
                                addSum = addSum,
                                cash = cash,
                                noCash = noCash,
                                typePay = typePay,
                                status = true,
                                isOldPrice = viewState.value.stateSwitchPrice,
                            )
                            updateViewState { it.copy(currentShop = dataShop) }
                            initShopFunction(event = FunShop.UPDATE)
                        }
                    } else {
                        dismissRequestDialog()
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
            newShop?.apply {
                val shopRequest = UpdateShopRequest(
                    id = id,
                    idTrip = idTrip,
                    idFactory = idFactory,
                    arrears = arrears,
                    addSum = addSum,
                    status = status,
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
                room.shopDao().updateShop(newShop.toLocal())
                val list = viewState.value.listUIShop.map { it.copy() }.toMutableList()
                val index = list.indexOfFirst { it.id == newShop.id }
                list[index] = newShop
                updateViewState { it.copy(listUIShop = list) }
                dismissRequestDialog()
                val response = shopApi.update(shopRequest)
                if (!response.success) {
                    sharedViewModel.message(response.message)
                }
                val trip = viewState.value.currentTrip
                if (trip != null && isSameDay(trip.date, System.currentTimeMillis())) {
                    updateShops(trip)
                }
            }
        }
    }

    private fun updateRequest(request: RequestModel) {
        launchCoroutine {
            val trip = viewState.value.currentTrip
            if (trip != null) {
                sharedViewModel.viewState.value.user?.let { user ->
                    if (isSameDay(
                            trip.date,
                            System.currentTimeMillis()
                        ) || user.isModOrAdminOrSys()
                    ) {

                        room.requestDao().updateRequest(request)
                        request.apply {
                            val reqResponse = UpdateRequestShopRequest(
                                id = request.id,
                                idShop = idShop,
                                idTrip = idTrip,
                                idFactory = idFactory,
                                count = request.count,
                                bonus = request.bonus,
                                status = request.status,
                                exchange = request.exchange,
                                price = price,
                                oldPrice = oldPrice,
                                name = name,
                                counter = counter
                            )
                            val response = requestApi.update(reqResponse)
                            if (!response.success) {
                                sharedViewModel.message(response.message)
                            }
                        }
                    } else {
                        sharedViewModel.message(Constants.ERROR.RESRTRAINT)
                        dismissRequestDialog()
                    }
                }
            }
        }
    }

    private fun dismissInfoShopDialog() {
        updateViewState { it.copy(stateInfoShopDialog = false) }
    }

    private fun openInfoShopDialog(shop: ShopModel) {
        updateViewState { it.copy(stateInfoShopDialog = true, currentShop = shop) }
        getDataInfoShop(shop)
    }

    private fun getDataInfoShop(curShop: ShopModel) {
        launchCoroutine {
            val response = shopApi.getCurrentShopsByFactory(
                id = curShop.id,
                idFactory = curShop.idFactory
            )
            if (response.success) {
                val list = response.obj
                if (list != null) {
                    updateViewState {
                        it.copy(
                            listInfoShop = list
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