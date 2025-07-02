package ru.krymer.delivery.data.api

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import ru.krymer.delivery.data.request.DateRequest
import ru.krymer.delivery.data.response.Analitic
import ru.krymer.delivery.data.response.BaseResponse
import ru.krymer.delivery.utills.Constants

interface AnaliticApi {
    @POST("analitic/trip/dateRange")
    suspend fun getDataTripOfRange(@Body dateRange: DateRequest): BaseResponse<Analitic>

    @POST("analitic/client/dateRange")
    suspend fun getDataClientOfRange(@Body dateRange: DateRequest): BaseResponse<Analitic>

    @POST("analitic/factory/dateRange")
    suspend fun getDataFactoryOfRange(@Body dateRange: DateRequest): BaseResponse<Analitic>

    @POST("analitic/client")
    suspend fun getClientAnalitic(@Query(Constants.HttpRequestKeys.ID) idClient: Long): BaseResponse<Analitic>

    @POST("analitic/trip")
    suspend fun getTripAnalitic(@Query(Constants.HttpRequestKeys.ID_ROUTE) idRoute: Long): BaseResponse<Analitic>
}