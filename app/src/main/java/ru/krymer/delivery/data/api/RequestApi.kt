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
    @POST("create-request")
    suspend fun addRequest(@Body request: CreateRequestShopRequest): BaseResponse<RequestModel>

    @GET("get-all-requests")
    suspend fun getAllRequests(@Query(Constants.ID.ID_FACTORY) idFactory: Long): BaseResponse<List<RequestModel>>

    @GET("get-current-requests")
    suspend fun getCurrentShopRequests(
        @Query(Constants.ID.ID_FACTORY) idFactory: Long,
        @Query(Constants.ID.ID_TRIP) idTrip: Long,
        @Query(Constants.ID.ID_SHOP) idShop: Long
    ): BaseResponse<List<RequestModel>>

    @GET("get-current-trip-requests")
    suspend fun getCurrentTripRequests(
        @Query(Constants.ID.ID_FACTORY) idFactory: Long,
        @Query(Constants.ID.ID_TRIP) idTrip: Long,
    ): BaseResponse<List<RequestModel>>

    @POST("update-request")
    suspend fun updateRequest(@Body request: UpdateRequestShopRequest): BaseResponse<RequestModel>

    @DELETE("delete-request")
    suspend fun deleteRequest(
        @Query(Constants.ID.ID) id: Long,
        @Query(Constants.ID.ID_SHOP) idShop: Long,
        @Query(Constants.ID.ID_TRIP) idTrip: Long
    ): BaseResponse<RequestModel>

    suspend fun findRequest(idRequest: Long, idTrip: Long, idShop: Long): BaseResponse<RequestModel>
}