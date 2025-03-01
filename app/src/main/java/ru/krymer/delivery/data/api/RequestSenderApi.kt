package ru.krymer.delivery.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Url
import ru.krymer.delivery.data.response.ApiResponse

interface RequestSenderApi {
    @GET
    suspend fun getRequest(@Url url: String): Response<ApiResponse>

    @POST
    suspend fun postRequest(@Url url: String, @Body body: Any): Response<ApiResponse>
}