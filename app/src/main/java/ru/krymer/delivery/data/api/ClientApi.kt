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
    @POST("create-client")
    suspend fun addClient(@Body client: ClientRequest): BaseResponse<ClientModel>

    @POST("update-client")
    suspend fun updateClient(@Body client: ClientRequest): BaseResponse<ClientModel>

    @DELETE("delete-client")
    suspend fun deleteClient(@Query(Constants.ID.ID) idClient: Long): BaseResponse<ClientModel>

    @GET("get-client")
    suspend fun getClientById(@Query(Constants.ID.ID) idClient: Long): BaseResponse<ClientModel?>

    @GET("get-clients")
    suspend fun getCurrentListClient(@Query(Constants.ID.ID_ROUTE) idRoute: Long): BaseResponse<List<ClientModel>>

    @GET("get-all-clients")
    suspend fun getAllCurrentListClient(@Query(Constants.ID.ID_FACTORY) idFactory: Long): BaseResponse<List<ClientModel>>
}