package ru.krymer.delivery.data.repository

import ru.krymer.delivery.data.model.CourierInfoModel
import ru.krymer.delivery.data.model.TripModel
import ru.krymer.delivery.data.request.TripRequest
import ru.krymer.delivery.utills.MyResult

interface TripRepository {
    suspend fun add(trip: TripRequest): MyResult<TripModel?>

    suspend fun getRoutesByTrip(idFactory: Long): MyResult<List<TripModel>>

    suspend fun update(trip: TripRequest): MyResult<TripModel?>

    suspend fun delete(id: Long): MyResult<Unit>

    suspend fun getTrip(idTrip: Long): MyResult<TripModel?>

    suspend fun getDataAboutTrip(idFactory: Long, idTrip: Long
    ): MyResult<CourierInfoModel?>

    suspend fun getTrips(idFactory: Long, sortBy: String? = null, uid: Long? = null, routeId: Long? = null,
    ): MyResult<List<TripModel>>

    suspend fun getPaginatedTrips(idFactory: Long, limit: Int = 10, lastDate: Long?, lastId: Long?,
    ): MyResult<List<TripModel>>
}