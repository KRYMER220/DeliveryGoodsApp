package ru.krymer.delivery.di

import android.util.Log
import com.google.gson.Gson
import ru.krymer.delivery.AppDatabase
import ru.krymer.delivery.data.api.RequestApi
import ru.krymer.delivery.data.api.ShopApi
import ru.krymer.delivery.data.model.FailedRequest
import ru.krymer.delivery.data.request.CreateRequestShopRequest
import ru.krymer.delivery.data.request.CreateShopRequest
import ru.krymer.delivery.data.request.UpdateRequestShopRequest
import ru.krymer.delivery.data.request.UpdateShopRequest
import ru.krymer.delivery.data.response.BaseResponse
import javax.inject.Inject

class RetryManager @Inject constructor(
    private val appDatabase: AppDatabase,
    private val requestApi: RequestApi,
    private val shopApi: ShopApi,
    private val gson: Gson
) {

    suspend fun retryFailedRequests() {
        val failedRequests = appDatabase.failedDao().getAllFailedRequests().sortedBy { it.timestamp }
        failedRequests.forEach { failedRequest ->
            Log.d("Debag", "$failedRequest")
            try {
                if (failedRequest.retryCount >= 3) {
                    appDatabase.failedDao().deleteById(failedRequest.id)
                    return@forEach
                }
                val params =
                    gson.fromJson(failedRequest.params, Map::class.java) as Map<String, String>
                val response = when (failedRequest.apiType) {
                    "request" -> retryRequestRequest(failedRequest, params)
                    "shop" -> retryShopRequest(failedRequest, params)
                    else -> {
                        appDatabase.failedDao().deleteById(failedRequest.id)
                        throw UnsupportedOperationException("Unknown apiType: ${failedRequest.apiType}")
                    }
                }
                if (response.success) {
                    appDatabase.failedDao().deleteById(failedRequest.id)
                } else {
                    println("Retry failed for ${failedRequest.url}: ${response.message}")
                    appDatabase.failedDao().update(failedRequest.copy(retryCount = failedRequest.retryCount + 1))
                }
            } catch (e: Exception) {
                appDatabase.failedDao().update(failedRequest.copy(retryCount = failedRequest.retryCount + 1))
                println("Retry failed for ${failedRequest.url}: ${e.message}")
            }
        }
    }

    private suspend fun retryRequestRequest(
        failedRequest: FailedRequest,
        params: Map<String, String>
    ): BaseResponse<*> {
        return when (failedRequest.endpoint) {
            "create" -> requestApi.add(
                gson.fromJson(
                    failedRequest.bodyJson,
                    CreateRequestShopRequest::class.java
                )
            )

            "get/trip" -> requestApi.getRequestsByTrip(
                params["idFactory"]?.toLong()
                    ?: throw IllegalArgumentException("Missing idFactory"),
                params["idTrip"]?.toLong() ?: throw IllegalArgumentException("Missing idTrip")
            )

            "update" -> requestApi.update(
                gson.fromJson(
                    failedRequest.bodyJson,
                    UpdateRequestShopRequest::class.java
                )
            )

            "delete" -> requestApi.delete(
                params["id"]?.toLong() ?: throw IllegalArgumentException("Missing id"),
                params["idShop"]?.toLong() ?: throw IllegalArgumentException("Missing idShop"),
                params["idTrip"]?.toLong() ?: throw IllegalArgumentException("Missing idTrip")
            )

            else -> throw UnsupportedOperationException("Unknown endpoint: ${failedRequest.endpoint}")
        }
    }

    private suspend fun retryShopRequest(
        failedRequest: FailedRequest,
        params: Map<String, String>
    ): BaseResponse<*> {
        return when (failedRequest.endpoint) {
            "create" -> shopApi.add(
                gson.fromJson(
                    failedRequest.bodyJson,
                    CreateShopRequest::class.java
                )
            )

            "by/trip" -> shopApi.getShopsByTrip(
                params["idTrip"]?.toLong() ?: throw IllegalArgumentException("Missing idTrip")
            )

            "by/factory" -> shopApi.getShops(
                params["idFactory"]?.toLong() ?: throw IllegalArgumentException("Missing idFactory")
            )

            "update" -> shopApi.update(
                gson.fromJson(
                    failedRequest.bodyJson,
                    UpdateShopRequest::class.java
                )
            )

            "delete" -> shopApi.delete(
                params["id"]?.toLong() ?: throw IllegalArgumentException("Missing id"),
                params["idTrip"]?.toLong() ?: throw IllegalArgumentException("Missing idTrip")
            )

            "for/info" -> shopApi.getCurrentShopsByFactory(
                params["idFactory"]?.toLong()
                    ?: throw IllegalArgumentException("Missing idFactory"),
                params["id"]?.toLong() ?: throw IllegalArgumentException("Missing id")
            )

            else -> throw UnsupportedOperationException("Unknown endpoint: ${failedRequest.endpoint}")
        }
    }
}