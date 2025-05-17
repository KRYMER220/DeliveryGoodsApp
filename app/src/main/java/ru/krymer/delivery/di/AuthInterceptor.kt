package ru.krymer.delivery.di

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.krymer.delivery.data.api.UserApi
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val token = tokenManager.getAccessToken()
        val requestWithToken = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        val response: Response
        try {
            response = chain.proceed(requestWithToken)
        } catch (e: Exception) {
            throw e
        }

        if (response.code == 401) {
            synchronized(this) {
                val newToken = runBlocking { refreshAccessToken() }
                if (newToken != null) {
                    val newRequest = originalRequest.newBuilder()
                        .header("Authorization", "Bearer $newToken")
                        .build()
                    response.close()
                    return chain.proceed(newRequest)
                }
            }
        }

        return response
    }

    private suspend fun refreshAccessToken(): String? {
        return try {
            val accessToken = tokenManager.getAccessToken()
            if (accessToken != null) {
                val retrofit = Retrofit.Builder().baseUrl(BASE_URL)
                    .client(OkHttpClient.Builder().build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val userApi = retrofit.create(UserApi::class.java)
                val response = userApi.refreshToken("Bearer $accessToken")
                if (response.success) {
                    val newToken = response.obj?.accessToken
                    if (newToken != null) {
                        newToken.let { tokenManager.saveAccessToken(it) }
                        return newToken
                    } else {
                        null
                    }
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
