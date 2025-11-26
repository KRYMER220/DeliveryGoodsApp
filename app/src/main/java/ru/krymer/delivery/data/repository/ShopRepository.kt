package ru.krymer.delivery.data.repository

import retrofit2.http.Body
import retrofit2.http.Query
import ru.krymer.delivery.data.model.ClientModel
import ru.krymer.delivery.data.model.CourierInfoModel
import ru.krymer.delivery.data.model.LogShopModel
import ru.krymer.delivery.data.model.ProductModel
import ru.krymer.delivery.data.model.RequestModel
import ru.krymer.delivery.data.model.ShopModel
import ru.krymer.delivery.data.model.ShopServerModel
import ru.krymer.delivery.data.model.TripModel
import ru.krymer.delivery.data.request.ClientRequest
import ru.krymer.delivery.data.request.RequestShopRequest
import ru.krymer.delivery.data.request.ShopRequest
import ru.krymer.delivery.data.request.TripRequest
import ru.krymer.delivery.data.response.BaseResponse
import ru.krymer.delivery.utills.Constants
import ru.krymer.delivery.utills.MyResult

interface ShopRepository {
    suspend fun getShops(idTrip: Long): MyResult<List<ShopServerModel>>
    suspend fun getLogsCurrentShop(idFactory: Long,idShop: Long,idTrip: Long): MyResult<List<LogShopModel>>

    suspend fun create(shop: ShopRequest): MyResult<ShopModel?>
    suspend fun update(shop: ShopRequest): MyResult<ShopModel?>

    suspend fun updateClient(request: ClientRequest): MyResult<Unit>

    suspend fun updateTrip(trip: TripRequest): MyResult<TripModel?>
    suspend fun getDataAboutCourier(idTrip: Long, idFactory: Long) : MyResult<CourierInfoModel?>
    suspend fun getTrip(idTrip: Long): MyResult<TripModel?>
    suspend fun updateRequest(request: RequestShopRequest): MyResult<RequestModel?>
    suspend fun getProducts(idFactory: Long): MyResult<List<ProductModel>>
    suspend fun getClientsByFactory(idFactory: Long): MyResult<List<ClientModel>>
    suspend fun getClientsByRoute(idRoute: Long): MyResult<List<ClientModel>>
    suspend fun getClientById(id: Long): MyResult<ClientModel?>
    suspend fun createRequest(request: RequestShopRequest): MyResult<RequestModel?>

    suspend fun getRequestsByTrip(idFactory: Long, idTrip: Long,
    ): MyResult<List<RequestModel>>

    suspend fun getCurrentShopsByFactory(idFactory: Long, id: Long
    ): MyResult<List<ShopServerModel>>
}