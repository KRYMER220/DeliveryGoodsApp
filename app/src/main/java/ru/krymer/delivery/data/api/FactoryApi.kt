package ru.krymer.delivery.data.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import ru.krymer.delivery.data.model.FactoryModel
import ru.krymer.delivery.data.request.FactoryRequest
import ru.krymer.delivery.data.response.BaseResponse
import ru.krymer.delivery.utills.Constants

interface FactoryApi {
    @GET("factory")
    suspend fun getFactoryById(@Query(Constants.HttpRequestKeys.ID) id: Long): BaseResponse<FactoryModel>

    @POST("factory/update")
    suspend fun update(@Body factory: FactoryRequest): BaseResponse<FactoryModel>
}