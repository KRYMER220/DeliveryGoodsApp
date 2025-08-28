package ru.krymer.delivery.data.repositoryImpl

import ru.krymer.delivery.data.api.RouteApi
import ru.krymer.delivery.data.model.RouteModel
import ru.krymer.delivery.data.repository.RouteRepository
import ru.krymer.delivery.data.request.RouteRequest
import ru.krymer.delivery.utills.MyResult
import javax.inject.Inject

class RouteRepositoryImpl @Inject constructor(
    private val routeApi: RouteApi
) : RouteRepository {

    override suspend fun getRoutes(idFactory: Long): MyResult<List<RouteModel>> {
        return try {
            val resp = routeApi.getRoutes(idFactory = idFactory)
            if (resp.success) MyResult.Success(resp.obj ?: emptyList())
            else MyResult.Error(resp.message)
        } catch (t: Throwable) {
            MyResult.Error(t.message)
        }
    }

    override suspend fun addRoute(request: RouteRequest): MyResult<RouteModel?> {
        return try {
            val resp = routeApi.add(route = request)
            if (resp.success) MyResult.Success(resp.obj)
            else MyResult.Error(resp.message)
        } catch (t: Throwable) {
            MyResult.Error(t.message)
        }
    }

    override suspend fun updateRoute(request: RouteRequest): MyResult<Unit> {
        return try {
            val resp = routeApi.update(route = request)
            if (resp.success) MyResult.Success(Unit)
            else MyResult.Error(resp.message)
        } catch (t: Throwable) {
            MyResult.Error(t.message)
        }
    }

    override suspend fun deleteRoute(id: Long): MyResult<Unit> {
        return try {
            val resp = routeApi.delete(id = id)
            if (resp.success) MyResult.Success(Unit)
            else MyResult.Error(resp.message)
        } catch (t: Throwable) {
            MyResult.Error(t.message)
        }
    }
}