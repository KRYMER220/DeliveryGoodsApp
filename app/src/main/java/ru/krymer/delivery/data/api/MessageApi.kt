package ru.krymer.delivery.data.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import ru.krymer.delivery.data.model.MessageModel
import ru.krymer.delivery.data.request.CreateMessage
import ru.krymer.delivery.data.response.BaseResponse
import ru.krymer.delivery.utills.Constants

interface MessageApi {
    @POST("message/create")
    suspend fun add(@Body message: CreateMessage): BaseResponse<MessageModel>

    @DELETE("message/delete")
    suspend fun delete(@Query(Constants.HttpRequestKeys.ID) id: Long): BaseResponse<MessageModel>

    @GET("messages")
    suspend fun getMessages(@Query(Constants.HttpRequestKeys.ID_CLIENT) idClient: Long): BaseResponse<List<MessageModel>>
}