package ru.krymer.delivery.data.model

import androidx.room.Entity
import kotlinx.serialization.Serializable
import ru.krymer.delivery.data.model.utilModel.StatusModel
import ru.krymer.delivery.data.model.utilModel.TypePayModel
import ru.krymer.delivery.data.model.utilModel.getStringByTypePay
import ru.krymer.delivery.data.model.utilModel.getTypePayByString
import ru.krymer.delivery.data.model.utilModel.toStr

@Entity(tableName = "shop", primaryKeys = ["id", "idTrip"])
@Serializable
data class ShopModel(
    val id: Long,
    var idTrip: Long,
    val idFactory: Long,
    val nameShop: String,
    var arrears: Double,
    var addSum: Double,
    var status: Boolean,
    var date: Long,
    var typePay: String,
    var cash: Double,
    val counter: Int,
    var noCash: Double,
    var isOldPrice: Boolean,
    val cord: String,
    var isBonus: Boolean,
    var isChanged: Boolean,
    val statusServer: String = StatusModel.NOT_CHANGE.toStr(),
)

data class ShopServerModel(
    val id: Long,
    var idTrip: Long,
    val idFactory: Long,
    val nameShop: String,
    var arrears: Double,
    var addSum: Double,
    var status: Boolean,
    var date: Long,
    var typePay: TypePayModel,
    var cash: Double,
    val counter: Int,
    var noCash: Double,
    var isOldPrice: Boolean,
    val cord: String,
    var listRequest: List<RequestModel>,
    var isBonus: Boolean,
    var isChanged: Boolean,
    var nameCourier: String = ""
)

data class ShopUIModel(
    val id: Long,
    var idTrip: Long,
    val idFactory: Long,
    val nameShop: String,
    var arrears: Double,
    var addSum: Double,
    var status: Boolean,
    var date: Long,
    var typePay: TypePayModel,
    var cash: Double,
    val counter: Int,
    var noCash: Double,
    var isOldPrice: Boolean,
    val cord: String,
    var listRequest: List<RequestModel>,
    var isBonus: Boolean,
    var isChanged: Boolean,
    val statusServer: String = StatusModel.NOT_CHANGE.toStr(),
)

fun ShopServerModel.toUiModel() = ShopModel(
    id = id,
    idTrip = idTrip,
    idFactory = idFactory,
    nameShop = nameShop,
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
    isBonus = isBonus,
    isChanged = isChanged,
    statusServer = StatusModel.NOT_CHANGE.toStr()
)

fun ShopModel.toServerModel() = ShopServerModel(
    id = id,
    idTrip = idTrip,
    idFactory = idFactory,
    nameShop = nameShop,
    arrears = arrears,
    addSum = addSum,
    status = status,
    date = date,
    typePay = typePay.getTypePayByString(),
    cash = cash,
    counter = counter,
    noCash = noCash,
    isOldPrice = isOldPrice,
    cord = cord,
    isBonus = isBonus,
    isChanged = isChanged,
    listRequest = emptyList(),
)
