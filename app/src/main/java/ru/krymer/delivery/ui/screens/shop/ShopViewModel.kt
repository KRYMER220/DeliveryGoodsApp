package ru.krymer.delivery.ui.screens.shop

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.krymer.delivery.common.EventHandler
import ru.krymer.delivery.data.api.ClientApi
import ru.krymer.delivery.data.api.LoggerApi
import ru.krymer.delivery.data.api.ProductApi
import ru.krymer.delivery.data.api.RequestApi
import ru.krymer.delivery.data.api.ShopApi
import ru.krymer.delivery.data.api.TripApi
import ru.krymer.delivery.data.model.ClientModel
import ru.krymer.delivery.data.model.ProductModel
import ru.krymer.delivery.data.model.RequestModel
import ru.krymer.delivery.data.model.ShopModel
import ru.krymer.delivery.data.model.utilModel.TypePayModel
import ru.krymer.delivery.data.model.utilModel.getStringByTypePay
import ru.krymer.delivery.data.request.ClientRequest
import ru.krymer.delivery.data.request.CreateRequestShopRequest
import ru.krymer.delivery.data.request.CreateShopRequest
import ru.krymer.delivery.data.request.LogRequest
import ru.krymer.delivery.data.request.UpdateRequestShopRequest
import ru.krymer.delivery.data.request.UpdateShopRequest
import ru.krymer.delivery.data.request.UpdateTripRequest
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import ru.krymer.delivery.ui.screens.shop.models.ShopAction
import ru.krymer.delivery.ui.screens.shop.models.ShopEvent
import ru.krymer.delivery.ui.screens.shop.models.ShopViewState
import ru.krymer.delivery.utills.Constants
import ru.krymer.delivery.utills.convertToTextDate
import ru.krymer.delivery.utills.copyToClipboard
import ru.krymer.delivery.utills.findChangedFields
import javax.inject.Inject

