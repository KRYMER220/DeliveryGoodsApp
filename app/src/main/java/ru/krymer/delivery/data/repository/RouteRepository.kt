package ru.krymer.delivery.data.repository

import ru.krymer.delivery.data.model.RouteModel
import ru.krymer.delivery.data.request.RouteRequest
import ru.krymer.delivery.utills.MyResult

interface RouteRepository {
    suspend fun getRoutes(idFactory: Long): MyResult<List<RouteModel>>
    suspend fun addRoute(request: RouteRequest): MyResult<RouteModel?>
    suspend fun updateRoute(request: RouteRequest): MyResult<Unit>
    suspend fun deleteRoute(id: Long): MyResult<Unit>
}