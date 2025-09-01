package ru.krymer.delivery.data.repository

import ru.krymer.delivery.data.model.ClientModel
import ru.krymer.delivery.data.model.RouteModel
import ru.krymer.delivery.data.request.ClientRequest
import ru.krymer.delivery.utills.MyResult

interface ClientRepository {
    suspend fun getClients(idRoute: Long): MyResult<List<ClientModel>>
    suspend fun addClient(request: ClientRequest): MyResult<ClientModel?>
    suspend fun updateClient(request: ClientRequest): MyResult<Unit>
    suspend fun deleteClient(id: Long): MyResult<Unit>
    suspend fun moves(clients: List<ClientRequest>): MyResult<Unit>
    suspend fun getClientById(id: Long): MyResult<ClientModel?>
    suspend fun getClientsByFactory(idFactory: Long): MyResult<List<ClientModel>>
}