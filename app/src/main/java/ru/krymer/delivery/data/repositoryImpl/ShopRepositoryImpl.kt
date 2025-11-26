package ru.krymer.delivery.data.repositoryImpl

import ru.krymer.delivery.data.api.ClientApi
import ru.krymer.delivery.data.api.LoggerApi
import ru.krymer.delivery.data.api.ProductApi
import ru.krymer.delivery.data.api.RequestApi
import ru.krymer.delivery.data.api.ShopApi
import ru.krymer.delivery.data.api.TripApi
import ru.krymer.delivery.data.model.ClientModel
import ru.krymer.delivery.data.model.CourierInfoModel
import ru.krymer.delivery.data.model.LogShopModel
import ru.krymer.delivery.data.model.ProductModel
import ru.krymer.delivery.data.model.RequestModel
import ru.krymer.delivery.data.model.ShopModel
import ru.krymer.delivery.data.model.ShopServerModel
import ru.krymer.delivery.data.model.TripModel
import ru.krymer.delivery.data.repository.ShopRepository
import ru.krymer.delivery.data.request.ClientRequest
import ru.krymer.delivery.data.request.RequestShopRequest
import ru.krymer.delivery.data.request.ShopRequest
import ru.krymer.delivery.data.request.TripRequest
import ru.krymer.delivery.utills.MyResult
import javax.inject.Inject
class ShopRepositoryImpl @Inject constructor(
    private val shopApi: ShopApi,
    private val loggerApi: LoggerApi,
    private val clientApi: ClientApi,
    private val tripApi: TripApi,
    private val requestApi: RequestApi,
    private val productApi: ProductApi
): ShopRepository{
    override suspend fun getShops(idTrip: Long): MyResult<List<ShopServerModel>> {
        return try {
            val resp = shopApi.getShopsByTrip(idTrip = idTrip)
            if (resp.success) MyResult.Success(resp.obj ?: emptyList())
            else MyResult.Error(resp.message)
        } catch (t: Throwable) {
            MyResult.Error(t.message)
        }
    }

    override suspend fun getLogsCurrentShop(
        idFactory: Long,
        idShop: Long,
        idTrip: Long
    ): MyResult<List<LogShopModel>> {
        return try {
            val resp = loggerApi.getLogsShop(
                idFactory = idFactory,
                idShop = idShop,
                idTrip = idTrip
            )
            if (resp.success) MyResult.Success(resp.obj ?: emptyList())
            else MyResult.Error(resp.message)
        } catch (t: Throwable) {
            MyResult.Error(t.message)
        }
    }

    override suspend fun create(shop: ShopRequest): MyResult<ShopModel?> {
        return try {
            val resp = shopApi.create(shop = shop)
            if (resp.success) MyResult.Success(resp.obj)
            else MyResult.Error(resp.message)
        } catch (t: Throwable) {
            MyResult.Error(t.message)
        }
    }

    override suspend fun update(shop: ShopRequest): MyResult<ShopModel?> {
        return try {
            val resp = shopApi.update(shop = shop)
            if (resp.success) MyResult.Success(resp.obj)
            else MyResult.Error(resp.message)
        } catch (t: Throwable) {
            MyResult.Error(t.message)
        }
    }

    override suspend fun updateClient(request: ClientRequest): MyResult<Unit> {
        return try {
            val resp = clientApi.update(request = request)
            if (resp.success) MyResult.Success(Unit)
            else MyResult.Error(resp.message)
        } catch (t: Throwable) {
            MyResult.Error(t.message)
        }
    }

    override suspend fun updateTrip(trip: TripRequest): MyResult<TripModel?> {
        return try {
            val resp = tripApi.update(trip = trip)
            if (resp.success) MyResult.Success(resp.obj)
            else MyResult.Error(resp.message)
        } catch (t: Throwable) {
            MyResult.Error(t.message)
        }
    }

    override suspend fun getDataAboutCourier(
        idTrip: Long,
        idFactory: Long
    ): MyResult<CourierInfoModel?> {
        return try {
            val resp = tripApi.getDataAboutTrip(idTrip = idTrip, idFactory = idFactory)
            if (resp.success) MyResult.Success(resp.obj)
            else MyResult.Error(resp.message)
        } catch (t: Throwable) {
            MyResult.Error(t.message)
        }
    }

    override suspend fun getTrip(idTrip: Long): MyResult<TripModel?> {
        return try {
            val resp = tripApi.getTrip(idTrip = idTrip)
            if (resp.success) MyResult.Success(resp.obj)
            else MyResult.Error(resp.message)
        } catch (t: Throwable) {
            MyResult.Error(t.message)
        }
    }

    override suspend fun updateRequest(request: RequestShopRequest): MyResult<RequestModel?> {
        return try {
            val resp = requestApi.update(request = request)
            if (resp.success) MyResult.Success(resp.obj)
            else MyResult.Error(resp.message)
        } catch (t: Throwable) {
            MyResult.Error(t.message)
        }
    }

    override suspend fun getProducts(idFactory: Long): MyResult<List<ProductModel>> {
        return try {
            val resp = productApi.getProducts(idFactory = idFactory)
            if (resp.success) MyResult.Success(resp.obj ?: emptyList())
            else MyResult.Error(resp.message)
        } catch (t: Throwable) {
            MyResult.Error(t.message)
        }
    }

    override suspend fun getClientsByFactory(idFactory: Long): MyResult<List<ClientModel>> {
        return try {
            val resp = clientApi.getClientsByFactory(idFactory = idFactory)
            if (resp.success) MyResult.Success(resp.obj ?: emptyList())
            else MyResult.Error(resp.message)
        } catch (t: Throwable) {
            MyResult.Error(t.message)
        }
    }

    override suspend fun getClientsByRoute(idRoute: Long): MyResult<List<ClientModel>> {
        return try {
            val resp = clientApi.getClientsByRoute(idRoute = idRoute)
            if (resp.success) MyResult.Success(resp.obj ?: emptyList())
            else MyResult.Error(resp.message)
        } catch (t: Throwable) {
            MyResult.Error(t.message)
        }
    }

    override suspend fun getClientById(id: Long): MyResult<ClientModel?> {
        return try {
            val resp = clientApi.getClientById(id = id)
            if (resp.success) MyResult.Success(resp.obj)
            else MyResult.Error(resp.message)
        } catch (t: Throwable) {
            MyResult.Error(t.message)
        }
    }

    override suspend fun createRequest(request: RequestShopRequest): MyResult<RequestModel?> {
        return try {
            val resp = requestApi.create(request = request)
            if (resp.success) MyResult.Success(resp.obj)
            else MyResult.Error(resp.message)
        } catch (t: Throwable) {
            MyResult.Error(t.message)
        }
    }

    override suspend fun getRequestsByTrip(
        idFactory: Long,
        idTrip: Long
    ): MyResult<List<RequestModel>> {
        return try {
            val resp = requestApi.getRequestsByTrip(idTrip = idTrip, idFactory = idFactory)
            if (resp.success) MyResult.Success(resp.obj ?: emptyList())
            else MyResult.Error(resp.message)
        } catch (t: Throwable) {
            MyResult.Error(t.message)
        }
    }

    override suspend fun getCurrentShopsByFactory(
        idFactory: Long,
        id: Long
    ): MyResult<List<ShopServerModel>> {
        return try {
            val resp = shopApi.getCurrentShopsByFactory(id = id, idFactory = idFactory)
            if (resp.success) MyResult.Success(resp.obj ?: emptyList())
            else MyResult.Error(resp.message)
        } catch (t: Throwable) {
            MyResult.Error(t.message)
        }
    }
}