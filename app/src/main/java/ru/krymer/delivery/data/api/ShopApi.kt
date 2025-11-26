package ru.krymer.delivery.data.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import ru.krymer.delivery.data.model.ShopModel
import ru.krymer.delivery.data.model.ShopServerModel
import ru.krymer.delivery.data.request.ShopRequest
import ru.krymer.delivery.data.response.BaseResponse
import ru.krymer.delivery.utills.Constants

interface ShopApi {
    @POST("shop/create")
    suspend fun create(@Body shop: ShopRequest): BaseResponse<ShopModel>

    @GET("shops/by/trip")
    suspend fun getShopsByTrip(
        @Query(Constants.HttpRequestKeys.ID_TRIP) idTrip: Long
    ): BaseResponse<List<ShopServerModel>>

    @POST("shop/update")
    suspend fun update(@Body shop: ShopRequest): BaseResponse<ShopModel>

    @DELETE("shop/delete")
    suspend fun delete(
        @Query(Constants.HttpRequestKeys.ID) id: Long,
        @Query(Constants.HttpRequestKeys.ID_TRIP) idTrip: Long
    ): BaseResponse<ShopServerModel>

    @GET("shops/for/info")
    suspend fun getCurrentShopsByFactory(
        @Query(Constants.HttpRequestKeys.ID_FACTORY) idFactory: Long,
        @Query(Constants.HttpRequestKeys.ID_SHOP) id: Long
    ): BaseResponse<List<ShopServerModel>>
}