package ru.krymer.delivery.data.api

import retrofit2.http.GET
import retrofit2.http.Query
import ru.krymer.delivery.data.model.LoggerModel
import ru.krymer.delivery.data.response.BaseResponse
import ru.krymer.delivery.utills.Constants

interface LoggerApi {
    @GET("logs")
    suspend fun getLogs(@Query(Constants.ID.ID_FACTORY) idFactory: Long): BaseResponse<List<LoggerModel>>
}