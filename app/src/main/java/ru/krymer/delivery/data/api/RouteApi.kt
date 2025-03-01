package ru.krymer.delivery.data.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import ru.krymer.delivery.data.model.RouteModel
import ru.krymer.delivery.data.request.RouteRequest
import ru.krymer.delivery.data.response.BaseResponse
import ru.krymer.delivery.utills.Constants

interface RouteApi {
    @POST("create-route")
    suspend fun addRoute(@Body route: RouteRequest): BaseResponse<RouteModel>

    @POST("update-route")
    suspend fun updateRoute(@Body route: RouteRequest): BaseResponse<RouteModel>

    @DELETE("delete-route")
    suspend fun deleteRoute(@Query(Constants.ID.ID) idRoute: Long): BaseResponse<RouteModel>

    @GET("get-routes")
    suspend fun getCurrentListRoute(@Query(Constants.ID.ID_FACTORY) idFactory: Long): BaseResponse<List<RouteModel>>

    @GET("get-route")
    suspend fun getCurrentRoute(@Query(Constants.ID.ID_ROUTE) idRoute: Long): BaseResponse<RouteModel>
}