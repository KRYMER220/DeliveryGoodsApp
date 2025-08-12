package ru.krymer.delivery.ui.screens.shop

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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
import kotlin.math.log

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
        _viewState.update { update(it) }
    }

    private fun launchCoroutine(block: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                block()
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
            is ShopEvent.SetArrearsInField -> setArrears()
            is ShopEvent.SetOrderInField -> setOrder()
            is ShopEvent.SetOrderAndArrearsSumInField -> sumOrderAndArrears()
            is ShopEvent.SetArrearsAndAddInField -> setArrearsAndAdd()
            is ShopEvent.SetOrderAndAddInField -> setOrderAndAdd()
            is ShopEvent.SetAddInField -> setAdd()
            is ShopEvent.SetOrderAndArrearsAndAddSumInField -> sumOrderAndArrearsAndAdd()
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
            is ShopEvent.ShowMessageAddDialog -> showMessageDialog()
            is ShopEvent.DismissMessageAddDialog -> dismissMessageDialog()
            is ShopEvent.SendMessage -> sendMessage()
            is ShopEvent.ChangeMessage -> changeMessage(event.message)
            is ShopEvent.DeleteMessage -> deleteMessage(event.message)
            is ShopEvent.SwitchBonusState -> switchStateBonus()
            ShopEvent.ShowHideDialogAnalitic -> showHideDialogAnalitic()
            is ShopEvent.CopyAndSaveShop -> {
                updateViewState { it.copy(currentShop = event.shop) }
                initShopFunction(event = FunShop.COPY)
            }

            is ShopEvent.ShowInfoShop -> isShowLogShopInfo(event.shop)
            is ShopEvent.DismissLogShopDialog -> dismissLogShopDialog()
        }
    }

    private fun isShowLogShopInfo(shop: ShopModel) {
        launchCoroutine {
            updateViewState { it.copy(isShowInfoShop = true) }
            val logs = loggerApi.getLogsShop(idShop = shop.id, idTrip = shop.idTrip, idFactory = shop.idFactory).obj
            if (logs != null) {
                updateViewState { it.copy(logShop = MutableStateFlow(logs)) }
            }
        }
    }

    private fun dismissLogShopDialog() {
        updateViewState { it.copy(isShowInfoShop = false, logShop = MutableStateFlow(emptyList())) }
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
            if (isSameDay(trip.date, System.currentTimeMillis())) {
                updateShops(trip)
            }
            val localTrip = room.tripDao().getTripById(trip.id)
            val user = sharedViewModel.viewState.value.user.value
            user?.let {
                if (localTrip != null) {
                    updateViewState { it.copy(currentTrip = MutableStateFlow(localTrip)) }
                    getLocalData(idTrip = trip.id)
                    if (user.id != trip.idCourier || !localTrip.isLoaded) {
                        getDataShops()
                        if (user.id == trip.idCourier) {
                            val updatedTrip = trip.copy(isLoaded = true)
                            room.tripDao().updateTrip(updatedTrip)
                            updateViewState { it.copy(currentTrip = MutableStateFlow(updatedTrip)) }
                        } else {
                            room.tripDao().updateTrip(trip)
                        }
                    } else {
                        getDataShops()
                    }
                } else {
                    room.tripDao().insertTrip(trip)
                    getDataShops()
                    updateViewState { it.copy(currentTrip = MutableStateFlow(trip)) }
                    if (user.id == trip.idCourier) {
                        val updatedTrip = trip.copy(isLoaded = true)
                        room.tripDao().updateTrip(updatedTrip)
                        updateViewState { it.copy(currentTrip = MutableStateFlow(updatedTrip)) }
                    }
                }
            }

        }
    }

    private fun updateShops(trip: TripModel) {
        launchCoroutine {
            val user = sharedViewModel.viewState.value.user.value
            if (user != null && user.id == trip.idCourier) {
                val shops = room.shopDao().getShops(idTrip = trip.id)
                if (shops.isNotEmpty()) {
                    shops.forEach { it ->
                        it?.toModel()?.let { shop ->
                                if (shop.status) {
                                    shop.apply {
                                        shopApi.update(
                                            UpdateShopRequest(
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
                                        )

                                    val requests =
                                        room.requestDao().getRequests(shop.id, shop.idTrip)
                                    if (requests.isNotEmpty()) {
                                        requests.forEach { req ->
                                            requestApi.update(
                                                UpdateRequestShopRequest(
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
                                            )
                                        }
                                        val sum =
                                            requests.sumOf { it.count * it.price - it.exchange * (if (isOldPrice) it.oldPrice else it.price) }
                                        if (isSameDay(trip.date, System.currentTimeMillis()))
                                            updateClient(shop.copy(arrears = (sum + shop.arrears + shop.addSum) - (shop.cash + shop.noCash)))
                                    }
                                    }
                                }
                        }
                    }
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
            val list = viewState.value.messages.value - message
            updateViewState { it.copy(messages = MutableStateFlow(list)) }
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
                dismissMessageDialog()
            }
        }
    }

    private fun dismissMessageDialog() {
        updateViewState { it.copy(isShowMessageDialog = false) }
    }

    private fun showMessageDialog() {
        updateViewState { it.copy(isShowMessageDialog = true) }
    }

    private fun switchBonus() {
        val shop = viewState.value.currentShop
        if (shop != null) {
            updateViewState { it.copy(currentShop = shop.copy(isBonus = !shop.isBonus)) }
        }
    }

    private fun getLocalData(idTrip: Long) {
        launchCoroutine {
            val shopsLocal = room.shopDao().getShops(idTrip = idTrip)
            val shops = mutableListOf<ShopModel>()
            shopsLocal.forEach { s ->
                s?.let {
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
            updateViewState { it.copy(listShop = MutableStateFlow(shops.sortedBy { it.counter })) }
        }
    }

    private fun changeDept(dept: String) {
        updateViewState { it.copy(dept = MutableStateFlow(if (dept.isNotEmpty()) dept.toDouble() else 0.0)) }
    }

    private fun changeAdd(add: String) {
        updateViewState { it.copy(add = MutableStateFlow(if (add.isNotEmpty()) add.toDouble() else 0.0)) }
    }

    private fun changeArrear(arrear: String) {
        updateViewState { it.copy(arrear = MutableStateFlow(if (arrear.isNotEmpty()) arrear.toDouble() else 0.0)) }
    }


    private fun copyToClip(context: Context) {
        var text = ""
        var counter = 0
        viewState.value.listInfoRequests.value.forEach {
            val count = it.count + it.bonus
            counter += count
            text += "${it.name}: $count \n"
        }
        text += "Всего: $counter"
        copyToClipboard(context = context, text = text)
    }

    private fun switchStatePrice() {
        val isOldPrice = !viewState.value.stateSwitchPrice.value
        updateViewState { it.copy(stateSwitchPrice = MutableStateFlow(isOldPrice)) }
        calculateOrder()
    }

    private fun dismissAddRequestDialog() {
        updateViewState { it.copy(showDialogAddRequest = false) }
    }

    private fun showAddRequestDialog() {
        val list = viewState.value.listDataRequests.value.map { it.copy() }
        updateViewState {
            it.copy(
                listProductRequest = MutableStateFlow(viewState.value.listProduct.value
                .filter { p -> p.isActive }
                .filterNot { p -> list.any { pr -> pr.id == p.id } }
                .map { p -> p.copy() }),
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
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val trip = viewState.value.currentTrip.value
                val list = viewState.value.listShop.value.map { it.copy() }
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
                                    listClient = MutableStateFlow(clients),
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

    private fun getDataClientCurrentRoute() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val trip = viewState.value.currentTrip.value
                val list = viewState.value.listShop.value.map { it.copy() }
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
                                    listClient = MutableStateFlow(clients),
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
            val user = sharedViewModel.viewState.value.user.value
            if (user != null) {
                val response = productApi.getProducts(idFactory = user.idFactory)
                if (response.success) {
                    val products = response.obj
                    if (!products.isNullOrEmpty()) {
                        updateViewState {
                            it.copy(
                                listProduct = MutableStateFlow(products),
                            )
                        }
                    }
                } else {
                    sharedViewModel.message(response.message)
                }
            }
        }
    }

    private fun getDataShops() {
        launchCoroutine {
            val trip = _viewState.value.currentTrip.value
            if (trip != null) {
                val response = shopApi.getShopsByTrip(idTrip = trip.id)
                if (response.success) {
                    val shops = response.obj
                    if (!shops.isNullOrEmpty()) {
                        databaseInit(shops, trip.id)
                    }
                } else {
                    sharedViewModel.message(response.message)
                }
            }
        }
    }

    private fun databaseInit(shops: List<ShopModel>, idTrip: Long) {
        launchCoroutine {
            shops.forEach { shop ->
                shop.let {
                    room.shopDao().insertShop(shop.toLocal())
                    shop.listRequest.forEach { request ->
                        room.requestDao().insertRequest(request)
                    }
                }
            }
            getLocalData(idTrip = idTrip)
        }
    }

    private fun openAddDialogCurrentClients() {
        getDataClientCurrentRoute()
        updateViewState {
            it.copy(
                isShowDialogWithListCurrentClients = true,
                stateAddDialog = true,
                listProductRequest = MutableStateFlow(viewState.value.listProduct.value.filter { p -> p.isActive }
                    .map { p -> p.copy() }),
            )
        }
    }

    private fun openAddDialogAllCurrentClients() {
        getAllDataClient()
        updateViewState {
            it.copy(
                isShowDialogWithListAllClients = true,
                stateAddDialog = true,
                listProductRequest = MutableStateFlow(viewState.value.listProduct.value.filter { p -> p.isActive }
                    .map { p -> p.copy() }),
            )
        }
    }

    private fun changeCurrentClient(client: ClientModel) {
        launchCoroutine {
            updateViewState { it.copy(currentClient = client) }
            val shops = room.shopDao().getShopsById(id = client.id).mapNotNull { shop ->
                return@mapNotNull shop?.let {
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
            val messages = messageApi.getMessages(idClient = client.id).obj
            updateViewState {
                it.copy(
                    listInfoShop = MutableStateFlow(shops.sortedByDescending { it.date }),
                    messages = MutableStateFlow(messages ?: listOf())
                )
            }
        }
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

    private fun changeAddStatusProduct(editProduct: ProductModel) {
        val list = viewState.value.listProductRequest.value.map { it.copy() }.toMutableList()
        val index = list.indexOfFirst { it.id == editProduct.id }
        val product = editProduct.copy(isAdd = !editProduct.isAdd)
        list[index] = product
        updateViewState {
            it.copy(
                listProductRequest = MutableStateFlow(list)
            )
        }
    }

    private fun changeRequestCountBonusProduct(bonus: String, editProduct: ProductModel) {
        val list = viewState.value.listProductRequest.value.map { it.copy() }.toMutableList()
        val index = list.indexOfFirst { it.id == editProduct.id }
        val product = if (bonus.isNotEmpty()) {
            editProduct.copy(addCount = bonus.trim().toInt())
        } else {
            editProduct.copy(addCount = 0)
        }
        list[index] = product
        updateViewState {
            it.copy(
                listProductRequest = MutableStateFlow(list)
            )
        }
    }

    private fun changeRequestCountProduct(count: String, editProduct: ProductModel) {
        val list = viewState.value.listProductRequest.value.map { it.copy() }.toMutableList()
        val index = list.indexOfFirst { it.id == editProduct.id }
        val product = if (count.isNotEmpty()) {
            editProduct.copy(count = count.trim().toInt())
        } else {
            editProduct.copy(count = 0)
        }
        list[index] = product
        updateViewState {
            it.copy(
                listProductRequest = MutableStateFlow(list)
            )
        }
    }

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
                saveShop()
                val shop = viewState.value.currentShop
                if (shop != null)
                saveCopyRequest(shop = shop)
            }
        }
    }

    private fun saveShop(sendShop: (ShopModel) -> Unit = {}) {
        launchCoroutine {
            val client = viewState.value.currentClient
            val trip = viewState.value.currentTrip.value
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
                    addSum = viewState.value.add.value,
                    cash = viewState.value.dept.value,
                    status = false,
                    isChanged = false
                )
                val response = shopApi.add(shop = shopRequest)
                if (response.success) {
                    val shop = response.obj
                    if (shop != null) {
                        shop.listRequest = emptyList<RequestModel>()
                        dismissAddDialog()
                        sendShop(shop)
                        room.shopDao().insertShop(shop = shop.toLocal())
                        updateViewState { it.copy(listClient = MutableStateFlow(it.listClient.value - client), isBonusState = false) }
                    }
                } else {
                    sharedViewModel.message(message = response.message)
                }
            }
        }
    }

    private fun saveRequest(shop: ShopModel) {
        launchCoroutine {
            val listProduct = viewState.value.listProductRequest.value.map { it.copy() }
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
                    dismissAddRequestDialog()
                }
            }
            val hasBonus = listRequest.any { it.bonus > 0 }
            val shops = viewState.value.listShop.value.map { it.copy() }.toMutableList()
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
                    listShop = MutableStateFlow(shops.sortedBy { s -> s.counter }),
                    listDataRequests = MutableStateFlow(listRequest.sortedBy { it.counter })
                )
            }
            calculateOrder()
        }
    }

    private fun saveCopyRequest(shop: ShopModel) {
        launchCoroutine {
            val trip = viewState.value.currentTrip.value
            val client = viewState.value.currentClient
            trip?.let { trip ->
               client?.let { client ->
                   val listRequest = shop.listRequest.map { it.copy(exchange = 0, status = false) }
                   val hasBonus = listRequest.any { it.bonus > 0 }
                   val shops = viewState.value.listShop.value.map { it.copy() }.toMutableList()
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
                           listShop = MutableStateFlow(shops.sortedBy { s -> s.counter }),
                           listDataRequests = MutableStateFlow(listRequest.sortedBy { it.counter })
                       )
                   }
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
                    val list = viewState.value.listShop.value.map { it.copy() }.toMutableList()
                    val item = list.first { it.id == shop.id }
                    val listNew = (list - item).sortedBy { it.counter }
                    updateViewState { it.copy(listShop = MutableStateFlow(listNew)) }
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
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
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
                    typePay = MutableStateFlow(shop.typePay), getCash = MutableStateFlow(
                        if (shop.cash > 0.0) shop.cash.toInt().toString() else ""
                    ), getNoCash = MutableStateFlow(
                        if (shop.noCash > 0.0) shop.noCash.toInt().toString() else ""
                    ),
                    stateSwitchPrice = MutableStateFlow(shop.isOldPrice),
                    listDataRequests = MutableStateFlow(shop.listRequest)
                )
            }
            calculateOrder()
            room.shopDao().insertShop(shop = shop.toLocal())
            shop.listRequest.forEach {
                room.requestDao().insertRequest(it)
            }
        }
    }


    private fun showDialogMillage() {
        updateViewState { it.copy(isShowMillageDialog = true) }
        getDataForCourier()
        getListRequestsInfo()
    }

    private fun getDataForCourier() {
        launchCoroutine {
            val trip = _viewState.value.currentTrip.value
            if (trip != null) {
                val response =
                    tripApi.getDataAboutTrip(idTrip = trip.id, idFactory = trip.idFactory)
                if (response.success) {
                    val getData = response.obj
                    if (getData != null) {
                        updateViewState {
                            it.copy(
                                millage = MutableStateFlow(trip.millage),
                                noCash = MutableStateFlow(getData.noCash),
                                salary = if (trip.millage > 0.0) MutableStateFlow(getData.salary) else MutableStateFlow(
                                    0.0
                                ),
                                cash = MutableStateFlow(getData.cash),
                                allMoney = MutableStateFlow(getData.allMoney),
                                remains = if (trip.millage > 0.0) MutableStateFlow(getData.remainCash) else MutableStateFlow(
                                    0.0
                                ),
                                salaryFix = MutableStateFlow(trip.salary),
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
            val millage = viewState.value.millage.value
            val trip = viewState.value.currentTrip.value
            val user = sharedViewModel.viewState.value.user.value
            if (trip != null && user != null) {
                if (user.id == trip.idCourier || user.isSysOrAdmin()) {
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
                    updateViewState { it.copy(currentTrip = MutableStateFlow(newTrip)) }
                    if (response.success) {
                        getDataForCourier()
                    } else {
                        sharedViewModel.message(response.message)
                    }
                } else {
                    dismissMillageDialog()
                    sharedViewModel.message(Constants.ERROR.RESRTRAINT)
                }
            }
        }
    }

    private fun changeMillage(millage: String) {
        updateViewState {
            it.copy(
                millage = if (millage.isEmpty()) MutableStateFlow(0.0) else MutableStateFlow(
                    millage.toDouble()
                )
            )
        }
    }

    private fun dismissRequestDialog() {
        updateViewState {
            it.copy(
                showRequestDialog = false,
                currentShop = null,
                listDataRequests = MutableStateFlow(
                    listOf()
                ),
                orderMoney = MutableStateFlow(0.0),
                getCash = MutableStateFlow(""),
                getNoCash = MutableStateFlow("")
            )
        }
    }

    private fun calculateOrder() {
        val list = viewState.value.listDataRequests.value
        val isOldPrice = viewState.value.stateSwitchPrice.value

        var money = 0.0
        list.forEach {
            money += it.count * it.price - it.exchange * (if (isOldPrice) it.oldPrice else it.price)
        }
        updateViewState { it.copy(orderMoney = MutableStateFlow(money)) }
    }

    private fun showInfoRequestsDialog() {
        updateViewState { it.copy(stateInfoDialog = true) }
        getListRequestsInfo()
    }

    private fun getListRequestsInfo() {
        launchCoroutine {
            val trip = viewState.value.currentTrip.value
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
                                listInfoRequests = MutableStateFlow(requests),
                                allCountRequestsInfo = MutableStateFlow(count),
                                allExchangeRequestsInfo = MutableStateFlow(exchange)
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
        val user = sharedViewModel.viewState.value.user.value
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
        val user = sharedViewModel.viewState.value.user.value
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
                getNoCash = if (money.isNotEmpty()) MutableStateFlow(
                    money
                ) else MutableStateFlow("")
            )
        }
    }

    private fun changeMoney(money: String) {
        updateViewState {
            it.copy(
                getCash = if (money.isNotEmpty()) MutableStateFlow(
                    money
                ) else MutableStateFlow("")
            )
        }
    }

    private fun sumOrderAndArrearsAndAdd() {
        val shop = viewState.value.currentShop
        if (shop != null) {
            when (viewState.value.typePay.value) {
                CASH -> updateCash((viewState.value.orderMoney.value + shop.arrears + shop.addSum).toInt())
                NO_CASH -> updateNoCash((viewState.value.orderMoney.value + shop.arrears + shop.addSum).toInt())
                ANOTHER -> {}
            }
        }
    }

    private fun updateNoCash(noCash: Int) {
        updateViewState { it.copy(getNoCash = MutableStateFlow("$noCash")) }
    }

    private fun updateCash(cash: Int) {
        updateViewState { it.copy(getCash = MutableStateFlow("$cash")) }
    }

    private fun setArrearsAndAdd() {
        val shop = viewState.value.currentShop
        if (shop != null) {
            when (viewState.value.typePay.value) {
                CASH -> updateCash((shop.arrears + shop.addSum).toInt())
                NO_CASH -> updateNoCash((shop.arrears + shop.addSum).toInt())
                ANOTHER -> {}
            }
        }
    }

    private fun setOrderAndAdd() {
        val shop = viewState.value.currentShop
        if (shop != null) {
            when (viewState.value.typePay.value) {
                CASH -> updateCash((viewState.value.orderMoney.value + shop.addSum).toInt())
                NO_CASH -> updateNoCash((viewState.value.orderMoney.value + shop.addSum).toInt())
                ANOTHER -> {}
            }
        }
    }

    private fun setAdd() {
        val shop = viewState.value.currentShop
        if (shop != null) {
            when (viewState.value.typePay.value) {
                CASH -> updateCash(shop.addSum.toInt())
                NO_CASH -> updateNoCash(shop.addSum.toInt())
                ANOTHER -> {}
            }
        }
    }

    private fun sumOrderAndArrears() {
        val shop = viewState.value.currentShop
        if (shop != null) {
            when (viewState.value.typePay.value) {
                CASH -> updateCash((viewState.value.orderMoney.value + shop.arrears).toInt())
                NO_CASH -> updateNoCash((viewState.value.orderMoney.value + shop.arrears).toInt())
                ANOTHER -> {}
            }
        }
    }

    private fun setOrder() {
        when (viewState.value.typePay.value) {
            CASH -> updateCash(viewState.value.orderMoney.value.toInt())
            NO_CASH -> updateNoCash(viewState.value.orderMoney.value.toInt())
            ANOTHER -> {}
        }
    }

    private fun setArrears() {

        val shop = viewState.value.currentShop
        if (shop != null) {
            when (viewState.value.typePay.value) {
                CASH -> updateCash(shop.arrears.toInt())
                NO_CASH -> updateNoCash(shop.arrears.toInt())
                ANOTHER -> {}
            }
        }
    }

    private fun deleteRequest(request: RequestModel) {
        val user = sharedViewModel.viewState.value.user.value
        user?.let {
            if (user.isModOrAdminOrSys()) {
                launchCoroutine {
                    room.requestDao().deleteRequest(request)
                    val response = requestApi.delete(
                        id = request.id, idShop = request.idShop, idTrip = request.idTrip
                    )
                    if (response.success) {
                        val shop = viewState.value.currentShop!!
                        val listRequest = shop.listRequest.map { it.copy() } - request
                        shop.listRequest = listRequest
                        val list = viewState.value.listDataRequests.value.map { it.copy() }
                            .toMutableList() - request
                        updateViewState {
                            it.copy(
                                listDataRequests = MutableStateFlow(list), currentShop = shop
                            )
                        }
                        calculateOrder()
                    } else {
                        sharedViewModel.message(response.message, type = TypeMessageModel.ERROR)
                    }
                }
            } else {
                sharedViewModel.message(Constants.ERROR.RESRTRAINT, type = TypeMessageModel.ERROR)
            }
        }
    }

    private fun changeCountRequest(item: RequestModel, count: String) {
        launchCoroutine {
            val shop = viewState.value.currentShop
            val shops = viewState.value.listShop.value.map { it.copy() }.toMutableList()
            val user = sharedViewModel.viewState.value.user.value
            if (shop != null && user != null) {
                val list =
                    viewState.value.listDataRequests.value.map { it.copy() }.toMutableList()
                val index = list.indexOfFirst { it.id == item.id }
                val request = if (count.isNotEmpty()) {
                    item.copy(count = count.trim().toInt(), status = !user.isSysOrAdmin() || item.status == true)
                } else {
                    item.copy(count = 0, status = !user.isSysOrAdmin() || item.status == true)
                }
                list[index] = request
                updateRequest(request)
                shop.listRequest = list
                val existingIndex = shops.indexOfFirst { it.id == shop.id }
                if (existingIndex != -1) {
                    shops[existingIndex] = shop
                }
                updateViewState {
                    it.copy(
                        listDataRequests = MutableStateFlow(list),
                        listShop = MutableStateFlow(shops)
                    )
                }
                calculateOrder()
            }
        }
    }

    private fun changeExchangeRequest(item: RequestModel, exchange: String) {
        launchCoroutine {
            val shop = viewState.value.currentShop
            val shops = viewState.value.listShop.value.map { it.copy() }.toMutableList()
            if (shop != null) {
                val list =
                    viewState.value.listDataRequests.value.map { it.copy() }.toMutableList()
                val index = list.indexOfFirst { it.id == item.id }
                val request = if (exchange.isNotEmpty()) {
                    item.copy(exchange = exchange.trim().toInt())
                } else {
                    item.copy(exchange = 0)
                }
                list[index] = request
                val existingIndex = shops.indexOfFirst { it.id == shop.id }
                shop.listRequest = list
                if (existingIndex != -1) {
                    shops[existingIndex] = shop
                }
                updateRequest(request)
                updateViewState {
                    it.copy(
                        listDataRequests = MutableStateFlow(list),
                        listShop = MutableStateFlow(shops)
                    )
                }
                calculateOrder()
            }
        }
    }

    private fun changeBonusRequest(item: RequestModel, bonus: String) {
        launchCoroutine {
            val shop = viewState.value.currentShop
            val shops = viewState.value.listShop.value.map { it.copy() }.toMutableList()
            val user = sharedViewModel.viewState.value.user.value
            if (shop != null && user != null) {
                val list =
                    viewState.value.listDataRequests.value.map { it.copy() }.toMutableList()
                val index = list.indexOfFirst { it.id == item.id }
                val request = if (bonus.isNotEmpty()) {
                    item.copy(bonus = bonus.trim().toInt(), status = !user.isSysOrAdmin() || item.status == true)
                } else {
                    item.copy(bonus = 0, status = !user.isSysOrAdmin() || item.status == true)
                }
                updateRequest(request)
                list[index] = request
                shop.listRequest = list
                val existingIndex = shops.indexOfFirst { it.id == shop.id }
                if (existingIndex != -1) {
                    shops[existingIndex] = shop
                }
                updateViewState {
                    it.copy(
                        listDataRequests = MutableStateFlow(list),
                        listShop = MutableStateFlow(shops)
                    )
                }
            }
        }
    }

    private fun changeStateDropDownTypePay(state: Boolean) {
        updateViewState { it.copy(isShowDropDownTypePay = state) }
    }


    private fun showTypePayDialog() {
        val currentType = _viewState.value.typePay.value
        val allTypes = TypePayModel.entries
        val currentIndex = allTypes.indexOf(currentType)
        val nextIndex = (currentIndex + 1) % allTypes.size
        val nextType = allTypes[nextIndex]
        updateViewState {
            it.copy(
                typePay = MutableStateFlow(nextType),
                getNoCash = MutableStateFlow(""),
                getCash = MutableStateFlow("")
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
            val trip = viewState.value.currentTrip.value
            if (trip != null) {
                val user = sharedViewModel.viewState.value.user.value
                user?.let {
                    if (user.id == trip.idCourier || user.isSysOrAdmin()) {
                        val shop = viewState.value.currentShop
                        if (shop != null) {
                            val getCash = viewState.value.getCash.value
                            val getNoCash = viewState.value.getNoCash.value
                            val cash = if (getCash.isEmpty()) 0.0 else getCash.toDouble()
                            val noCash = if (getNoCash.isEmpty()) 0.0 else getNoCash.toDouble()
                            val arrear = shop.arrears
                            val addSum = shop.addSum
                            val typePay = viewState.value.typePay.value
                            val order = viewState.value.orderMoney.value
                            val newArrear = (order + arrear + addSum) - (cash + noCash)
                            updateClient(shop.copy(arrears = newArrear))
                            val dataShop = shop.copy(
                                addSum = addSum,
                                cash = cash,
                                noCash = noCash,
                                typePay = typePay,
                                status = true,
                                isOldPrice = viewState.value.stateSwitchPrice.value,
                            )
                            updateViewState { it.copy(currentShop = dataShop) }
                            initShopFunction(event = FunShop.UPDATE)
                            dismissRequestDialog()
                        }
                    } else {
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
        val newShop = viewState.value.currentShop
        launchCoroutine {
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
                val list = viewState.value.listShop.value.map { it.copy() }.toMutableList()
                val index = list.indexOfFirst { it.id == newShop.id }
                list[index] = newShop
                updateViewState { it.copy(listShop = MutableStateFlow(list)) }
                val response = shopApi.update(shopRequest)
                if (!response.success) {
                    sharedViewModel.message(response.message)
                }
                val trip = viewState.value.currentTrip.value
                if (trip != null && isSameDay(trip.date, System.currentTimeMillis())) {
                    updateShops(trip)
                }
            }
        }
    }

    private fun updateRequest(request: RequestModel) {
        val trip = viewState.value.currentTrip.value
        if (trip != null) {
            sharedViewModel.viewState.value.user.value?.let { user ->
                if (user.id == trip.idCourier || user.isSysOrAdmin()) {
                    launchCoroutine {
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
                            room.requestDao().updateRequest(request)
                            val response = requestApi.update(reqResponse)
                            if (!response.success) {
                                sharedViewModel.message(response.message)
                            }
                        }
                    }
                } else {
                    sharedViewModel.message(Constants.ERROR.RESRTRAINT)
                    dismissRequestDialog()
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
                            listInfoShop = MutableStateFlow(list)
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