package ru.krymer.delivery.ui.screens.shop.models

import kotlinx.coroutines.flow.MutableStateFlow
import ru.krymer.delivery.data.model.ClientModel
import ru.krymer.delivery.data.model.ProductModel
import ru.krymer.delivery.data.model.RequestModel
import ru.krymer.delivery.data.model.ShopModel
import ru.krymer.delivery.data.model.TripModel
import ru.krymer.delivery.data.model.utilModel.TypePayModel


sealed class ShopAction {
    data object OpenRequest : ShopAction()
    data object None : ShopAction()
}

data class ShopViewState(
    val shopAction: ShopAction = ShopAction.None,

    val isLoadShopsData: Boolean = false,
    val listShop: MutableStateFlow<List<ShopModel>> = MutableStateFlow(listOf()),

    val listProduct: MutableStateFlow<List<ProductModel>> = MutableStateFlow(listOf()),
    val listProductRequest: MutableStateFlow<List<ProductModel>> = MutableStateFlow(listOf()),
    val isLoadDataProducts: Boolean = false,

    val listClient: MutableStateFlow<List<ClientModel>> = MutableStateFlow(listOf()),
    val isLoadDataClients: Boolean = false,
    val isShowDialogWithListCurrentClients: Boolean = false,
    val isShowDialogWithListAllClients: Boolean = false,
    val stateAddDialog: Boolean = false,
    val currentClient: ClientModel? = null,
    val isShowSelectorClientInAddDialog: Boolean = false,
    val isAdminServices: Boolean = false,
    val arrear: MutableStateFlow<Double> = MutableStateFlow(0.0),
    val dept: MutableStateFlow<Double> = MutableStateFlow(0.0),
    val add: MutableStateFlow<Double> = MutableStateFlow(0.0),

    val showDialogAddRequest: Boolean = false,

    val isShowMillageDialog: Boolean = false,
    val millage: MutableStateFlow<Double> = MutableStateFlow(0.0),
    val salary: MutableStateFlow<Double> = MutableStateFlow(0.0),
    val cash: MutableStateFlow<Double> = MutableStateFlow(0.0),
    val noCash: MutableStateFlow<Double> = MutableStateFlow(0.0),
    val allMoney: MutableStateFlow<Double> = MutableStateFlow(0.0),
    val remains: MutableStateFlow<Double> = MutableStateFlow(0.0),
    val isDataShopForCourierLoad: Boolean = false,

    val currentTrip: TripModel? = null,

    val currentShop: ShopModel? = null,
    val showRequestDialog: Boolean = false,
    val listDataRequests: MutableStateFlow<List<RequestModel>> = MutableStateFlow(listOf()),
    val orderMoney: MutableStateFlow<Double> = MutableStateFlow(0.0),
    val getCash: MutableStateFlow<String> = MutableStateFlow(""),
    val getNoCash: MutableStateFlow<String> = MutableStateFlow(""),

    val stateInfoDialog: Boolean = false,
    val isLoadDataRequestsInfoDialog: Boolean = false,
    val listInfoRequests: MutableStateFlow<List<RequestModel>> = MutableStateFlow(listOf()),
    val allCountRequestsInfo: MutableStateFlow<Int> = MutableStateFlow(0),
    val allExchangeRequestsInfo: MutableStateFlow<Int> = MutableStateFlow(0),

    val isShowDialogArrears: Boolean = false,
    val isShowAddSumDialog: Boolean = false,

    val shopDeleted: ShopModel? = null,
    val itemNameToDelete: String = "",
    val showDeleteDialog: Boolean = false,
    val typePay: MutableStateFlow<TypePayModel> = MutableStateFlow(TypePayModel.CASH),
    val isShowTypePayChangeDialog: Boolean = false,
    val isShowDropDownTypePay: Boolean = false,

    val stateConfirmRequestDialog: Boolean = false,


    val stateInfoShopDialog: Boolean = false,
    val stateInfoShopIsDataLoad: Boolean = false,
    val listInfoShop: MutableStateFlow<List<ShopModel>> = MutableStateFlow(listOf()),
    val stateIsBlocked: Boolean = false,
    val stateSwitchPrice: MutableStateFlow<Boolean> = MutableStateFlow(false)
)