package ru.krymer.delivery.data.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import ru.krymer.delivery.data.model.ProductModel
import ru.krymer.delivery.data.request.CreateProductRequest
import ru.krymer.delivery.data.request.UpdateProductRequest
import ru.krymer.delivery.data.response.BaseResponse
import ru.krymer.delivery.utills.Constants

interface ProductApi {
    @POST("create-product")
    suspend fun addProduct(@Body product: CreateProductRequest): BaseResponse<ProductModel>

    @POST("update-product")
    suspend fun updateProduct(@Body product: UpdateProductRequest): BaseResponse<ProductModel>

    @DELETE("delete-product")
    suspend fun deleteProduct(@Query(Constants.ID.ID) idProduct: Long): BaseResponse<ProductModel>

    @GET("get-products")
    suspend fun getCurrentListProduct(@Query(Constants.ID.ID_FACTORY) idFactory: Long): BaseResponse<List<ProductModel>>
}