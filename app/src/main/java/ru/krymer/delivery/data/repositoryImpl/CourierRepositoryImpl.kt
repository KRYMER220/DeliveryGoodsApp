package ru.krymer.delivery.data.repositoryImpl

import ru.krymer.delivery.data.api.FactoryApi
import ru.krymer.delivery.data.api.UserApi
import ru.krymer.delivery.data.model.user.UserModel
import ru.krymer.delivery.data.repository.CourierRepository
import ru.krymer.delivery.data.request.FactoryRequest
import ru.krymer.delivery.data.request.SignUpRequest
import ru.krymer.delivery.data.request.UserRequest
import ru.krymer.delivery.utills.MyResult
import javax.inject.Inject

class CourierRepositoryImpl @Inject constructor(
    private val factoryApi: FactoryApi,
    private val userApi: UserApi
) : CourierRepository {

    override suspend fun updateFactory(factory: FactoryRequest): MyResult<Unit> {
        return try {
            val resp = factoryApi.update(factory = factory)
            if (resp.success) MyResult.Success(Unit) else MyResult.Error(resp.message)
        } catch (t: Throwable) {
            MyResult.Error(t.message)
        }
    }

    override suspend fun signUp(request: SignUpRequest): MyResult<UserModel?> {
        return try {
            val resp = userApi.signUp(request = request)
            if (resp.success) MyResult.Success(resp.obj) else MyResult.Error(resp.message)
        } catch (t: Throwable) {
            MyResult.Error(t.message)
        }
    }

    override suspend fun getUsers(idFactory: Long): MyResult<List<UserModel>> {
        return try {
            val resp = userApi.getUsers(idFactory = idFactory)
            if (resp.success) MyResult.Success(
                resp.obj ?: emptyList()
            ) else MyResult.Error(resp.message)
        } catch (t: Throwable) {
            MyResult.Error(t.message)
        }
    }

    override suspend fun update(request: UserRequest): MyResult<Unit> {
        return try {
            val resp = userApi.update(request = request)
            if (resp.success) MyResult.Success(Unit) else MyResult.Error(resp.message)
        } catch (t: Throwable) {
            MyResult.Error(t.message)
        }
    }

    override suspend fun delete(id: Long): MyResult<Unit> {
        return try {
            val resp = userApi.delete(id = id)
            if (resp.success) MyResult.Success(Unit) else MyResult.Error(resp.message)
        } catch (t: Throwable) {
            MyResult.Error(t.message)
        }
    }
}