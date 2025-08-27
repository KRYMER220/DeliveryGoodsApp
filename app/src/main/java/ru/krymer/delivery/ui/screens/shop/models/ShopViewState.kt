package ru.krymer.delivery.ui.screens.shop.models

import kotlinx.coroutines.flow.MutableStateFlow
import ru.krymer.delivery.data.model.ClientModel
import ru.krymer.delivery.data.model.LogShopModel
import ru.krymer.delivery.data.model.MessageModel
import ru.krymer.delivery.data.model.ProductModel
import ru.krymer.delivery.data.model.RequestModel
import ru.krymer.delivery.data.model.ShopModel
import ru.krymer.delivery.data.model.TripModel
import ru.krymer.delivery.data.model.utilModel.TypePayModel


sealed class ShopAction {
    data object None : ShopAction()
}

data class ShopViewState(
    val shopAction: ShopAction = ShopAction.None,

    val listUIShop: List<ShopModel> = emptyList(),

    val listProduct: List<ProductModel> = emptyList(),
    val listProductRequest: List<ProductModel> = emptyList(),


    val listClient: List<ClientModel> = emptyList(),
    val isShowDialogWithListCurrentClients: Boolean = false,
    val isShowDialogWithListAllClients: Boolean = false,
    val stateAddDialog: Boolean = false,
    val currentClient: ClientModel? = null,
    val isShowSelectorClientInAddDialog: Boolean = false,
    val arrear: Double = 0.0,
    val dept: Double = 0.0,
    val add: Double = 0.0,

    val showDialogAddRequest: Boolean = false,

    val isShowMillageDialog: Boolean = false,
    val millage: Double = 0.0,
    val salaryFix: Double = 0.0,
    val salary: Double = 0.0,
    val cash: Double = 0.0,
    val noCash: Double = 0.0,
    val allMoney: Double = 0.0,
    val remains: Double = 0.0,
    val isDataShopForCourierLoad: Boolean = false,

    val currentTrip: TripModel? = null,

    val currentShop: ShopModel? = null,
    val showRequestDialog: Boolean = false,
    val listDataRequests: List<RequestModel> = emptyList(),
    val orderMoney: Double = 0.0,
    val getCash: String = "",
    val getNoCash: String = "",

    val stateInfoDialog: Boolean = false,
    val isLoadDataRequestsInfoDialog: Boolean = false,
    val listInfoRequests: List<RequestModel> = emptyList(),
    val allCountRequestsInfo: Int = 0,
    val allExchangeRequestsInfo: Int = 0,

    val isShowDialogArrears: Boolean = false,
    val isShowAddSumDialog: Boolean = false,
    val isShowAddSumView: Boolean = false,

    val showDeleteDialog: Boolean = false,
    val typePay: TypePayModel = TypePayModel.CASH,
    val isShowDropDownTypePay: Boolean = false,

    val stateConfirmRequestDialog: Boolean = false,


    val stateInfoShopDialog: Boolean = false,
    val listInfoShop: List<ShopModel> = emptyList(),
    val stateSwitchPrice: Boolean = false,

    val messages: List<MessageModel> = emptyList(),
    val isShowMessageDialog: Boolean = false,
    val message: String = "",
    val messageDelete: MessageModel? =null,
    val isShowDeleteMessage: Boolean = false,
    val isBonusState: Boolean = false,

    val isShowAnaliticTrip: Boolean = false,
    val lightVersion: Boolean = false,

    val isShowInfoShop: Boolean = false,
    val logShop: List<LogShopModel> = emptyList(),

    val isCopyAndSave: Boolean = false
    )