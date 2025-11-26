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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.krymer.delivery.AppDatabase
import ru.krymer.delivery.data.api.MessageApi
import ru.krymer.delivery.data.model.ClientModel
import ru.krymer.delivery.data.model.MessageModel
import ru.krymer.delivery.data.model.ProductModel
import ru.krymer.delivery.data.model.RequestModel
import ru.krymer.delivery.data.model.ShopModel
import ru.krymer.delivery.data.model.ShopServerModel
import ru.krymer.delivery.data.model.TripModel
import ru.krymer.delivery.data.model.toServerModel
import ru.krymer.delivery.data.model.toUiModel
import ru.krymer.delivery.data.model.user.UserModel
import ru.krymer.delivery.data.model.utilModel.StatusModel
import ru.krymer.delivery.data.model.utilModel.TypeMessageModel
import ru.krymer.delivery.data.model.utilModel.TypePayModel
import ru.krymer.delivery.data.model.utilModel.getTypePayByString
import ru.krymer.delivery.data.model.utilModel.toStatusModel
import ru.krymer.delivery.data.model.utilModel.toStr
import ru.krymer.delivery.data.repositoryImpl.ShopRepositoryImpl
import ru.krymer.delivery.data.request.ClientRequest
import ru.krymer.delivery.data.request.RequestShopRequest
import ru.krymer.delivery.data.request.ShopRequest
import ru.krymer.delivery.data.request.TripRequest
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent.ChangeCountBonusProduct
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent.ChangeCountProduct
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent.ClientsLoaded
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent.CopyAndSaveShop
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent.CopyInfoData
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent.CourierInfoLoaded
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent.DeleteMessage
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent.DismissAddDialog
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent.Error
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent.GetDataRequestsByTrip
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent.InitSettings
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent.Initialize
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent.LoadState
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent.MessageLoaded
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent.MillageSaveAction
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent.OpenGeoPoint
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent.OpenInfoShopDialog
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent.ProductsLoaded
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent.ProductsRequestLoaded
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent.RefreshShops
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent.RequestAddAction
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent.SaveCurrentTrip
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent.SelectClient
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent.SelectShop
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent.ShopAddAction
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent.ShopsAnaliticLoaded
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent.ShopsForCreateShopLoaded
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent.ShopsLoaded
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent.ShowAddDialogShopAllRoutes
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent.ShowAddDialogShopCurrentRoute
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent.ShowLogsShop
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent.SwitchBonusState
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent.ToggleAnaliticShopsCurrentTrip
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent.ToggleConfirmCopyAndSave
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent.ToggleInfoDialogAboutCurrentShop
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent.ToggleLogsShopDialog
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent.ToggleMillageDialog
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent.ValueChangeCash
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent.ValueChangeMillage
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent.ValueChangeNoCashMoney
import ru.krymer.delivery.ui.screens.shop.models.ShopViewState
import ru.krymer.delivery.utills.Constants
import ru.krymer.delivery.utills.MyResult
import ru.krymer.delivery.utills.copyToClipboard
import ru.krymer.delivery.utills.isSameDay
import java.io.IOException
import java.net.UnknownHostException
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

enum class RequestUpdateType { COUNT, BONUS, EXCHANGE }

