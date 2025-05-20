package ru.krymer.delivery.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
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
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {
    private val refreshMutex = Mutex()
    private val refreshScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun intercept(chain: Chain): Response {
        val originalRequest = chain.request()

        val requestWithToken = originalRequest.withAuthToken(tokenManager.getAccessToken())

        val response = try {
            chain.proceed(requestWithToken)
        } catch (e: Exception) {
            throw e
        }

        if (response.code == 401) {
            refreshScope.launch {
                handleUnauthorizedError(chain, originalRequest)
            }
            response
        } else {
            response
        }

        return response
    }

    private suspend fun handleUnauthorizedError(
        chain: Chain, request: Request
    ): Response {
        if (!refreshMutex.tryLock()) {
            return chain.proceed(request)
        }

        return try {
            val newToken = withContext(Dispatchers.IO) { refreshAccessToken() }

            newToken?.let { token ->
                tokenManager.saveAccessToken(token)
                chain.proceedWithNewToken(request, token)
            } ?: run {
                tokenManager.deleteToken()
                createUnauthorizedResponse(request)
            }
        } finally {
            refreshMutex.unlock()
        }
    }

    private suspend fun refreshAccessToken(): String? {
        val accessToken = tokenManager.getAccessToken() ?: return null

        val retrofit =
            Retrofit.Builder().baseUrl(BASE_URL).client(OkHttpClient.Builder().build())
                .addConverterFactory(GsonConverterFactory.create()).build()

        val userApi = retrofit.create(UserApi::class.java)
        val response = userApi.refreshToken("Bearer $accessToken")

        return if (response.success) {
            response.obj?.accessToken?.also { token ->
                tokenManager.saveAccessToken(token)
            }
        } else {
            null
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

    private fun Chain.proceedWithNewToken(request: Request, token: String): Response {
        return proceed(
            request.newBuilder().header("Authorization", "Bearer $token").build()
        )
    }
}