package ru.krymer.delivery.data.repository

import ru.krymer.delivery.data.model.user.UserModel
import ru.krymer.delivery.data.request.FactoryRequest
import ru.krymer.delivery.data.request.SignUpRequest
import ru.krymer.delivery.data.request.UserRequest
import ru.krymer.delivery.utills.MyResult

interface CourierRepository {
    suspend fun updateFactory(factory: FactoryRequest): MyResult<Unit>
    suspend fun signUp(request: SignUpRequest): MyResult<UserModel?>
    suspend fun getUsers(idFactory: Long): MyResult<List<UserModel>>
    suspend fun update(request: UserRequest): MyResult<Unit>
    suspend fun delete(id: Long): MyResult<Unit>
}