@HiltViewModel
class ShopViewModel @Inject constructor(
    private val sharedViewModel: SharedViewModel,
    private val room: AppDatabase,
    private val messageApi: MessageApi, private val repository: ShopRepositoryImpl
) : ViewModel() {

    private val _events = MutableSharedFlow<ShopEvent>(extraBufferCapacity = 64)

    private fun getCurrentUser(): UserModel? {
        return sharedViewModel.viewState.value.user
    }

    private suspend fun getCurrentUserOrEmitError(): UserModel? {
        val user = getCurrentUser()
        if (user == null) {
            _events.emit(Error(Constants.ERROR.GENERAL_ERROR))
        }
        return user
    }

    val viewState: StateFlow<ShopViewState> = _events.onStart {
        emit(InitSettings)
        launchCoroutine { getDataProduct() }
    }.runningFold(ShopViewState()) { state, event ->
        when (event) {
            is Initialize -> {
                val trip = event.trip
                launchCoroutine {
                    val shops =
                        room.shopDao().getShops(idTrip = trip.id).sortedBy { shop -> shop.counter }
                    _events.emit(ShopsLoaded(shops = shops))
                    initData(trip = trip)
                }
                state.copy(currentTrip = trip, isLoading = true)
            }

            is ToggleLogsShopDialog -> {
                val shop = event.shop
                if (!state.toggleLogShop) {
                    shop?.let {
                        getLogsCurrentShop(shop = shop)
                    } ?: run {
                        sharedViewModel.message(Constants.ERROR.AGAIN)
                    }
                }
                state.copy(toggleLogShop = !state.toggleLogShop)
            }

            is ShowLogsShop -> {
                val logs = event.logs
                state.copy(logShop = logs)
            }

            InitSettings -> state.copy(
                lightVersion = sharedViewModel.viewState.value.lightVersion,
                isSettingsInstall = true
            )

            is RefreshShops -> {
                val trip = event.trip
                getDataShops(trip = trip)
                state
            }

            is ShopsLoaded -> {
                val shops = event.shops
                state.copy(isLoading = false, shops = shops)
            }

            is ChangeCountBonusProduct -> {
                launchCoroutine {
                    changeRequestBonusProduct(bonus = event.bonus, editProduct = event.product)
                }
                state
            }

            is ChangeCountProduct -> {
                launchCoroutine {
                    changeRequestCountProduct(count = event.count, editProduct = event.product)
                }
                state
            }

            CopyAndSaveShop -> {
                launchCoroutine {
                    initCopyAndSaveShopWithData()
                }
                state
            }

            is CopyInfoData -> {
                copyToClip(context = event.context)
                state
            }

            is DeleteMessage -> {
                deleteMessage(message = event.message)
                state
            }

            DismissAddDialog -> state.copy(
                toggleAddDialog = false,
                currentClient = null,
                isBonusState = false,
                toggleDialogAllClients = false,
                toggleDialogCurrentClients = false
            )

            GetDataRequestsByTrip -> {
                getListRequestsInfo()
                state
            }

            MillageSaveAction -> {
                saveMillage()
                state
            }

            is OpenGeoPoint -> {
                openGeoPoint(context = event.context, cord = event.cord)
                state
            }

            is OpenInfoShopDialog -> {
                getDataInfoShop(event.shop)
                state.copy(currentShop = event.shop, listCurrentShopInfo = emptyList())
            }

            RequestAddAction -> {
                initSaveOrUpdateRequest()
                state
            }

            is SelectClient -> {
                changeClient(client = event.client)
                state.copy(currentClient = event.client)
            }

            is SelectShop -> state.copy(copyShop = event.shop)

            ShopAddAction -> {
                launchCoroutine {
                    initSaveShopWithData()
                }
                state
            }

            ShowAddDialogShopAllRoutes -> {
                _events.emit(ShopEvent.ProductsRequestLoaded(products = state.products.filter { p -> p.isActive }
                    .map { p -> p.copy() }))
                getAllDataClient()
                state.copy(toggleAddDialog = true)
            }

            ShowAddDialogShopCurrentRoute -> {
                _events.emit(ShopEvent.ProductsRequestLoaded(products = state.products.filter { p -> p.isActive }
                    .map { p -> p.copy() }))
                getDataClientCurrentRoute()
                state.copy(toggleAddDialog = true)
            }

            SwitchBonusState -> state.copy(isBonusState = !state.isBonusState)

            ToggleAnaliticShopsCurrentTrip -> state.copy(toggleAnaliticOfTrip = !state.toggleAnaliticOfTrip)

            ToggleConfirmCopyAndSave -> {
                state.copy(isCopyAndSave = !viewState.value.isCopyAndSave)
            }

            ToggleInfoDialogAboutCurrentShop -> {
                state.copy(toggleInfoDialogAboutCurrentShop = !state.toggleInfoDialogAboutCurrentShop)
            }

            ToggleMillageDialog -> {
                state.copy(toggleMillageDialog = !state.toggleMillageDialog)
            }

            is ValueChangeCash -> state.copy(getCash = event.money)

            is ValueChangeMillage -> state.copy(millage = event.millage)

            is ValueChangeNoCashMoney -> state.copy(getNoCash = event.money)

            is Error -> {
                sharedViewModel.message(event.message, type = TypeMessageModel.ERROR)
                state.copy(isLoading = false)
            }

            is ProductsLoaded -> state.copy(products = event.products)

            is CourierInfoLoaded -> {
                val trip = event.trip
                val dataCourier = event.info
                state.copy(
                    millage = trip.millage,
                    noCash = dataCourier.noCash,
                    salary = if (trip.millage > 0.0) dataCourier.salary else 0.0,
                    cash = dataCourier.cash,
                    allMoney = dataCourier.allMoney,
                    remains = if (trip.millage > 0.0) dataCourier.remainCash else 0.0,
                    salaryFix = trip.salary,
                    isDataShopForCourierLoad = true
                )
            }

            is ShopsAnaliticLoaded -> state.copy(shopsAnalitic = event.shops)

            is ClientsLoaded -> state.copy(clients = event.clients)

            is MessageLoaded -> state.copy(messages = event.messages)

            is ShopsForCreateShopLoaded -> state.copy(listCurrentShopInfo = event.shops)

            is SaveCurrentTrip -> {
                state.copy(currentTrip = event.trip)
            }

            is LoadState -> event.state

            is ProductsRequestLoaded -> state.copy(listProductRequest = event.products)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ShopViewState())

    private suspend fun getLogsCurrentShop(shop: ShopModel) {
        when (val response = repository.getLogsCurrentShop(
            idShop = shop.id, idTrip = shop.idTrip, idFactory = shop.idFactory
        )) {
            is MyResult.Error -> _events.emit(Error(message = response.message))
            is MyResult.Success -> {
                val logs = response.data
                if (logs.isNotEmpty()) {
                    _events.emit(ShowLogsShop(logs = logs))
                } else {
                    _events.emit(ShowLogsShop(logs = emptyList()))
                    sharedViewModel.message(Constants.ERROR.LIST_EMPTY)
                }
            }
        }
    }

    fun obtainEvent(event: ShopEvent) {
        _events.tryEmit(event)
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

    private suspend fun getDataProduct() {
        val user = getCurrentUserOrEmitError() ?: return
        when (val response = repository.getProducts(idFactory = user.idFactory)) {
            is MyResult.Error -> _events.emit(Error(message = "Ошибка загрузки товаров!"))
            is MyResult.Success -> {
                val products = response.data
                _events.emit(ProductsLoaded(products = products))
            }
        }
    }

    private suspend fun getDataForCourier(trip: TripModel) {
        when (val response =
            repository.getDataAboutCourier(idTrip = trip.id, idFactory = trip.idFactory)) {
            is MyResult.Error -> _events.emit(Error(message = "Ошибка загрузки информации о курьере!"))
            is MyResult.Success -> {
                val getData = response.data ?: return
                _events.emit(CourierInfoLoaded(getData, trip = trip))
            }
        }
    }

    private suspend fun deleteMessage(message: MessageModel) {
        messageApi.delete(message.id)
        _events.emit(MessageLoaded(viewState.value.messages - message))
    }

    private suspend fun initData(trip: TripModel) {
        val user = getCurrentUserOrEmitError() ?: return
        getDataForCourier(trip = trip)
        val localTrip = room.tripDao().getTripById(tripId = trip.id)
        if (localTrip != null) {
            if (user.id == trip.idCourier && localTrip.isLoaded) {
                if (isSameDay(trip.date, System.currentTimeMillis())) {
                    updateShops(trip = trip)
                    if (localTrip.millage > 0) {
                        val updateTrip = TripRequest(
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
                        )
                        when (val response = repository.updateTrip(trip = updateTrip)) {
                            is MyResult.Error -> _events.emit(Error(message = "Не удалось обновить оффлайн данные рейса!"))
                            is MyResult.Success -> {
                                var newTrip = response.data
                                    ?: return _events.emit(Error(message = "Данные о рейсе не найдены!"))
                                newTrip = newTrip.copy(isLoaded = true)
                                room.tripDao().upsertTrip(newTrip)
                                _events.emit(SaveCurrentTrip(trip = newTrip))
                            }
                        }
                    } else {
                        val updatedTrip = trip.copy(isLoaded = true)
                        _events.emit(SaveCurrentTrip(trip = updatedTrip))
                        getDataShops(trip = trip)
                    }
                } else {
                    val updatedTrip = localTrip.copy(isLoaded = true)
                    _events.emit(SaveCurrentTrip(trip = updatedTrip))
                    getDataShops(trip = trip)
                }
            } else {
                room.tripDao().upsertTrip(trip)
                getDataShops(trip = trip)
                when (val response = repository.getTrip(trip.id)) {
                    is MyResult.Error -> _events.emit(Error(message = "Не удалось обновить рейс!"))
                    is MyResult.Success -> {
                        var newTrip = response.data
                            ?: return _events.emit(Error(message = "Данные о рейсе не найдены!"))
                        newTrip = newTrip.copy(isLoaded = true)
                        room.tripDao().upsertTrip(trip = newTrip)
                        _events.emit(SaveCurrentTrip(trip = newTrip))
                    }
                }
            }
        } else {
            room.tripDao().upsertTrip(trip)
            _events.emit(SaveCurrentTrip(trip = trip))
            getDataShops(trip = trip)
            if (user.id == trip.idCourier) {
                val updatedTrip = trip.copy(isLoaded = true)
                room.tripDao().upsertTrip(updatedTrip)
                _events.emit(SaveCurrentTrip(trip = updatedTrip))
            } else {
                when (val response = repository.getTrip(trip.id)) {
                    is MyResult.Error -> _events.emit(Error(message = "Не удалось обновить рейс!"))
                    is MyResult.Success -> {
                        val newTrip = response.data
                            ?: return _events.emit(Error(message = "Данные о рейсе не найдены!"))
                        room.tripDao().upsertTrip(newTrip)
                        _events.emit(SaveCurrentTrip(trip = newTrip))
                    }
                }
            }
        }
    }

    private suspend fun getDataShops(trip: TripModel) {
        when (val response = repository.getShops(idTrip = trip.id)) {
            is MyResult.Error -> _events.emit(Error(message = response.message))
            is MyResult.Success -> {
                val shops = response.data
                if (shops.isNotEmpty()) {
                    databaseInit(shops, trip)
                } else {
                    clearLocalShopsForTrip(trip.id)
                    _events.emit(ShopsLoaded(shops = emptyList()))
                    sharedViewModel.message(Constants.ERROR.LIST_EMPTY)
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
                var shop = local
                if (shop.statusServer.toStatusModel() == StatusModel.UN_SYNC) {
                    val shopReq = ShopRequest(
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
                    when (repository.update(shop = shopReq)) {
                        is MyResult.Success -> {
                            shop = shop.copy(status = true, statusServer = StatusModel.SYNC.toStr())
                            room.shopDao().insertShop(shop)
                        }

                        is MyResult.Error -> sharedViewModel.message("Ошибка обновления магазина!")
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
                        when (repository.updateRequest(request = reqReq)) {
                            is MyResult.Error -> _events.emit(Error(message = "Ошибка обновления заявки"))
                            is MyResult.Success -> room.requestDao()
                                .upsertRequest(req.copy(statusServer = StatusModel.SYNC.toStr()))
                        }
                    }
                }
                val sumOrder = requests.sumOf {
                    it.count * it.price - it.exchange * (if (shop.isOldPrice) it.oldPrice else it.price)
                }
                val arrear = (sumOrder + shop.arrears + shop.addSum) - (shop.cash + shop.noCash)
                updateClient(shop.copy(arrears = arrear))
            }
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
            when (val response = repository.getClientsByFactory(
                idFactory = trip.idFactory
            )) {
                is MyResult.Error -> _events.emit(Error(message = "Ошибка получения списка клиентов!"))
                is MyResult.Success -> {
                    val clients = response.data.filterNot { p -> list.any { pr -> pr.id == p.id } }
                        .sortedBy { s -> s.name }
                    _events.emit(ClientsLoaded(clients = clients))
                    changeClient(client = clients[0])
                }
            }
        }
    }

    private suspend fun getDataClientCurrentRoute() {
        val trip = viewState.value.currentTrip
        val list = viewState.value.shops.map { it.copy() }
        if (trip != null) {
            when (val response = repository.getClientsByRoute(
                idRoute = trip.idRoute
            )) {
                is MyResult.Error -> _events.emit(Error(message = "Ошибка получения списка клиентов!"))
                is MyResult.Success -> {
                    val clients = response.data.filterNot { p -> list.any { pr -> pr.id == p.id } }
                        .sortedBy { s -> s.counter }
                    _events.emit(ClientsLoaded(clients = clients))
                    changeClient(client = clients[0])
                }
            }
        }
    }


    private suspend fun clearLocalShopsForTrip(idTrip: Long) {
        room.withTransaction {
            room.requestDao().deleteRequestsByTrip(idTrip)
            room.shopDao().deleteShopsByTrip(idTrip)
        }
    }

    private suspend fun databaseInit(shops: List<ShopServerModel>, trip: TripModel) {
        room.withTransaction {
            var localShops = room.shopDao().getShops(idTrip = trip.id)
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
            localShops = room.shopDao().getShops(idTrip = trip.id).sortedBy { shop -> shop.counter }
            _events.emit(ShopsLoaded(shops = localShops))
            _events.emit(ShopsAnaliticLoaded(shops = shops))
        }
    }

    private suspend fun changeClient(client: ClientModel?) {
        if (client == null) return
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
        }.sortedByDescending { shop -> shop.date }
        val messages = getMessages(client.id)
        _events.emit(MessageLoaded(messages = messages))
        _events.emit(ShopsForCreateShopLoaded(shops = shops))

    }

    private suspend fun getMessages(id: Long): List<MessageModel> {
        val messages = messageApi.getMessages(idClient = id).obj
        return messages ?: emptyList()
    }

    private suspend fun updateProductRequest(
        editProduct: ProductModel,
        transform: (ProductModel) -> ProductModel
    ) {
        val list = viewState.value.listProductRequest.toMutableList()
        val index = list.indexOfFirst { it.id == editProduct.id }
        if (index == -1) return
        list[index] = transform(editProduct)
        _events.emit(ShopEvent.ProductsRequestLoaded(list))
    }

    private suspend fun changeRequestCountProduct(count: String, editProduct: ProductModel) =
        updateProductRequest(editProduct) { it.copy(count = count.trim().toIntOrNull() ?: 0) }

    private suspend fun changeRequestBonusProduct(bonus: String, editProduct: ProductModel) =
        updateProductRequest(editProduct) { it.copy(addCount = bonus.trim().toIntOrNull() ?: 0) }

    private suspend fun initSaveShopWithData() {
        saveShop(sendShop = {
            saveRequest(
                shop = it
            )
        })
    }

    private suspend fun initCopyAndSaveShopWithData() {
        _events.emit(ToggleConfirmCopyAndSave)
        val shop = viewState.value.copyShop
        if (shop != null) {
            when (val response = repository.getClientById(shop.id)) {
                is MyResult.Error -> _events.emit(Error(message = "Ошибка получения клиента!"))
                is MyResult.Success -> {
                    val client = response.data ?: return
                    saveShop()
                    saveCopyRequest(shop = shop, client = client)
                }
            }
        }
    }

    private suspend fun saveShop(sendShop: (ShopModel) -> Unit = {}) {
        val client = viewState.value.currentClient
        val trip = viewState.value.currentTrip
        if (client != null && trip != null) {
            val shopRequest = ShopRequest(
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
            when (val response = repository.create(shop = shopRequest)) {
                is MyResult.Error -> _events.emit(Error(message = "Ошибка создания магазина!"))
                is MyResult.Success -> {
                    val shop = response.data ?: return
                    shop.let {
                        obtainEvent(DismissAddDialog)
                        sendShop(shop.copy(statusServer = StatusModel.NOT_CHANGE.toStr()))
                        _events.emit(ClientsLoaded(clients = viewState.value.clients - client))
                    }
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
                shop.typePay = TypePayModel.CASH
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
                        when (repository.createRequest(reqResponse)) {
                            is MyResult.Error -> _events.emit(Error(message = "Ошибка создания заявки!"))
                            is MyResult.Success -> {
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
                }
                val existingIndex = shops.indexOfFirst { it.id == shop.id }
                if (existingIndex != -1) {
                    shops[existingIndex] = shop.toUiModel()
                } else {
                    shops.add(shop.toUiModel())
                }
                _events.emit(ShopsLoaded(shops = shops.sortedBy { s -> s.counter }))
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
                when (repository.createRequest(request = reqResponse)) {
                    is MyResult.Error -> _events.emit(Error(message = "Ошибка создания заявки!"))
                    is MyResult.Success -> {
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
            _events.emit(ShopsLoaded(shops = shops.sortedBy { s -> s.counter }))
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


    private suspend fun saveMillage() {
        val millage = viewState.value.millage
        val trip = viewState.value.currentTrip
        val user = getCurrentUserOrEmitError()
        if (trip != null && user != null) {
            if (user.id == trip.idCourier || user.isModOrAdminOrSys()) {
                val newTrip = trip.copy(millage = millage)
                _events.emit(SaveCurrentTrip(trip = newTrip))
                room.tripDao().upsertTrip(newTrip)
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
                when (repository.updateTrip(trip = request)) {
                    is MyResult.Error -> _events.emit(Error(message = "Ошибка сохранения пробега в рейсе!"))
                    is MyResult.Success -> {
                        getDataForCourier(trip = newTrip)
                    }
                }
            } else {
                sharedViewModel.message(Constants.ERROR.RESRTRAINT)
                obtainEvent(ToggleMillageDialog)
            }
        }
    }

    private suspend fun getListRequestsInfo() {
        val trip = viewState.value.currentTrip
        if (trip != null) {
            when (val response = repository.getRequestsByTrip(
                idTrip = trip.id, idFactory = trip.idFactory
            )) {
                is MyResult.Error -> _events.emit(Error(message = "Ошибка получения списка заявок по рейсу!"))
                is MyResult.Success -> {
                    val requests = response.data.sortedBy { it.price }
                    var count = 0
                    var exchange = 0
                    requests.forEach {
                        count += it.count
                        count += it.bonus
                        exchange += it.exchange
                    }

                    val updatedRequests = requests.map { req ->
                        req.copy(
                            countRemain = req.count + req.bonus, statusServer = ""
                        )
                    }.toMutableList()

                    val shops = room.shopDao().getShops(trip.id)
                    shops.forEach { shop ->
                        if (shop.status) {
                            val localRequests =
                                room.requestDao().getRequests(idShop = shop.id, idTrip = trip.id)

                            localRequests.forEach { req ->
                                val index = updatedRequests.indexOfFirst { it.id == req.id }
                                if (index != -1) {
                                    val currentRequest = updatedRequests[index]
                                    val newCountRemain =
                                        currentRequest.countRemain - (req.count + req.bonus)
                                    updatedRequests[index] = currentRequest.copy(
                                        countRemain = newCountRemain, statusServer = ""
                                    )
                                }
                            }
                        }
                    }
                    val newState = viewState.value.copy(
                        isLoadDataRequestsInfoDialog = true,
                        listInfoRequests = updatedRequests,
                        allCountRequestsInfo = count,
                        allExchangeRequestsInfo = exchange
                    )
                    _events.emit(ShopEvent.LoadState(state = newState))
                }
            }
        }
    }

    private suspend fun updateClient(shop: ShopModel) {
        shop.apply {
            when (val response = repository.getClientById(id = id)) {
                is MyResult.Error -> _events.emit(Error(message = "Ошибка получения клиента!"))
                is MyResult.Success -> {
                    val client = response.data
                    if (client != null) {
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
                        when (repository.updateClient(request = request)) {
                            is MyResult.Error -> _events.emit(Error(message = "Ошибка обновления долга клиента!"))
                            is MyResult.Success -> {}
                        }
                    }


                }
            }
        }
    }

    private fun getDataInfoShop(curShop: ShopModel) {
        launchCoroutine {
            val localList = room.shopDao().getShopsById(id = curShop.id).map { it.toServerModel() }
                .sortedByDescending { it.date }
            val request =
                room.requestDao().getRequests(idShop = curShop.id, idTrip = curShop.idTrip)
            _events.emit(ShopsForCreateShopLoaded(shops = localList.map { it.copy(listRequest = request) }))
            when (val response = repository.getCurrentShopsByFactory(
                id = curShop.id,
                idFactory = curShop.idFactory
            )) {
                is MyResult.Error -> _events.emit(Error(message = "Ошибка получения магазинов!"))
                is MyResult.Success -> {
                    val list = response.data
                    _events.emit(ShopsForCreateShopLoaded(shops = list))
                }
            }
        }
    }
}