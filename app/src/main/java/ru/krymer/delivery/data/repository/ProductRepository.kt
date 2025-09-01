package ru.krymer.delivery.data.repository

import ru.krymer.delivery.data.model.ProductModel
import ru.krymer.delivery.data.request.ProductRequest
import ru.krymer.delivery.utills.MyResult

interface ProductRepository {
    suspend fun addProduct(product: ProductRequest): MyResult<ProductModel?>

    suspend fun updateProduct(product: ProductRequest): MyResult<Unit>

    suspend fun moves(products: List<ProductRequest>): MyResult<Unit>

    suspend fun deleteProduct(id: Long): MyResult<Unit>

    suspend fun getProducts(idFactory: Long): MyResult<List<ProductModel>>
}