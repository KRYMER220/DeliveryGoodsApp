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
    @POST("route/create")
    suspend fun add(@Body route: RouteRequest): BaseResponse<RouteModel>

    @POST("route/update")
    suspend fun update(@Body route: RouteRequest): BaseResponse<RouteModel>

    @DELETE("route/delete")
    suspend fun delete(@Query(Constants.HttpRequestKeys.ID) id: Long): BaseResponse<RouteModel>

    @GET("routes")
    suspend fun getRoutes(@Query(Constants.HttpRequestKeys.ID_FACTORY) idFactory: Long): BaseResponse<List<RouteModel>>
}