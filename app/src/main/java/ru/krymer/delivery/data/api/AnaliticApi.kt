package ru.krymer.delivery.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import ru.krymer.delivery.data.request.DateRequest
import ru.krymer.delivery.data.response.BaseResponse
import ru.krymer.delivery.data.response.Client
import ru.krymer.delivery.data.response.Factory
import ru.krymer.delivery.data.response.Trip
import ru.krymer.delivery.utills.Constants

interface AnaliticApi {
    @POST("get-factory-data")
    suspend fun getDataFactoryOfRange(@Body dateRange: DateRequest): BaseResponse<Factory>

    @POST("get-client-data")
    suspend fun getClientAnalitic(@Query(Constants.ID.ID) idClient: Long): BaseResponse<Client>

    @POST("get-trip-data")
    suspend fun getTripAnalitic(@Query(Constants.ID.ID_ROUTE) idRoute: Long): BaseResponse<Trip>
}