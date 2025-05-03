package ru.krymer.delivery.data.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import ru.krymer.delivery.data.model.ShopModel
import ru.krymer.delivery.data.request.CreateShopRequest
import ru.krymer.delivery.data.request.UpdateShopRequest
import ru.krymer.delivery.data.response.BaseResponse
import ru.krymer.delivery.utills.Constants

interface ShopApi {
    @POST("shop/create")
    suspend fun add(@Body shop: CreateShopRequest): BaseResponse<ShopModel>

    @GET("shops/by/trip")
    suspend fun getShopsByTrip(
        @Query(Constants.ID.ID_TRIP) idTrip: Long
    ): BaseResponse<List<ShopModel>>

    @GET("shops/by/factory")
    suspend fun getShops(
        @Query(Constants.ID.ID_FACTORY) idFactory: Long
    ): BaseResponse<List<ShopModel>>

    @POST("shop/update")
    suspend fun update(@Body shop: UpdateShopRequest): BaseResponse<ShopModel>

    @DELETE("shop/delete")
    suspend fun delete(
        @Query(Constants.ID.ID) id: Long,
        @Query(Constants.ID.ID_TRIP) idTrip: Long
    ): BaseResponse<ShopModel>

    @GET("shops/for/info")
    suspend fun getCurrentShopsByFactory(
        @Query(Constants.ID.ID_FACTORY) idFactory: Long,
        @Query(Constants.ID.ID_SHOP) id: Long
    ): BaseResponse<List<ShopModel>>
}