package ru.krymer.delivery.data.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import ru.krymer.delivery.data.model.LoggerModel
import ru.krymer.delivery.data.request.DateRequest
import ru.krymer.delivery.data.response.BaseResponse
import ru.krymer.delivery.utills.Constants

interface LoggerApi {
    @GET("logs")
    suspend fun getLogs(@Query(Constants.HttpRequestKeys.ID_FACTORY) idFactory: Long): BaseResponse<List<LoggerModel>>

    @POST("logs/ofRange")
    suspend fun getLogsOfRange(@Body dateRange: DateRequest): BaseResponse<List<LoggerModel>>

    @DELETE("logs/delete")
    suspend fun deleteLogs(@Query(Constants.HttpRequestKeys.ID_FACTORY) idFactory: Long): BaseResponse<LoggerModel>
}