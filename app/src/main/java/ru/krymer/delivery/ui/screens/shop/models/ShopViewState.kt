package ru.krymer.delivery.ui.screens.shop.models

import ru.krymer.delivery.data.model.ClientModel
import ru.krymer.delivery.data.model.LogShopModel
import ru.krymer.delivery.data.model.MessageModel
import ru.krymer.delivery.data.model.ProductModel
import ru.krymer.delivery.data.model.RequestModel
import ru.krymer.delivery.data.model.ShopModel
import ru.krymer.delivery.data.model.ShopServerModel
import ru.krymer.delivery.data.model.TripModel
import ru.krymer.delivery.data.model.utilModel.TypePayModel


sealed class ShopAction {
    data object None : ShopAction()
}

data class ShopViewState(
    val shopAction: ShopAction = ShopAction.None,
    val isSettingsInstall: Boolean = false,

    val shops: List<ShopModel> = emptyList(),
    val shopsAnalitic: List<ShopServerModel> = emptyList(),

    val listProduct: List<ProductModel> = emptyList(),
    val listProductRequest: List<ProductModel> = emptyList(),


    val listClient: List<ClientModel> = emptyList(),
    val toggleDialogCurrentClients: Boolean = false,
    val toggleDialogAllClients: Boolean = false,
    val toggleAddDialog: Boolean = false,
    val currentClient: ClientModel? = null,
    val arrear: Double = 0.0,
    val dept: Double = 0.0,
    val add: Double = 0.0,

    val toggleAddRequestDialog: Boolean = false,

    val toggleMillageDialog: Boolean = false,
    val millage: Double = 0.0,
    val salaryFix: Double = 0.0,
    val salary: Double = 0.0,
    val cash: Double = 0.0,
    val noCash: Double = 0.0,
    val allMoney: Double = 0.0,
    val remains: Double = 0.0,
    val isDataShopForCourierLoad: Boolean = false,

    val currentTrip: TripModel? = null,
    val copyShop: ShopServerModel? = null,
    val currentShop: ShopModel? = null,
    val toggleRequestDialog: Boolean = false,
    val listDataRequests: List<RequestModel> = emptyList(),
    val orderMoney: Double = 0.0,
    val getCash: String = "",
    val getNoCash: String = "",

    val toggleInfoTrip: Boolean = false,
    val isLoadDataRequestsInfoDialog: Boolean = false,
    val listInfoRequests: List<RequestModel> = emptyList(),
    val allCountRequestsInfo: Int = 0,
    val allExchangeRequestsInfo: Int = 0,

    val toggleArrearsDialog: Boolean = false,
    val toggleAddSumDialog: Boolean = false,

    val toggleDeleteDialog: Boolean = false,
    val typePay: TypePayModel = TypePayModel.CASH,
    val toggleTypePayDialog: Boolean = false,

    val toggleConfirmRequestDialog: Boolean = false,


    val toggleCurrentShopInfo: Boolean = false,
    val listCurrentShopInfo: List<ShopServerModel> = emptyList(),
    val stateSwitchPrice: Boolean = false,

    val messages: List<MessageModel> = emptyList(),
    val toggleMessageDialog: Boolean = false,
    val message: String = "",
    val messageDelete: MessageModel? =null,
    val isShowDeleteMessage: Boolean = false,
    val isBonusState: Boolean = false,

    val toggleAnaliticOfTrip: Boolean = false,
    val lightVersion: Boolean = false,

    val toggleLogShop: Boolean = false,
    val logShop: List<LogShopModel> = emptyList(),

    val isCopyAndSave: Boolean = false
    )