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
    @POST("log/create")
    suspend fun add(@Body log: LogRequest)

    @GET("logs")
    suspend fun getLogs(@Query(Constants.ID.ID_FACTORY) idFactory: Long): BaseResponse<List<LoggerModel>>
}