package ru.krymer.delivery.di

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.krymer.delivery.AppDatabase
import ru.krymer.delivery.data.api.UserApi
import ru.krymer.delivery.data.model.FailedRequest
import java.net.SocketTimeoutException
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
    private val appDatabase: AppDatabase,
    private val gson: Gson
) : Interceptor {
    private val refreshMutex = Mutex()

    override fun intercept(chain: Chain): Response {
        val originalRequest = chain.request()
        val requestWithToken = originalRequest.withAuthToken(tokenManager.getAccessToken())

        val initialResponse = try {
            chain.proceed(requestWithToken)
        } catch (e: SocketTimeoutException) {
            Log.e("AuthInterceptor", "Timeout: ${e.message}", e)
            saveFailedRequestAsync(originalRequest, "Timeout")
            return createTimeoutResponse(originalRequest)
        } catch (e: Exception) {
            Log.e("AuthInterceptor", "Request failed: ${e.message}", e)
            saveFailedRequestAsync(originalRequest, e.message ?: "Unknown error")
            throw e
        }

        if (initialResponse.code == 401) {
            initialResponse.close()

            return runBlocking(Dispatchers.IO) {
                refreshMutex.withLock {
                    val newToken = tokenManager.getAccessToken()
                    if (newToken != requestWithToken.header("Authorization")
                            ?.removePrefix("Bearer ")
                    ) {
                        chain.proceed(originalRequest.withAuthToken(newToken))
                    } else {
                        val refreshResult = refreshAccessToken()
                        if (refreshResult.isSuccess) {
                            val freshToken = refreshResult.getOrThrow()
                            tokenManager.saveAccessToken(freshToken)
                            chain.proceed(originalRequest.withAuthToken(freshToken))
                        } else {
                            tokenManager.deleteToken()
                            createUnauthorizedResponse(originalRequest)
                        }
                    }
                }
            }
        }
        return initialResponse
    }

    private fun saveFailedRequestAsync(request: Request, error: String) {
        CoroutineScope(Dispatchers.IO).launch {
            saveFailedRequest(request, error)
        }
    }


    private suspend fun refreshAccessToken(): Result<String> {
        val accessToken = tokenManager.getAccessToken() ?: return Result.failure(Exception("No access token available").also {
            Log.e("AuthInterceptor", "No access token available")
        })

        return try {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(OkHttpClient.Builder().build())
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val userApi = retrofit.create(UserApi::class.java)
            val response = userApi.refreshToken("Bearer $accessToken")

            if (response.success) {
                response.obj?.accessToken?.let { token ->
                    tokenManager.saveAccessToken(token)
                    Result.success(token)
                } ?: Result.failure(Exception("No access token in response").also {
                    Log.e("AuthInterceptor", "No access token in response")
                })
            } else {
                Result.failure(Exception("Token refresh failed").also {
                    Log.e("AuthInterceptor", "Token refresh failed")
                })
            }
        } catch (e: SocketTimeoutException) {
            Log.e("AuthInterceptor", "Token refresh timed out: ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e("AuthInterceptor", "Token refresh error: ${e.message}", e)
            Result.failure(e)
        }
    }

    private suspend fun saveFailedRequest(request: Request?, errorMessage: String) {
        val failedRequest = FailedRequest(
            apiType = determineApiType(request?.url?.toString() ?: "refresh_token"),
            endpoint = determineEndpoint(request?.url?.toString() ?: "refresh_token"),
            method = request?.method ?: "POST",
            url = request?.url?.toString() ?: "refresh_token",
            params = gson.toJson(extractParams(request)),
            bodyJson = request?.body?.let { body ->
                try {
                    val buffer = Buffer()
                    body.writeTo(buffer)
                    buffer.readUtf8()
                } catch (e: Exception) {
                    null
                }
            },
            errorMessage = errorMessage
        )
        appDatabase.failedDao().insert(failedRequest)
    }

    private fun extractParams(request: Request?): Map<String, String> {
        val params = mutableMapOf<String, String>()
        request?.url?.queryParameterNames?.forEach { name ->
            request.url.queryParameter(name)?.let { value ->
                params[name] = value
            }
        }
        return params
    }

    private fun determineEndpoint(url: String): String {
        return url.substringAfterLast("/").substringBefore("?")
    }

    private fun determineApiType(url: String): String {
        return when {
            url.contains("/request") -> "request"
            url.contains("/shop") -> "shop"
            else -> "unknown"
        }
    }

    private fun createTimeoutResponse(request: Request): Response {
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(504)
            .message("Request Timeout")
            .body("{\"error\":\"Request timed out\"}".toResponseBody("application/json".toMediaType()))
            .build()
    }

    private fun createUnauthorizedResponse(request: Request): Response {
        return Response.Builder().request(request).protocol(Protocol.HTTP_1_1).code(401)
            .message("Unauthorized")
            .body("{\"error\":\"Authentication required\"}".toResponseBody("application/json".toMediaType()))
            .build()
    }

    private fun Request.withAuthToken(token: String?): Request {
        return if (token != null) {
            this.newBuilder().header("Authorization", "Bearer $token").build()
        } else {
            this
        }
    }
}