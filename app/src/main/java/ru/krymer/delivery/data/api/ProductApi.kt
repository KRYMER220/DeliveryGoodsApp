package ru.krymer.delivery.data.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import ru.krymer.delivery.data.model.ClientModel
import ru.krymer.delivery.data.model.ProductModel
import ru.krymer.delivery.data.request.ProductRequest
import ru.krymer.delivery.data.response.BaseResponse
import ru.krymer.delivery.utills.Constants

interface ProductApi {
    @POST("product/create")
    suspend fun addProduct(@Body product: ProductRequest): BaseResponse<ProductModel>

    @POST("product/update")
    suspend fun updateProduct(@Body product: ProductRequest): BaseResponse<ProductModel>

    @POST("product/move")
    suspend fun moves(@Body requests: List<ProductRequest>): BaseResponse<ClientModel>

    @DELETE("product/delete")
    suspend fun deleteProduct(@Query(Constants.HttpRequestKeys.ID) id: Long): BaseResponse<ProductModel>

    @GET("products")
    suspend fun getProducts(@Query(Constants.HttpRequestKeys.ID_FACTORY) idFactory: Long): BaseResponse<List<ProductModel>>
}