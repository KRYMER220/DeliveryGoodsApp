package ru.krymer.delivery.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import ru.krymer.delivery.data.model.LoggerModel
import ru.krymer.delivery.data.request.LogRequest
import ru.krymer.delivery.data.response.BaseResponse
import ru.krymer.delivery.utills.Constants

interface LoggerApi {
    @POST("create-log")
    suspend fun addLog(@Body log: LogRequest)

    @GET("get-current-logs")
    suspend fun getAllFactoryLogs(@Query(Constants.ID.ID_FACTORY) idFactory: Long): BaseResponse<List<LoggerModel>>
}