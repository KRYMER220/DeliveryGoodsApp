package ru.krymer.delivery.data.repositoryImpl

import ru.krymer.delivery.data.api.MessageApi
import ru.krymer.delivery.data.model.MessageModel
import ru.krymer.delivery.data.model.ShopServerModel
import ru.krymer.delivery.data.repository.MessageRepository
import ru.krymer.delivery.data.request.CreateMessage
import ru.krymer.delivery.utills.MyResult
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val messageApi: MessageApi
): MessageRepository {

    override suspend fun add(message: CreateMessage): MyResult<MessageModel?> {
        return try {
            val resp = messageApi.add(message = message)
            if (resp.success) MyResult.Success(resp.obj)
            else MyResult.Error(resp.message)
        } catch (t: Throwable) {
            MyResult.Error(t.message)
        }
    }

    override suspend fun delete(id: Long): MyResult<Unit> {
        return try {
            val resp = messageApi.delete(id = id)
            if (resp.success) MyResult.Success(Unit)
            else MyResult.Error(resp.message)
        } catch (t: Throwable) {
            MyResult.Error(t.message)
        }
    }

    override suspend fun get(idClient: Long): MyResult<List<MessageModel>> {
        return try {
            val resp = messageApi.getMessages(idClient = idClient)
            if (resp.success) MyResult.Success(resp.obj ?: emptyList())
            else MyResult.Error(resp.message)
        } catch (t: Throwable) {
            MyResult.Error(t.message)
        }
    }
}