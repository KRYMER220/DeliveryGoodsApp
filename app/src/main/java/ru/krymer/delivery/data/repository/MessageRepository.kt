package ru.krymer.delivery.data.repository

import ru.krymer.delivery.data.model.MessageModel
import ru.krymer.delivery.data.model.ShopServerModel
import ru.krymer.delivery.data.request.CreateMessage
import ru.krymer.delivery.utills.MyResult

interface MessageRepository {
    suspend fun add(message: CreateMessage): MyResult<MessageModel?>
    suspend fun delete(id: Long) : MyResult<Unit>
    suspend fun get(idClient: Long) : MyResult<List<MessageModel>>
}