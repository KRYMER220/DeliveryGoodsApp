package ru.krymer.delivery.data.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import ru.krymer.delivery.data.model.CourierInfoModel
import ru.krymer.delivery.data.model.ShopModel
import ru.krymer.delivery.data.request.CreateShopRequest
import ru.krymer.delivery.data.request.UpdateShopRequest
import ru.krymer.delivery.data.response.BaseResponse
import ru.krymer.delivery.utills.Constants

interface ShopApi {
    @POST("create-shop")
    suspend fun addShop(@Body shop: CreateShopRequest): BaseResponse<ShopModel>

    @GET("get-all-shops")
    suspend fun getAllShops(): BaseResponse<List<ShopModel>>

    @GET("get-current-shops")
    suspend fun getCurrentShops(
        @Query(Constants.ID.ID_TRIP) idTrip: Long
    ): BaseResponse<List<ShopModel>>

    @GET("get-all-current-shops")
    suspend fun getAllShops(
        @Query(Constants.ID.ID_FACTORY) idFactory: Long
    ): BaseResponse<List<ShopModel>>

    @POST("update-shop")
    suspend fun updateShop(@Body shop: UpdateShopRequest): BaseResponse<ShopModel>

    @DELETE("delete-shop")
    suspend fun deleteShop(
        @Query(Constants.ID.ID) idShop: Long,
        @Query(Constants.ID.ID_TRIP) idTrip: Long
    ): BaseResponse<ShopModel>

    @GET("get-data-current-shops")
    suspend fun getDataForCourier(
        @Query(Constants.ID.ID_FACTORY) idFactory: Long,
        @Query(Constants.ID.ID_TRIP) idTrip: Long
    ): BaseResponse<CourierInfoModel>

    @GET("get-all-current-shops-factory")
    suspend fun getAllCurrentShopsCurrentFactory(
        @Query(Constants.ID.ID_FACTORY) idFactory: Long,
        @Query(Constants.ID.ID_SHOP) idShop: Long
    ): BaseResponse<List<ShopModel>>
}