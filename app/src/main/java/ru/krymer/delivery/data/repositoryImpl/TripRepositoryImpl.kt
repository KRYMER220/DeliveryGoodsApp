package ru.krymer.delivery.data.repositoryImpl

import ru.krymer.delivery.data.api.TripApi
import ru.krymer.delivery.data.model.CourierInfoModel
import ru.krymer.delivery.data.model.TripModel
import ru.krymer.delivery.data.repository.TripRepository
import ru.krymer.delivery.data.request.TripRequest
import ru.krymer.delivery.utills.MyResult
import javax.inject.Inject

class TripRepositoryImpl @Inject constructor(
    private val tripApi: TripApi
): TripRepository{
    override suspend fun add(trip: TripRequest): MyResult<TripModel?> {
        return try {
            val resp = tripApi.add(trip = trip)
            if (resp.success) MyResult.Success(resp.obj)
            else MyResult.Error(resp.message)
        } catch (t: Throwable) {
            MyResult.Error(t.message)
        }
    }

    override suspend fun getRoutesByTrip(idFactory: Long): MyResult<List<TripModel>> {
        return try {
            val resp = tripApi.getRoutesByTrip(idFactory = idFactory)
            if (resp.success) MyResult.Success(resp.obj ?: emptyList())
            else MyResult.Error(resp.message)
        } catch (t: Throwable) {
            MyResult.Error(t.message)
        }
    }

    override suspend fun update(trip: TripRequest): MyResult<TripModel?> {
        return try {
            val resp = tripApi.update(trip = trip)
            if (resp.success) MyResult.Success(resp.obj)
            else MyResult.Error(resp.message)
        } catch (t: Throwable) {
            MyResult.Error(t.message)
        }
    }

    override suspend fun delete(id: Long): MyResult<Unit> {
        return try {
            val resp = tripApi.delete(id = id)
            if (resp.success) MyResult.Success(Unit)
            else MyResult.Error(resp.message)
        } catch (t: Throwable) {
            MyResult.Error(t.message)
        }
    }

    override suspend fun getTrip(idTrip: Long): MyResult<TripModel?> {
        return try {
            val resp = tripApi.getTrip(idTrip = idTrip)
            if (resp.success) MyResult.Success(resp.obj)
            else MyResult.Error(resp.message)
        } catch (t: Throwable) {
            MyResult.Error(t.message)
        }
    }

    override suspend fun getDataAboutTrip(
        idFactory: Long,
        idTrip: Long
    ): MyResult<CourierInfoModel?> {
        return try {
            val resp = tripApi.getDataAboutTrip(idTrip = idTrip, idFactory = idFactory)
            if (resp.success) MyResult.Success(resp.obj)
            else MyResult.Error(resp.message)
        } catch (t: Throwable) {
            MyResult.Error(t.message)
        }
    }

    override suspend fun getTrips(
        idFactory: Long,
        sortBy: String?,
        uid: Long?,
        routeId: Long?
    ): MyResult<List<TripModel>> {
        return try {
            val resp = tripApi.getTrips(idFactory = idFactory, sortBy = sortBy, uid = uid, routeId = routeId)
            if (resp.success) MyResult.Success(resp.obj ?: emptyList())
            else MyResult.Error(resp.message)
        } catch (t: Throwable) {
            MyResult.Error(t.message)
        }
    }

    override suspend fun getPaginatedTrips(
        idFactory: Long,
        limit: Int,
        lastDate: Long?,
        lastId: Long?
    ): MyResult<List<TripModel>> {
        return try {
            val resp = tripApi.getPaginatedTrips(idFactory = idFactory, limit = limit, lastId = lastId, lastDate = lastDate)
            if (resp.success) MyResult.Success(resp.obj ?: emptyList())
            else MyResult.Error(resp.message)
        } catch (t: Throwable) {
            MyResult.Error(t.message)
        }
    }

}