package ru.krymer.delivery.data.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import ru.krymer.delivery.data.model.FactoryModel
import ru.krymer.delivery.data.request.CreateFactoryRequest
import ru.krymer.delivery.data.request.UpdateFactoryRequest
import ru.krymer.delivery.data.response.BaseResponse
import ru.krymer.delivery.utills.Constants

interface FactoryApi {
    @POST("create-factory")
    suspend fun addFactory(@Body factory: CreateFactoryRequest): BaseResponse<FactoryModel>

    @GET("get-factory")
    suspend fun getById(@Query(Constants.ID.ID) idFactory: Long): BaseResponse<FactoryModel>

    @GET("get-factories")
    suspend fun getAllFactory(): BaseResponse<List<FactoryModel>>

    @POST("update-factory")
    suspend fun updateFactory(@Body factory: UpdateFactoryRequest): BaseResponse<FactoryModel>

    @DELETE("delete-factory")
    suspend fun deleteFactory(@Query(Constants.ID.ID) idFactory: Long): BaseResponse<FactoryModel>
}