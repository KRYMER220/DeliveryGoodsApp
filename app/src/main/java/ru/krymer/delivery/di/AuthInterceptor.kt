package ru.krymer.delivery.di

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.krymer.delivery.data.api.UserApi
import ru.krymer.delivery.utills.Constants
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val secureDataStore: SecureDataStore
) : Interceptor {
    private val tokenFlow = MutableStateFlow<String?>(null)

    init {
        runBlocking {
            tokenFlow.value = secureDataStore.getString(Constants.TOKEN.ACCESS)
        }
    }

    suspend fun updateToken(newToken: String?) {
        tokenFlow.value = newToken
        if (newToken != null) {
            secureDataStore.putString(Constants.TOKEN.ACCESS, newToken)
        } else {
            secureDataStore.clear()
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = tokenFlow.value
        val requestWithToken = originalRequest.withAuthToken(token)

        val initialResponse = chain.proceed(requestWithToken)

        if (initialResponse.code == 401) {
            initialResponse.close()

            return runBlocking {
                val newToken = tokenFlow.value
                if (newToken != requestWithToken.header("Authorization")?.removePrefix("Bearer ")) {
                    chain.proceed(originalRequest.withAuthToken(newToken))
                } else {
                    val refreshResult = refreshAccessToken()
                    if (refreshResult.isSuccess) {
                        val freshToken = refreshResult.getOrThrow()
                        updateToken(freshToken)
                        chain.proceed(originalRequest.withAuthToken(freshToken))
                    } else {
                        updateToken(null)
                        initialResponse
                    }
                }
            }
        }
        return initialResponse
    }

    private suspend fun refreshAccessToken(): Result<String> {
        val accessToken = tokenFlow.value
            ?: return Result.failure(Exception("No access token available"))

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
                    Result.success(token)
                } ?: Result.failure(Exception("No access token in response"))
            } else {
                Result.failure(Exception("Token refresh failed"))
            }
        } catch (e: Exception) {
            Log.e("AuthInterceptor", "Token refresh error: ${e.message}", e)
            Result.failure(e)
        }
    }

    private fun Request.withAuthToken(token: String?): Request {
        return if (token != null) {
            newBuilder().header("Authorization", "Bearer $token").build()
        } else {
            this
        }
    }
}