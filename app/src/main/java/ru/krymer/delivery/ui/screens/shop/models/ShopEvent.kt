package ru.krymer.delivery.ui.screens.shop.models

import android.content.Context
import ru.krymer.delivery.data.model.ClientModel
import ru.krymer.delivery.data.model.CourierInfoModel
import ru.krymer.delivery.data.model.LogShopModel
import ru.krymer.delivery.data.model.MessageModel
import ru.krymer.delivery.data.model.ProductModel
import ru.krymer.delivery.data.model.ShopModel
import ru.krymer.delivery.data.model.ShopServerModel
import ru.krymer.delivery.data.model.TripModel

sealed class ShopEvent {
    data class ToggleLogsShopDialog(val shop: ShopModel?): ShopEvent()
    data class ShowLogsShop(val logs: List<LogShopModel>): ShopEvent()
    data class LoadState(val state: ShopViewState) : ShopEvent()
    data class RefreshShops(val trip: TripModel)  : ShopEvent()
    data class SaveCurrentTrip(val trip: TripModel) : ShopEvent()
    data class ShopsLoaded(val shops: List<ShopModel>) : ShopEvent()
    data class ShopsForCreateShopLoaded(val shops: List<ShopServerModel>) : ShopEvent()
    data class ClientsLoaded(val clients: List<ClientModel>) : ShopEvent()
    data class MessageLoaded(val messages: List<MessageModel>) : ShopEvent()
    data class ShopsAnaliticLoaded(val shops: List<ShopServerModel>) : ShopEvent()
    data class ProductsLoaded(val products: List<ProductModel>) : ShopEvent()
    data class ProductsRequestLoaded(val products: List<ProductModel>) : ShopEvent()
    data class CourierInfoLoaded(val info: CourierInfoModel, val trip: TripModel) : ShopEvent()
    data class Initialize(val trip: TripModel) : ShopEvent()
    data object InitSettings: ShopEvent()

    data object ShowAddDialogShopCurrentRoute : ShopEvent()
    data object ShowAddDialogShopAllRoutes : ShopEvent()
    data class ChangeCountBonusProduct(val bonus: String, val product: ProductModel) : ShopEvent()
    data class ChangeCountProduct(val count: String, val product: ProductModel) : ShopEvent()
    data object DismissAddDialog : ShopEvent()
    data object ShopAddAction : ShopEvent()
    data class OpenGeoPoint(val context: Context, val cord: String) : ShopEvent()
    data class SelectClient(val client: ClientModel?) : ShopEvent()
    data object ToggleMillageDialog : ShopEvent()
    data object MillageSaveAction : ShopEvent()
    data class ValueChangeCash(val money: String) : ShopEvent()
    data class ValueChangeNoCashMoney(val money: String) : ShopEvent()
    data class ValueChangeMillage(val millage: Double) : ShopEvent()
    data object GetDataRequestsByTrip : ShopEvent()

    data class OpenInfoShopDialog(val shop: ShopModel) : ShopEvent()
    data object ToggleInfoDialogAboutCurrentShop : ShopEvent()

    data object RequestAddAction : ShopEvent()
    data class DeleteMessage(val message: MessageModel): ShopEvent()

    data class CopyInfoData(val context: Context) : ShopEvent()

    data object SwitchBonusState: ShopEvent()
    data object ToggleAnaliticShopsCurrentTrip : ShopEvent()
    data object CopyAndSaveShop : ShopEvent()

    data object ToggleConfirmCopyAndSave : ShopEvent()
    data class SelectShop(val shop: ShopServerModel) : ShopEvent()

    data class Error(val message: String?) : ShopEvent()
}