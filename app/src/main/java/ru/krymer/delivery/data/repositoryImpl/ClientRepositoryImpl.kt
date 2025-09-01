package ru.krymer.delivery.data.repositoryImpl

import ru.krymer.delivery.data.api.ClientApi
import ru.krymer.delivery.data.model.ClientModel
import ru.krymer.delivery.data.repository.ClientRepository
import ru.krymer.delivery.data.request.ClientRequest
import ru.krymer.delivery.utills.MyResult
import javax.inject.Inject

class ClientRepositoryImpl @Inject constructor(
    private val clientApi: ClientApi
) : ClientRepository {

    override suspend fun getClients(idRoute: Long): MyResult<List<ClientModel>> {
        return try {
            val resp = clientApi.getClientsByRoute(idRoute = idRoute)
            if (resp.success) MyResult.Success(resp.obj ?: emptyList())
            else MyResult.Error(resp.message)
        } catch (t: Throwable) {
            MyResult.Error(t.message)
        }
    }

    override suspend fun addClient(request: ClientRequest): MyResult<ClientModel?> {
        return try {
            val resp = clientApi.add(request = request)
            if (resp.success) MyResult.Success(resp.obj)
            else MyResult.Error(resp.message)
        } catch (t: Throwable) {
            MyResult.Error(t.message)
        }
    }

    override suspend fun updateClient(request: ClientRequest): MyResult<Unit> {
        return try {
            val resp = clientApi.update(request = request)
            if (resp.success) MyResult.Success(Unit)
            else MyResult.Error(resp.message)
        } catch (t: Throwable) {
            MyResult.Error(t.message)
        }
    }

    override suspend fun deleteClient(id: Long): MyResult<Unit> {
        return try {
            val resp = clientApi.delete(id = id)
            if (resp.success) MyResult.Success(Unit)
            else MyResult.Error(resp.message)
        } catch (t: Throwable) {
            MyResult.Error(t.message)
        }
    }

    override suspend fun moves(requests: List<ClientRequest>): MyResult<Unit> {
        return try {
            val resp = clientApi.moves(requests = requests)
            if (resp.success) MyResult.Success(Unit)
            else MyResult.Error(resp.message)
        } catch (t: Throwable) {
            MyResult.Error(t.message)
        }
    }

    override suspend fun getClientById(id: Long): MyResult<ClientModel?> {
        return try {
            val resp = clientApi.getClientById(id = id)
            if (resp.success) MyResult.Success(resp.obj)
            else MyResult.Error(resp.message)
        } catch (t: Throwable) {
            MyResult.Error(t.message)
        }
    }

    override suspend fun getClientsByFactory(idFactory: Long): MyResult<List<ClientModel>> {
        return try {
            val resp = clientApi.getClientsByFactory(idFactory = idFactory)
            if (resp.success) MyResult.Success(resp.obj ?: emptyList())
            else MyResult.Error(resp.message)
        } catch (t: Throwable) {
            MyResult.Error(t.message)
        }
    }
}