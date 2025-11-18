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
import ru.krymer.delivery.data.model.ShopModel
import ru.krymer.delivery.data.model.ShopServerModel
import ru.krymer.delivery.data.model.TripModel
import ru.krymer.delivery.data.model.toServerModel
import ru.krymer.delivery.data.model.toUiModel
import ru.krymer.delivery.data.model.utilModel.StatusModel
import ru.krymer.delivery.data.model.utilModel.TypePayModel.CASH
import ru.krymer.delivery.data.model.utilModel.getTypePayByString
import ru.krymer.delivery.data.model.utilModel.toStatusModel
import ru.krymer.delivery.data.model.utilModel.toStr
import ru.krymer.delivery.data.request.ClientRequest
import ru.krymer.delivery.data.request.CreateShopRequest
import ru.krymer.delivery.data.request.RequestShopRequest
import ru.krymer.delivery.data.request.TripRequest
import ru.krymer.delivery.data.request.UpdateShopRequest
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent
import ru.krymer.delivery.ui.screens.shop.models.ShopViewState
import ru.krymer.delivery.utills.Constants
import ru.krymer.delivery.utills.copyToClipboard
import ru.krymer.delivery.utills.isSameDay
import java.io.IOException
import java.net.UnknownHostException
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

enum class RequestUpdateType { COUNT, BONUS, EXCHANGE }

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
            } catch (_: UnknownHostException) {
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

    override fun obtainEvent(event: ShopEvent) {
        when (event) {
            is ShopEvent.ShowAddDialogShopCurrentRoute -> toggleAddDialog(isAll = false)
            is ShopEvent.ShowAddDialogShopAllRoutes -> toggleAddDialog(isAll = true)
            is ShopEvent.SelectClient -> launchCoroutine { changeClient(event.client) }
            is ShopEvent.ChangeCountProduct -> changeRequestCountProduct(
                count = event.count, editProduct = event.product
            )
            is ShopEvent.ChangeCountBonusProduct -> changeRequestCountBonusProduct(
                bonus = event.bonus, editProduct = event.product
            )
            is ShopEvent.ShopAddAction -> initSaveShopWithData()
            is ShopEvent.OpenGeoPoint -> openGeoPoint(event.context, event.cord)
            is ShopEvent.ToggleMillageDialog -> {
                updateViewState { it.copy(toggleMillageDialog = !it.toggleMillageDialog) }
            }
            is ShopEvent.MillageSaveAction -> saveMillage()
            is ShopEvent.ValueChangeMillage -> updateViewState { it.copy(millage = event.millage) }
            is ShopEvent.DismissAddDialog -> updateViewState {
                it.copy(
                    toggleAddDialog = false,
                    currentClient = null,
                    isBonusState = false,
                    toggleDialogAllClients = false,
                    toggleDialogCurrentClients = false
                )
            }

            is ShopEvent.ValueChangeCash -> updateViewState { it.copy(getCash = event.money) }
            is ShopEvent.ValueChangeNoCashMoney -> updateViewState { it.copy(getNoCash = event.money) }

            is ShopEvent.ToggleInfoCurrentShopDialog -> {
                updateViewState { it.copy(toggleCurrentShopInfo = !it.toggleCurrentShopInfo) }
            }
            is ShopEvent.OpenInfoShopDialog -> openInfoShopDialog(event.shop)

            is ShopEvent.RequestAddAction -> initSaveOrUpdateRequest()
            is ShopEvent.CopyInfoData -> copyToClip(event.context)
            is ShopEvent.SwitchBonusState -> updateViewState { it.copy(isBonusState = !viewState.value.isBonusState) }
            ShopEvent.ToggleAnaliticShopsCurrentTrip -> updateViewState {
                it.copy(
                    toggleAnaliticOfTrip = !viewState.value.toggleAnaliticOfTrip
                )
            }

            is ShopEvent.CopyAndSaveShop -> initCopyAndSaveShopWithData()
            is ShopEvent.ToggleLogsShopDialog -> toggleLogsShopDialog(shop = event.shop)
            ShopEvent.ToggleConfirmCopyAndSave -> updateViewState { it.copy(isCopyAndSave = !viewState.value.isCopyAndSave) }
            is ShopEvent.SelectShop -> {
                updateViewState { it.copy(copyShop = event.shop) }
            }

            is ShopEvent.DeleteMessage -> launchCoroutine { deleteMessage(message = event.message) }
            ShopEvent.GetDataRequestsByTrip -> getListRequestsInfo()
        }
    }

    private fun toggleLogsShopDialog(shop: ShopModel?) {
        updateViewState { it.copy(toggleLogShop = !viewState.value.toggleLogShop) }
        if (viewState.value.toggleLogShop) shop?.let { getLogsShop(shop) }
        else updateViewState { it.copy(logShop = emptyList()) }
    }

    private suspend fun deleteMessage(message: MessageModel) {
        messageApi.delete(message.id)
        val list = viewState.value.messages - message
        updateViewState { it.copy(messages = list) }
    }

    private fun getLogsShop(shop: ShopModel) {
        launchCoroutine {
            val logs = loggerApi.getLogsShop(idShop = shop.id, idTrip = shop.idTrip, idFactory = shop.idFactory).obj
            if (logs != null) {
                updateViewState { it.copy(logShop = logs) }
            }
        }
    }

    private fun initSettings() {
        val value = sharedViewModel.viewState.value.lightVersion
        updateViewState { it.copy(lightVersion = value, isSettingsInstall = true) }
    }

    fun initData(trip: TripModel) {
        launchCoroutine {
            initSettings()
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
                            room.tripDao().upsertTrip(tripResponse.copy(isLoaded = true))
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
            getDataForCourier()
            getDataProduct()
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
                var shop = local
                if (shop.statusServer.toStatusModel() == StatusModel.UN_SYNC) {
                    val shopReq = UpdateShopRequest(
                        id = shop.id,
                        idTrip = shop.idTrip,
                        idFactory = shop.idFactory,
                        arrears = shop.arrears,
                        addSum = shop.addSum,
                        status = true,
                        date = shop.date,
                        typePay = shop.typePay,
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
                        shop = shop.copy(status = true, statusServer = StatusModel.SYNC.toStr())
                        room.shopDao().insertShop(shop)
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
                        val reqReq = RequestShopRequest(
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

                val sumOrder = requests.sumOf {
                    it.count * it.price - it.exchange * (if (shop.isOldPrice) it.oldPrice else it.price)
                }
                val arrear = (sumOrder + shop.arrears + shop.addSum) - (shop.cash + shop.noCash)
                updateClient(shop.copy(arrears = arrear))

                getLocalData(trip.id)
            }
        }
    }

    private suspend fun getLocalData(idTrip: Long) {
        val shopsLocal = room.shopDao().getShops(idTrip = idTrip)
        updateViewState {
            it.copy(
                shops = shopsLocal.sortedBy { shop -> shop.counter },
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

    private fun initSaveOrUpdateRequest() {
        val shop = viewState.value.currentShop
        if (shop != null) {
            saveRequest(
                shop = shop
            )
        }
    }

    private suspend fun getAllDataClient() {
        val trip = viewState.value.currentTrip
        val list = viewState.value.shops.map { it.copy() }
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
                    changeClient(client = clients[0])
                }
            } else {
                sharedViewModel.message(response.message)
            }
        }
    }

    private suspend fun getDataClientCurrentRoute() {
        val trip = viewState.value.currentTrip
        val list = viewState.value.shops.map { it.copy() }
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
                    changeClient(client = clients[0])
                }
            } else {
                sharedViewModel.message(response.message)
            }

        }
    }

    private suspend fun getDataProduct() {
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
                    updateViewState { it.copy(shops = emptyList()) }
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

    private suspend fun databaseInit(shops: List<ShopServerModel>, idTrip: Long) {
        room.withTransaction {
            val localShops = room.shopDao().getShops(idTrip = idTrip)
            val localIds = localShops.map { it.id }.toSet()
            val serverIds = shops.map { it.id }.toSet()

            val toDeleteIds = localIds - serverIds


            if (toDeleteIds.isNotEmpty()) {
                room.requestDao().deleteRequestsByShopIds(toDeleteIds.toList())
                room.shopDao().deleteShopsByIds(toDeleteIds.toList())
            }

            val shopsToUpdate = mutableListOf<ShopModel>()
            val shopsToInsert = mutableListOf<ShopModel>()
            val requestsToInsert = mutableListOf<RequestModel>()
            val requestsToUpdate = mutableListOf<RequestModel>()

            for (serverShop in shops) {
                val localShop = localShops.find { it.id == serverShop.id }

                if (localShop != null) {
                    if (serverShop.status) {
                        shopsToUpdate.add(
                            serverShop.toUiModel().copy(
                            statusServer = StatusModel.SYNC.toStr(),
                        ))
                    } else {
                        if (localShop.statusServer.toStatusModel() == StatusModel.NOT_CHANGE) {
                            shopsToUpdate.add(
                                serverShop.toUiModel().copy(
                                    statusServer = StatusModel.NOT_CHANGE.toStr(),
                                )
                            )
                        }
                        if (localShop.statusServer.toStatusModel() == StatusModel.SYNC) {
                            shopsToUpdate.add(
                                serverShop.toUiModel().copy(
                                    statusServer = StatusModel.SYNC.toStr(),
                                )
                            )
                        }
                    }
                } else {
                    shopsToInsert.add(serverShop.toUiModel())
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
            updateViewState { it.copy(shopsAnalitic = shops) }

            getLocalData(idTrip)
        }
    }

    private fun toggleAddDialog(isAll: Boolean) {
        launchCoroutine {
            if (isAll) {
                getAllDataClient()
                updateViewState { it.copy(toggleDialogAllClients = true) }
            } else {
                getDataClientCurrentRoute()
                updateViewState { it.copy(toggleDialogCurrentClients = true) }
            }
            updateViewState { it.copy(toggleAddDialog = true) }
            updateViewState {
                it.copy(
                    listProductRequest = viewState.value.listProduct.filter { p -> p.isActive }
                        .map { p -> p.copy() },
                )
            }
        }
    }

    private suspend fun changeClient(client: ClientModel?) {
        if (client == null) return
        updateViewState { it.copy(currentClient = client) }
        val shops = room.shopDao().getShopsById(id = client.id).map { shop ->
            return@map shop.let {
                val requests =
                    room.requestDao().getRequests(idTrip = shop.idTrip, idShop = shop.id)
                ShopServerModel(
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
                listCurrentShopInfo = shops.sortedByDescending {shop -> shop.date },
                messages = messages
            )
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

    private fun initSaveShopWithData() {
        saveShop(sendShop = {
            saveRequest(
                shop = it
            )
        })
    }

    private fun initCopyAndSaveShopWithData() {
        launchCoroutine {
            obtainEvent(ShopEvent.ToggleConfirmCopyAndSave)
            val shop = viewState.value.copyShop
            if (shop != null) {
                val client = clientApi.getClientById(shop.id).obj
                if (client != null) {
                    saveShop()
                    saveCopyRequest(shop = shop, client = client)
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
                        obtainEvent(ShopEvent.DismissAddDialog)
                        sendShop(shop.copy(statusServer = StatusModel.NOT_CHANGE.toStr()))
                        updateViewState { it.copy(listClient = it.listClient - client) }
                    }
                } else {
                    sharedViewModel.message(message = response.message)
                }
            }
        }
    }

    private fun saveCopyRequest(shop: ShopServerModel, client: ClientModel) {
        launchCoroutine {
            val trip = viewState.value.currentTrip
            trip?.let { trip ->
                val listRequest = shop.listRequest.map { it.copy(exchange = 0, status = false) }
                val hasBonus = listRequest.any { it.bonus > 0 }
                val shops = viewState.value.shops.map { it.copy() }.toMutableList()
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
                shop.isChanged = false
                listRequest.forEach { product ->
                    product.apply {
                        val reqResponse = RequestShopRequest(
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
                    shops[existingIndex] = shop.toUiModel()
                } else {
                    shops.add(shop.toUiModel())
                }
                updateViewState {
                    it.copy(
                        shops = shops.sortedBy { s -> s.counter },
                        listDataRequests = listRequest.sortedBy { req -> req.counter }
                    )
                }
            }
        }
    }

    private fun saveRequest(shop: ShopModel) {
        launchCoroutine {
            val listProduct = viewState.value.listProductRequest.map { it.copy() }
            val listRequest = mutableListOf<RequestModel>()
            listProduct.forEach { product ->
                val reqResponse = RequestShopRequest(
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
            val shops = viewState.value.shops.map { it.copy() }.toMutableList()
            shop.isBonus = hasBonus
            val existingIndex = shops.indexOfFirst { it.id == shop.id }
            if (existingIndex != -1) {
                shops[existingIndex] = shop
            } else {
                shops.add(shop)
            }
            updateViewState {
                it.copy(
                    shops = shops.sortedBy { s -> s.counter },
                    listDataRequests = listRequest.sortedBy { req -> req.counter }
                )
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

    private suspend fun getDataForCourier() {
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
                    }
                } else {
                    sharedViewModel.message(Constants.ERROR.RESRTRAINT)
                    obtainEvent(ShopEvent.ToggleMillageDialog)
                }
            }
        }
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

                        val updatedRequests = requests.map { req ->
                            req.copy(
                                countRemain = req.count + req.bonus,
                                statusServer = ""
                            )
                        }.toMutableList()

                        val shops = room.shopDao().getShops(trip.id)
                        shops.forEach { shop ->
                            if (shop.status) {
                                val localRequests = room.requestDao().getRequests(idShop = shop.id, idTrip = trip.id)

                                localRequests.forEach { req ->
                                    val index = updatedRequests.indexOfFirst { it.id == req.id }
                                    if (index != -1) {
                                        val currentRequest = updatedRequests[index]
                                        val newCountRemain = currentRequest.countRemain - (req.count + req.bonus)
                                        updatedRequests[index] = currentRequest.copy(countRemain = newCountRemain, statusServer = "")
                                    }
                                }
                            }
                        }
                        updateViewState {
                            it.copy(
                                isLoadDataRequestsInfoDialog = true,
                                listInfoRequests = updatedRequests,
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

    private fun openInfoShopDialog(shop: ShopModel) {
        updateViewState { it.copy(currentShop = shop, listCurrentShopInfo = emptyList()) }
        getDataInfoShop(shop)
    }

    private fun getDataInfoShop(curShop: ShopModel) {
        launchCoroutine {
            val localList = room.shopDao().getShopsById(id = curShop.id).map { it.toServerModel() }
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