@HiltViewModel
class ShopViewModel @Inject constructor(
    private val shopApi: ShopApi,
    private val productApi: ProductApi,
    private val sharedViewModel: SharedViewModel,
    private val clientApi: ClientApi,
    private val tripApi: TripApi,
    private val requestApi: RequestApi,
    private val loggerApi: LoggerApi
) : ViewModel(), EventHandler<ShopEvent> {

    private val _viewState = MutableStateFlow(ShopViewState())
    val viewState: StateFlow<ShopViewState> = _viewState

    private fun updateViewState(update: (ShopViewState) -> ShopViewState) {
        _viewState.update { update(it) }
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

            is ShopEvent.ShopAddAction -> saveShop()
            is ShopEvent.ShowDeleteDialog -> showDeleteDialog(event.shop)
            is ShopEvent.DismissDeleteDialog -> dismissDeleteDialog()
            is ShopEvent.DeleteAction -> deleteShop()
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
            is ShopEvent.ChangeTypePay -> changeTypePay(event.type)
            is ShopEvent.DismissChangeTypePayDialog -> dismissTypePayDialog()
            is ShopEvent.ShowChangeTypePayDialog -> showTypePayDialog()
            is ShopEvent.ChangeDropDownStateTypePayChanger -> changeStateDropDownTypePay(event.state)
            is ShopEvent.ValueChangeNoCashMoney -> changeNoCashMoney(event.money)
            is ShopEvent.DeleteRequest -> deleteRequest(event.request)
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
            is ShopEvent.DismissInfoBlockDialog -> dismissBlockDialog()

            is ShopEvent.DismissDialogAddRequest -> dismissAddRequestDialog()
            is ShopEvent.RequestAddAction -> initSaveOrUpdateRequest()
            is ShopEvent.ShowDialogAddRequest -> showAddRequestDialog()
            is ShopEvent.SwitchPrice -> switchStatePrice()
            is ShopEvent.CopyInfoData -> copyToClip(event.context)
            is ShopEvent.OnAdminManaged -> changeManage()
            is ShopEvent.ChangeCountExchange -> changeRequestExchangeProduct(editProduct = event.product, exchange = event.exchange)
            is ShopEvent.ChangeAdd -> changeAdd(event.add)
            is ShopEvent.ChangeArrear -> changeArrear(arrear = event.arrear)
            is ShopEvent.ChangeDept -> changeDept(dept = event.dept)
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

    private fun changeRequestExchangeProduct(exchange: String, editProduct: ProductModel) {
        val list = viewState.value.listProductRequest.value.map { it.copy() }.toMutableList()
        val index = list.indexOfFirst { it.id == editProduct.id }
        val product = if (exchange.isNotEmpty()) {
            editProduct.copy(exchange = exchange.trim().toInt())
        } else {
            editProduct.copy(exchange = 0)
        }
        list[index] = product
        updateViewState {
            it.copy(
                listProductRequest = MutableStateFlow(list)
            )
        }
    }

    private fun changeManage() {
        if (sharedViewModel.initSysAdm()) {
            updateViewState { it.copy(isAdminServices = !viewState.value.isAdminServices) }
        }
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
        val isOldPrice = viewState.value.stateSwitchPrice.value
        updateViewState { it.copy(stateSwitchPrice = MutableStateFlow(!isOldPrice)) }
        calculateOrder()
    }

    private fun dismissAddRequestDialog() {
        updateViewState { it.copy(showDialogAddRequest = false) }
    }

    private fun showAddRequestDialog() {
        val list = viewState.value.listDataRequests.value.map { it.copy() }
        updateViewState {
            it.copy(
                showDialogAddRequest = true,
                listProductRequest = MutableStateFlow(viewState.value.listProduct.value
                    .filter { p -> p.isActive }
                    .filterNot { p -> list.any { pr -> pr.id == p.id } }
                    .map { p -> p.copy() })
            )
        }
    }

    init {
        getDataShops()
        getDataProduct()
    }

    private fun initSaveOrUpdateRequest() {
        try {
            val shop = viewState.value.currentShop
            if (shop != null) {
                saveRequest(
                    idShop = shop.id, idTrip = shop.idTrip, idFactory = shop.idFactory
                )
            } else {
                sharedViewModel.message(Constants.ERROR.GENERAL_ERROR)
            }
        } catch (e: Exception) {
            sharedViewModel.message(message = e.message)
        } finally {
            dismissAddRequestDialog()
        }
    }

    private fun getAllDataClient() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val trip = sharedViewModel.viewState.value.currentTrip
                val list = viewState.value.listShop.value.map { it.copy() }
                if (trip != null) {
                    val response = clientApi.getAllCurrentListClient(
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
                                    isLoadDataClients = true,
                                    currentClient = clients[0]
                                )
                            }
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
                val trip = sharedViewModel.viewState.value.currentTrip
                val list = viewState.value.listShop.value.map { it.copy() }
                if (trip != null) {
                    val response = clientApi.getCurrentListClient(
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
                                    isLoadDataClients = true,
                                    currentClient = clients[0]
                                )
                            }
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
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val user = sharedViewModel.viewState.value.user
                if (user != null) {
                    val response = productApi.getCurrentListProduct(idFactory = user.idFactory)
                    if (response.success) {
                        val products = response.obj?.sortedBy { it.price }
                        if (products != null) {
                            updateViewState {
                                it.copy(
                                    listProduct = MutableStateFlow(products),
                                    isLoadDataProducts = true
                                )
                            }
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

    private fun getDataShops() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val factory = sharedViewModel.viewState.value.factory
                val trip = sharedViewModel.viewState.value.currentTrip
                if (factory != null && trip != null) {
                    val response = shopApi.getCurrentShops(idTrip = trip.id)
                    if (response.success) {
                        val shops = response.obj?.sortedBy { it.counter }
                        if (shops != null) {
                            val list = shops.sortedBy { s -> s.counter }
                            updateViewState {
                                it.copy(
                                    listShop = MutableStateFlow(list),
                                    isLoadShopsData = true,
                                    currentTrip = trip
                                )
                            }
                        }
                    } else {
                        sharedViewModel.message(response.message)
                    }
                } else {
                    sharedViewModel.message(Constants.ERROR.ERROR)
                }
            } catch (e: Exception) {
                sharedViewModel.message(e.message)
            }
        }
    }

    private fun openAddDialogCurrentClients() {
        getDataClientCurrentRoute()
        updateViewState {
            it.copy(
                isShowDialogWithListCurrentClients = true,
                stateAddDialog = true,
                listProductRequest = MutableStateFlow(viewState.value.listProduct.value.filter { p -> p.isActive }
                    .map { p -> p.copy() })
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
                    .map { p -> p.copy() })
            )
        }
    }

    private fun changeCurrentClient(client: ClientModel) {
        updateViewState { it.copy(currentClient = client) }
    }

    private fun dismissAddDialog() {
        updateViewState {
            it.copy(
                stateAddDialog = false,
                isShowDialogWithListCurrentClients = false,
                isShowDialogWithListAllClients = false,
                isAdminServices = false,
                currentClient = null,
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

    private fun saveShop() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val client = viewState.value.currentClient
                val trip = sharedViewModel.viewState.value.currentTrip
                val factory = sharedViewModel.viewState.value.factory
                if (client != null && trip != null && factory != null) {
                    val shopRequest = CreateShopRequest(
                        id = client.id,
                        idTrip = trip.id,
                        idFactory = factory.id,
                        arrears = if (viewState.value.isAdminServices) viewState.value.arrear.value else client.arrears,
                        date = trip.date,
                        counter = client.counter,
                        isOldPrice = false,
                        nameShop = client.name,
                        cord = client.cord,
                        addSum = viewState.value.add.value,
                        cash = viewState.value.dept.value,
                        status = if (viewState.value.isAdminServices) true else false
                    )
                    val response = shopApi.addShop(shop = shopRequest)
                    if (response.success) {
                        val shop = response.obj
                        if (!sharedViewModel.initSysAdm()) {
                            loggerApi.addLog(
                                log = LogRequest(
                                    idFactory = sharedViewModel.viewState.value.factory?.id!!,
                                    log = "Пользователь: ${sharedViewModel.viewState.value.user?.name},\nуспешно добавил магазин:${client.name},\nтекущий долг: ${client.arrears},\nв рейс: ${trip.nameRoute}, ${
                                        convertToTextDate(
                                            trip.date
                                        )
                                    }",
                                    date = System.currentTimeMillis()
                                )
                            )
                        }
                        if (shop != null) {
                            val list =
                                viewState.value.listShop.value.map { it.copy() }.toMutableList()
                            list.add(shop)
                            updateViewState { it.copy(listShop = MutableStateFlow(list.sortedBy { s -> s.counter })) }
                            saveRequest(
                                idShop = shop.id, idTrip = shop.idTrip, idFactory = shop.idFactory
                            )
                        }
                    } else {
                        sharedViewModel.message(message = response.message)
                    }
                }
            } catch (e: Exception) {
                sharedViewModel.message(message = e.message)
            } finally {
                dismissAddDialog()
            }
        }
    }

    private fun saveRequest(idTrip: Long, idFactory: Long, idShop: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val listProduct = viewState.value.listProductRequest.value.map { it.copy() }
                val listRequest =
                    viewState.value.listDataRequests.value.map { it.copy() }.toMutableList()
                var logStr = ""
                listProduct.forEach { product ->
                    if ((product.count == 0 && product.addCount > 0) || (product.count > 0 && product.addCount == 0) || ((product.count > 0 && product.addCount > 0))) {
                        val reqResponse = CreateRequestShopRequest(
                            id = product.id,
                            idShop = idShop,
                            idTrip = idTrip,
                            idFactory = idFactory,
                            count = product.count,
                            bonus = product.addCount,
                            status = false,
                            exchange = product.exchange ?: 0,
                            price = product.price,
                            oldPrice = product.oldPrice,
                            name = product.name,
                            counter = product.counter
                        )
                        val response = requestApi.addRequest(reqResponse)
                        if (response.success) {
                            val requestModel = RequestModel(
                                id = product.id,
                                idShop = idShop,
                                idTrip = idTrip,
                                idFactory = idFactory,
                                count = product.count,
                                bonus = product.addCount,
                                status = false,
                                exchange = product.exchange ?: 0,
                                price = product.price,
                                oldPrice = product.oldPrice,
                                name = product.name,
                                counter = product.counter
                            )
                            logStr += "${product.name}, заявка: ${product.count}, бонус: ${product.addCount}\n"
                            listRequest.add(requestModel)
                        }
                    }
                }
                loggerApi.addLog(
                    log = LogRequest(
                        idFactory = sharedViewModel.viewState.value.factory?.id!!,
                        log = "Пользователь: ${sharedViewModel.viewState.value.user?.name}, успешно добавил заявки: \n$logStr",
                        date = System.currentTimeMillis()
                    )
                )
                updateViewState { it.copy(listDataRequests = MutableStateFlow(listRequest.sortedBy { r -> r.price })) }
                calculateOrder()
            } catch (e: Exception) {
                sharedViewModel.message(message = e.message)
            }
        }
    }

    private fun showDeleteDialog(shop: ShopModel) {
        updateViewState {
            it.copy(
                showDeleteDialog = true, shopDeleted = shop, itemNameToDelete = shop.nameShop
            )
        }
    }

    private fun deleteShop() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val shop = viewState.value.shopDeleted
                if (shop != null) {
                    val response = shopApi.deleteShop(idShop = shop.id, idTrip = shop.idTrip)
                    if (response.success) {
                        val list = viewState.value.listShop.value.map { it.copy() }.toMutableList()
                        val item = list.first { it.id == shop.id }
                        val listNew = (list - item).sortedBy { it.counter }
                        updateViewState { it.copy(listShop = MutableStateFlow(listNew)) }
                    } else {
                        sharedViewModel.message(response.message)
                    }
                } else {
                    sharedViewModel.message(Constants.ERROR.GENERAL_ERROR)
                }
            } catch (e: Exception) {
                sharedViewModel.message(e.message)
            } finally {
                dismissDeleteDialog()
            }
        }
    }

    private fun dismissDeleteDialog() {
        updateViewState {
            it.copy(
                showDeleteDialog = false, shopDeleted = null, itemNameToDelete = ""
            )
        }
    }

    private fun openGeoPoint(context: Context, cord: String) {
        if (cord.isNotEmpty()) {
            try {
                val cords = cord.split(",")
                if (cords[0].isNotEmpty() && cords[1].isNotEmpty()) {
                    val url =
                        "https://yandex.ru/maps/?ll=${cords[1]},${cords[0]}&z=12&pt=${cords[1]},${cords[0]},pm2"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                }
            } catch (e: Exception) {
                sharedViewModel.message(e.message)
            }
        } else {
            sharedViewModel.message(Constants.ERROR.MISSING_CORDS)
        }
    }

    private fun shopActionInvoked() {
        updateViewState { it.copy(shopAction = ShopAction.None) }
    }

    private fun openRequest(shop: ShopModel) {
        updateViewState {
            it.copy(
                currentShop = shop,
                showRequestDialog = true,
                typePay = MutableStateFlow(shop.typePay),
                getCash = MutableStateFlow(shop.cash.toString()),
                getNoCash = MutableStateFlow(shop.noCash.toString()),
                stateSwitchPrice = MutableStateFlow(shop.isOldPrice)
            )
        }
        loadRequest(shop = shop)
    }

    private fun loadRequest(shop: ShopModel) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = requestApi.getCurrentShopRequests(
                    idTrip = shop.idTrip, idShop = shop.id, idFactory = shop.idFactory

                )
                if (response.success) {
                    val requests = response.obj
                    if (!requests.isNullOrEmpty()) {
                        updateViewState {
                            it.copy(
                                listDataRequests = MutableStateFlow(requests.sortedBy { r -> r.price })
                            )
                        }
                        calculateOrder()
                    } else {
                        sharedViewModel.message(Constants.EMPTY.EMPTY_LIST)
                    }
                } else {
                    sharedViewModel.message(response.message)
                }
            } catch (e: Exception) {
                sharedViewModel.message(e.message)
            }
        }
    }

    private fun showDialogMillage() {
        updateViewState { it.copy(isShowMillageDialog = true) }
        getDataForCourier()
    }

    private fun getDataForCourier() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val factory = sharedViewModel.viewState.value.factory
                val trip = sharedViewModel.viewState.value.currentTrip
                if (factory != null && trip != null) {
                    val response =
                        shopApi.getDataForCourier(idTrip = trip.id, idFactory = factory.id)
                    if (response.success) {
                        val getData = response.obj
                        if (getData != null) {
                            updateViewState {
                                it.copy(
                                    noCash = MutableStateFlow(getData.noCash),
                                    salary = if (trip.millage > 0.0) MutableStateFlow(getData.salary) else MutableStateFlow(
                                        0.0
                                    ),
                                    isDataShopForCourierLoad = true,
                                    cash = MutableStateFlow(getData.cash),
                                    allMoney = MutableStateFlow(getData.allMoney),
                                    remains = if (trip.millage > 0.0) MutableStateFlow(getData.remainCash) else MutableStateFlow(
                                        0.0
                                    ),
                                )
                            }
                        }
                    } else {
                        sharedViewModel.message(response.message)
                    }
                }
            } catch (e: Exception) {
                sharedViewModel.message(message = e.message)
            }
        }
    }

    private fun dismissMillageDialog() {
        updateViewState { it.copy(isShowMillageDialog = false) }
    }


    private fun saveMillage() {
        viewModelScope.launch(Dispatchers.IO) {
            try {

                val millage = viewState.value.millage.value
                val trip = viewState.value.currentTrip

                if (trip != null) {
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

                    val response = tripApi.updateTrip(request)
                    if (response.success) {
                        val updatedShop = response.obj
                        if (updatedShop != null) {
                            loggerApi.addLog(
                                log = LogRequest(
                                    idFactory = sharedViewModel.viewState.value.factory?.id!!,
                                    log = "Пользователь: ${sharedViewModel.viewState.value.user?.name},\n" +
                                            "успешно обновил рейс: ${viewState.value.currentTrip?.nameRoute} " +
                                            "${convertToTextDate(viewState.value.currentTrip?.date!!)}\n" +
                                            findChangedFields(trip, updatedShop),
                                    date = System.currentTimeMillis()
                                )
                            )
                            sharedViewModel.initCurrentTrip(tripModel = updatedShop)
                            updateViewState { it.copy(currentTrip = updatedShop) }
                            getDataForCourier()
                        }
                    } else {
                        sharedViewModel.message(response.message)
                    }
                }
            } catch (e: Exception) {
                sharedViewModel.message(message = e.message)
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
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val trip = sharedViewModel.viewState.value.currentTrip
                if (trip != null) {
                    val response = requestApi.getCurrentTripRequests(
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
            } catch (e: Exception) {
                sharedViewModel.message(message = e.message)
            }
        }
    }

    private fun dismissInfoDialog() {
        updateViewState { it.copy(stateInfoDialog = false) }
    }

    private fun changeArrears(arrears: String) {
        val shop = viewState.value.currentShop
        if (shop != null) {
            val newShop = shop.copy(arrears = if (arrears.isNotEmpty()) arrears.toDouble() else 0.0)
            updateViewState { it.copy(currentShop = newShop) }
        }
    }

    private fun showChangeArrearsDialog() {
        updateViewState { it.copy(isShowDialogArrears = true) }
    }

    private fun openAddSumDialog() {
        updateViewState { it.copy(isShowAddSumDialog = true) }
    }

    private fun changeAddSum(add: String) {
        val shop = viewState.value.currentShop
        if (shop != null) {
            val newShop = shop.copy(addSum = if (add.isNotEmpty()) add.toDouble() else 0.0)
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
            updateViewState { it.copy(getCash = MutableStateFlow("${(viewState.value.orderMoney.value + shop.arrears + shop.addSum).toInt()}")) }
        }
    }

    private fun setArrearsAndAdd() {
        val shop = viewState.value.currentShop
        if (shop != null) {
            updateViewState { it.copy(getCash = MutableStateFlow("${(shop.arrears + shop.addSum).toInt()}")) }
        }
    }

    private fun setOrderAndAdd() {
        val shop = viewState.value.currentShop
        if (shop != null) {
            updateViewState { it.copy(getCash = MutableStateFlow("${(viewState.value.orderMoney.value + shop.addSum).toInt()}")) }
        }
    }

    private fun setAdd() {
        val shop = viewState.value.currentShop
        if (shop != null) {
            updateViewState { it.copy(getCash = MutableStateFlow("${(shop.addSum).toInt()}")) }
        }
    }

    private fun sumOrderAndArrears() {
        val shop = viewState.value.currentShop
        if (shop != null) {
            updateViewState { it.copy(getCash = MutableStateFlow("${(viewState.value.orderMoney.value + shop.arrears).toInt()}")) }
        }
    }

    private fun setOrder() {
        updateViewState { it.copy(getCash = MutableStateFlow("${(viewState.value.orderMoney.value).toInt()}")) }
    }

    private fun setArrears() {
        val shop = viewState.value.currentShop
        if (shop != null) updateViewState { it.copy(getCash = MutableStateFlow("${shop.arrears.toInt()}")) }
    }

    private fun deleteRequest(request: RequestModel) {
        if (sharedViewModel.initSysAdm()) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val response = requestApi.deleteRequest(
                        id = request.id, idShop = request.idShop, idTrip = request.idTrip
                    )
                    if (response.success) {
                        if (!sharedViewModel.initSysAdm()) {
                            loggerApi.addLog(
                                log = LogRequest(
                                    idFactory = sharedViewModel.viewState.value.factory?.id!!,
                                    log = "Пользователь: ${sharedViewModel.viewState.value.user?.name}" +
                                            ", успешно удалил заявку: \n${request.name}" +
                                            "\nв магазине: ${viewState.value.currentShop?.nameShop}\n" +
                                            "в рейсе: ${viewState.value.currentTrip?.nameRoute}\n" +
                                            convertToTextDate(viewState.value.currentTrip?.date!!),
                                    date = System.currentTimeMillis()
                                )
                            )
                        }
                        val list = viewState.value.listDataRequests.value.map { it.copy() }
                            .toMutableList() - request
                        updateViewState {
                            it.copy(
                                listDataRequests = MutableStateFlow(list)
                            )
                        }
                        calculateOrder()
                    } else {
                        sharedViewModel.message(response.message)
                    }
                } catch (e: Exception) {
                    sharedViewModel.message(e.message)
                }
            }
        }
    }

    private fun changeCountRequest(item: RequestModel, count: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val list = viewState.value.listDataRequests.value.map { it.copy() }.toMutableList()
                val index = list.indexOfFirst { it.id == item.id }
                val request = if (count.isNotEmpty()) {
                    item.copy(count = count.trim().toInt())
                } else {
                    item.copy(count = 0)
                }
                list[index] = request
                updateViewState {
                    it.copy(
                        listDataRequests = MutableStateFlow(list)
                    )
                }
                calculateOrder()
                updateRequest(request, item)
            } catch (e: Exception) {
                sharedViewModel.message(e.message)
            }
        }
    }

    private fun changeExchangeRequest(item: RequestModel, exchange: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val list = viewState.value.listDataRequests.value.map { it.copy() }.toMutableList()
                val index = list.indexOfFirst { it.id == item.id }
                val request = if (exchange.isNotEmpty()) {
                    item.copy(exchange = exchange.trim().toInt())
                } else {
                    item.copy(exchange = 0)
                }
                list[index] = request
                updateViewState {
                    it.copy(
                        listDataRequests = MutableStateFlow(list)
                    )
                }
                calculateOrder()
                updateRequest(request, item)
            } catch (e: Exception) {
                sharedViewModel.message(e.message)
            }
        }

    }

    private fun changeBonusRequest(item: RequestModel, bonus: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val list = viewState.value.listDataRequests.value.map { it.copy() }.toMutableList()
                val index = list.indexOfFirst { it.id == item.id }
                val request = if (bonus.isNotEmpty()) {
                    item.copy(bonus = bonus.trim().toInt())
                } else {
                    item.copy(bonus = 0)
                }
                list[index] = request
                updateViewState {
                    it.copy(
                        listDataRequests = MutableStateFlow(list)
                    )
                }
                updateRequest(request, item)
            } catch (e: Exception) {
                sharedViewModel.message(e.message)
            }
        }

    }


    private fun changeStateDropDownTypePay(state: Boolean) {
        updateViewState { it.copy(isShowDropDownTypePay = state) }
    }

    private fun dismissTypePayDialog() {
        updateViewState { it.copy(isShowTypePayChangeDialog = false) }
    }

    private fun showTypePayDialog() {
        updateViewState { it.copy(isShowTypePayChangeDialog = true) }
    }

    private fun changeTypePay(type: TypePayModel) {
        updateViewState {
            it.copy(
                typePay = MutableStateFlow(type),
                getNoCash = MutableStateFlow(""),
                getCash = MutableStateFlow("")
            )
        }
        dismissTypePayDialog()
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
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val trip = sharedViewModel.viewState.value.currentTrip
                if (trip != null) {
                    val shop = viewState.value.currentShop
                    if (shop != null) {
                        val getCash = viewState.value.getCash.value
                        val getNoCash = viewState.value.getNoCash.value
                        val cash = if (getCash.isEmpty()) 0.0 else getCash.toDouble()
                        val noCash = if (getNoCash.isEmpty()) 0.0 else getNoCash.toDouble()
                        val order = viewState.value.orderMoney.value
                        val arrear = shop.arrears
                        val addSum = shop.addSum
                        val typePay = viewState.value.typePay.value
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
                        updateShop(newShop = dataShop, oldShop = shop)
                    }
                }
            } catch (e: Exception) {
                sharedViewModel.message(e.message)
            } finally {
                dismissRequestDialog()
            }
        }
    }

    private fun updateClient(shop: ShopModel) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                shop.apply {
                    val responseGetClient = clientApi.getClientById(idClient = id)
                    if (responseGetClient.success) {
                        val client = responseGetClient.obj
                        if (client != null ) {
                            val updateClient = client.copy(arrears = arrears)
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
                            val response = clientApi.updateClient(request)
                            if (response.success) {
                                loggerApi.addLog(
                                    log = LogRequest(
                                        idFactory = sharedViewModel.viewState.value.factory?.id!!,
                                        log = "Пользователь: ${sharedViewModel.viewState.value.user?.name}" +
                                                ", успешно обновил клиента: \n${client.name}" +
                                                "\n${
                                                    findChangedFields(
                                                        client, updateClient
                                                    )
                                                }",
                                        date = System.currentTimeMillis()
                                    )
                                )
                            } else {
                                sharedViewModel.message(response.message)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                sharedViewModel.message(e.message)
            }
        }
    }

    private fun updateShop(newShop: ShopModel, oldShop: ShopModel) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                oldShop.apply {
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
                        nameShop = nameShop
                    )
                    val response = shopApi.updateShop(shopRequest)
                    if (response.success) {
                        loggerApi.addLog(
                            log = LogRequest(
                                idFactory = sharedViewModel.viewState.value.factory?.id!!,
                                log = "Пользователь: ${sharedViewModel.viewState.value.user?.name}" +
                                        ", успешно обновил магазин: \n${newShop.nameShop}" +
                                        "\nв рейсе: ${viewState.value.currentTrip?.nameRoute} ${
                                            convertToTextDate(
                                                viewState.value.currentTrip?.date!!
                                            )
                                        }" +
                                        "\n${
                                            findChangedFields(
                                                oldShop, newShop
                                            )
                                        }",
                                date = System.currentTimeMillis()
                            )
                        )
                        val list = viewState.value.listShop.value.map { it.copy() }.toMutableList()
                        val index = list.indexOfFirst { it.id == newShop.id }
                        list[index] = newShop
                        updateViewState { it.copy(listShop = MutableStateFlow(list)) }
                    } else {
                        sharedViewModel.message(response.message)
                    }
                }

            } catch (e: Exception) {
                sharedViewModel.message(e.message)
            }
        }
    }

    private fun updateRequest(request: RequestModel, item: RequestModel) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                request.apply {
                    val reqResponse = UpdateRequestShopRequest(
                        id = request.id,
                        idShop = idShop,
                        idTrip = idTrip,
                        idFactory = idFactory,
                        count = request.count,
                        bonus = request.bonus,
                        status = status,
                        exchange = request.exchange,
                        price = price,
                        oldPrice = oldPrice,
                        name = name,
                        counter = counter
                    )
                    val response = requestApi.updateRequest(reqResponse)
                    if (response.success) {
                        loggerApi.addLog(
                            log = LogRequest(
                                idFactory = sharedViewModel.viewState.value.factory?.id!!,
                                log = "Пользователь: ${sharedViewModel.viewState.value.user?.name}" +
                                        ", успешно обновил заявку: \n${request.name}" +
                                        "\nв магазине: ${viewState.value.currentShop?.nameShop} ${
                                            convertToTextDate(
                                                viewState.value.currentTrip?.date!!
                                            )
                                        }" +
                                        "\n${
                                            findChangedFields(
                                                item, request
                                            )
                                        }",
                                date = System.currentTimeMillis()
                            )
                        )
                    } else {
                        sharedViewModel.message(response.message)
                    }
                }
            } catch (e: Exception) {
                sharedViewModel.message(e.message)
            }
        }
    }


    private fun dismissBlockDialog() {
        updateViewState { it.copy(stateIsBlocked = false) }
    }

    private fun dismissInfoShopDialog() {
        updateViewState { it.copy(stateInfoShopDialog = false) }
    }

    private fun openInfoShopDialog(shop: ShopModel) {
        updateViewState { it.copy(stateInfoShopDialog = true, currentShop = shop) }
        getDataInfoShop(shop)
    }

    private fun getDataInfoShop(curShop: ShopModel) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = shopApi.getAllCurrentShopsCurrentFactory(
                    idShop = curShop.id,
                    idFactory = curShop.idFactory
                )
                if (response.success) {
                    val list = response.obj
                    if (list != null) {
                        updateViewState {
                            it.copy(
                                listInfoShop = MutableStateFlow(list),
                                stateInfoShopIsDataLoad = true
                            )
                        }
                    } else {
                        sharedViewModel.message(Constants.ERROR.LIST_EMPTY)
                    }
                } else {
                    sharedViewModel.message(response.message)
                }
            } catch (e: Exception) {
                sharedViewModel.message(e.message)
            }
        }
    }
}