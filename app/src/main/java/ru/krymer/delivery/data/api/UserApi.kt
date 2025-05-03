package ru.krymer.delivery.data.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import ru.krymer.delivery.data.model.user.UserModel
import ru.krymer.delivery.data.request.SignInRequest
import ru.krymer.delivery.data.request.SignUpRequest
import ru.krymer.delivery.data.request.UpdateUserRequest
import ru.krymer.delivery.data.response.BaseResponse
import ru.krymer.delivery.data.response.TokenResponse
import ru.krymer.delivery.utills.Constants

interface UserApi {
    @POST("user/sign/up")
    suspend fun signUp(@Body request: SignUpRequest): BaseResponse<UserModel>

    @POST("user/sign/in")
    suspend fun signIn(@Body request: SignInRequest): BaseResponse<TokenResponse>

    @GET("token/refresh")
    suspend fun refreshToken(@Header("Authorization") token: String): BaseResponse<TokenResponse>

    @GET("user/info")
    suspend fun getData(): BaseResponse<UserModel>

    @DELETE("user/logout")
    suspend fun logout(): BaseResponse<UserModel>

    @DELETE("user/delete")
    suspend fun delete(@Query(Constants.ID.ID) id: Long): BaseResponse<UserModel>

    @POST("user/update")
    suspend fun update(@Body request: UpdateUserRequest): BaseResponse<UserModel>

    @GET("users")
    suspend fun getUsers(@Query(Constants.ID.ID_FACTORY) idFactory: Long): BaseResponse<List<UserModel>>
}