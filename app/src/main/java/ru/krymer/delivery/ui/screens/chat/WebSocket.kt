package ru.krymer.delivery.ui.screens.chat

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import ru.krymer.delivery.data.model.user.MessageModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketManager @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson
) {
    private var webSocket: WebSocket? = null
    private val _messages = MutableSharedFlow<MessageModel>()
    val messages = _messages.asSharedFlow()

    fun connect(idUser: Long, token: String) {
        val request =
            Request.Builder().url("wss://monolit.containerapps.ru/api/v1/chat?id=$idUser").addHeader("Authorization", "Bearer $token")
                .build()

        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocket", "Открытие сессии $response")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val message = gson.fromJson(text, MessageModel::class.java)
                    Log.d("WebSocket", "Received: $message")
                    runBlocking {
                        _messages.emit(message)
                    }
                } catch (e: Exception) {
                    Log.e("WebSocket", "Ошибка отправки", e)
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "Закрытие сессии: $reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "Ошибка", t)
            }
        })
    }

    suspend fun sendMessage(message: MessageModel) {
        val json = gson.toJson(message)
        Log.d("WebSocket", "$json")

        webSocket?.send(json)
    }

    fun disconnect() {
        webSocket?.close(1000, "Пользователь вышел")
    }
}