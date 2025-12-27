package ru.krymer.delivery.data.model

import androidx.room.Entity
import ru.krymer.delivery.data.model.utilModel.StatusModel
import ru.krymer.delivery.data.model.utilModel.toStr
import ru.krymer.delivery.data.request.RequestShopRequest

@Entity(tableName = "request", primaryKeys = ["id", "idTrip", "idShop"])
data class RequestModel(
    val id: Long,
    val idShop: Long,
    val idTrip: Long,
    val idFactory: Long,
    val count: Int,
    val exchange: Int,
    val countRemain: Int = 0,
    val bonus: Int,
    val status: Boolean,
    val price: Double,
    val oldPrice: Double,
    val name: String,
    val counter: Int,
    val statusServer: String = StatusModel.NOT_CHANGE.toStr(),
)

fun RequestModel.toRequest() = RequestShopRequest(
    id = id,
    idShop = idShop,
    idTrip = idTrip,
    idFactory = idFactory,
    count = count,
    bonus = bonus,
    status = status,
    exchange = exchange,
    price = price,
    oldPrice = oldPrice,
    name = name,
    counter = counter
)
