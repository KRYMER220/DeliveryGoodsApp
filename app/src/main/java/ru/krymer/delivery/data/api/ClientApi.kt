package ru.krymer.delivery.data.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import ru.krymer.delivery.data.model.ClientModel
import ru.krymer.delivery.data.request.ClientRequest
import ru.krymer.delivery.data.response.BaseResponse
import ru.krymer.delivery.utills.Constants

interface ClientApi {
    @POST("client/create")
    suspend fun add(@Body client: ClientRequest): BaseResponse<ClientModel>

    @POST("client/update")
    suspend fun update(@Body client: ClientRequest): BaseResponse<ClientModel>

    @DELETE("client/delete")
    suspend fun delete(@Query(Constants.HttpRequestKeys.ID) id: Long): BaseResponse<ClientModel>

    @GET("client")
    suspend fun getClientById(@Query(Constants.HttpRequestKeys.ID) id: Long): BaseResponse<ClientModel?>

    @GET("clients/route")
    suspend fun getClientsByRoute(@Query(Constants.HttpRequestKeys.ID_ROUTE) idRoute: Long): BaseResponse<List<ClientModel>>

    @GET("clients/factory")
    suspend fun getClientsByFactory(@Query(Constants.HttpRequestKeys.ID_FACTORY) idFactory: Long): BaseResponse<List<ClientModel>>
}