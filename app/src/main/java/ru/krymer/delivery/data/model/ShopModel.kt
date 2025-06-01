package ru.krymer.delivery.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.krymer.delivery.data.model.utilModel.TypePayModel
import ru.krymer.delivery.data.model.utilModel.getStringByTypePay

data class ShopModel(
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
    var listRequest: List<RequestModel>,
    var isOldPrice: Boolean,
    val cord: String,
    var isBonus: Boolean
)

@Entity(tableName = "shop", primaryKeys = ["id", "idTrip"])
data class ShopLocalModel(
    val id: Long,
    val idTrip: Long,
    val idFactory: Long,
    val nameShop: String,
    val arrears: Double,
    val addSum: Double,
    val status: Boolean,
    val date: Long,
    val typePay: String,
    val cash: Double,
    val counter: Int,
    val noCash: Double,
    val isOldPrice: Boolean,
    val cord: String,
    var isBonus: Boolean
)

fun ShopModel.toLocal() = ShopLocalModel(
    id = id,
    idTrip =idTrip,
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
    isBonus = isBonus
)