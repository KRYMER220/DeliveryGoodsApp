package ru.krymer.delivery.data.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import ru.krymer.delivery.data.model.RequestModel
import ru.krymer.delivery.data.request.CreateRequestShopRequest
import ru.krymer.delivery.data.request.UpdateRequestShopRequest
import ru.krymer.delivery.data.response.BaseResponse
import ru.krymer.delivery.utills.Constants

interface RequestApi {
    @POST("request/create")
    suspend fun add(@Body request: CreateRequestShopRequest): BaseResponse<RequestModel>

    @GET("requests/get/trip")
    suspend fun getRequestsByTrip(
        @Query(Constants.ID.ID_FACTORY) idFactory: Long,
        @Query(Constants.ID.ID_TRIP) idTrip: Long,
    ): BaseResponse<List<RequestModel>>

    @POST("request/update")
    suspend fun update(@Body request: UpdateRequestShopRequest): BaseResponse<RequestModel>

    @DELETE("request/delete")
    suspend fun delete(
        @Query(Constants.ID.ID) id: Long,
        @Query(Constants.ID.ID_SHOP) idShop: Long,
        @Query(Constants.ID.ID_TRIP) idTrip: Long
    ): BaseResponse<RequestModel>
}