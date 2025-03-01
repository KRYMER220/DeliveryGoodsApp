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
    @POST("sign-up")
    suspend fun signUpUser(@Body request: SignUpRequest): BaseResponse<UserModel>

    @POST("login")
    suspend fun signInUser(@Body request: SignInRequest): BaseResponse<TokenResponse>

    @GET("token-refresh")
    suspend fun refreshAccessToken(@Header("Authorization") token: String): BaseResponse<TokenResponse>

    @GET("get-user-info")
    suspend fun getUserData(): BaseResponse<UserModel>

    @DELETE("logout")
    suspend fun logout(): BaseResponse<UserModel>

    @DELETE("delete-user")
    suspend fun deleteUser(@Query(Constants.ID.ID) idUser: Long): BaseResponse<UserModel>

    @POST("update-user")
    suspend fun updateUser(@Body request: UpdateUserRequest): BaseResponse<UserModel>

    @GET("get-list-user")
    suspend fun getListUser(@Query(Constants.ID.ID_FACTORY) idFactory: Long): BaseResponse<List<UserModel>>
}