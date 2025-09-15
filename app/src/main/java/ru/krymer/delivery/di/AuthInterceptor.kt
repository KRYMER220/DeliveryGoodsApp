package ru.krymer.delivery.di

import android.util.Log
import kotlinx.coroutines.Dispatchers
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
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.krymer.delivery.data.api.UserApi
import java.net.SocketTimeoutException
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
) : Interceptor {
    private val refreshMutex = Mutex()

    override fun intercept(chain: Chain): Response {
        val originalRequest = chain.request()
        val requestWithToken = originalRequest.withAuthToken(tokenManager.getAccessToken())

        val initialResponse = try {
            chain.proceed(requestWithToken)
        } catch (e: Exception) {
            Log.e("AuthInterceptor", "Request failed: ${e.message}", e)
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
                            createUnauthorizedResponse(originalRequest)
                        }
                    }
                }
            }
        }
        return initialResponse
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