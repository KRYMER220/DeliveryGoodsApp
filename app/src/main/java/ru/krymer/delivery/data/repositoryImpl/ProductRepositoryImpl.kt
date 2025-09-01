package ru.krymer.delivery.data.repositoryImpl

import ru.krymer.delivery.data.api.ProductApi
import ru.krymer.delivery.data.model.ProductModel
import ru.krymer.delivery.data.repository.ProductRepository
import ru.krymer.delivery.data.request.ProductRequest
import ru.krymer.delivery.utills.MyResult
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val productApi: ProductApi
) : ProductRepository {

    override suspend fun addProduct(product: ProductRequest): MyResult<ProductModel?> {
        return try {
            val resp = productApi.addProduct(product)
            if (resp.success) MyResult.Success(resp.obj)
            else MyResult.Error(resp.message)
        } catch (t: Throwable) {
            MyResult.Error(t.message)
        }
    }

    override suspend fun updateProduct(product: ProductRequest): MyResult<Unit> {
        return try {
            val resp = productApi.updateProduct(product = product)
            if (resp.success) MyResult.Success(Unit)
            else MyResult.Error(resp.message)
        } catch (t: Throwable) {
            MyResult.Error(t.message)
        }
    }

    override suspend fun moves(requests: List<ProductRequest>): MyResult<Unit> {
        return try {
            val resp = productApi.moves(requests = requests)
            if (resp.success) MyResult.Success(Unit)
            else MyResult.Error(resp.message)
        } catch (t: Throwable) {
            MyResult.Error(t.message)
        }
    }

    override suspend fun deleteProduct(id: Long): MyResult<Unit> {
        return try {
            val resp = productApi.deleteProduct(id = id)
            if (resp.success) MyResult.Success(Unit)
            else MyResult.Error(resp.message)
        } catch (t: Throwable) {
            MyResult.Error(t.message)
        }
    }

    override suspend fun getProducts(idFactory: Long): MyResult<List<ProductModel>> {
        return try {
            val resp = productApi.getProducts(idFactory = idFactory)
            if (resp.success) MyResult.Success(resp.obj ?: emptyList())
            else MyResult.Error(resp.message)
        } catch (t: Throwable) {
            MyResult.Error(t.message)
        }
    }
}