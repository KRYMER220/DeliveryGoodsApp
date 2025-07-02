package ru.krymer.delivery.data.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import ru.krymer.delivery.data.model.CourierInfoModel
import ru.krymer.delivery.data.model.TripModel
import ru.krymer.delivery.data.request.CreateTripRequest
import ru.krymer.delivery.data.request.UpdateTripRequest
import ru.krymer.delivery.data.response.BaseResponse
import ru.krymer.delivery.utills.Constants

interface TripApi {
    @POST("trip/create")
    suspend fun add(@Body trip: CreateTripRequest): BaseResponse<TripModel>

    @GET("routes/by/trips")
    suspend fun getRoutesByTrip(@Query(Constants.HttpRequestKeys.ID_FACTORY) idFactory: Long): BaseResponse<List<TripModel>>

    @POST("trip/update")
    suspend fun update(@Body trip: UpdateTripRequest): BaseResponse<TripModel>

    @DELETE("trip/delete")
    suspend fun delete(@Query(Constants.HttpRequestKeys.ID) id: Long): BaseResponse<TripModel>

    @GET("trip/get/data")
    suspend fun getDataAboutTrip(
        @Query(Constants.HttpRequestKeys.ID_FACTORY) idFactory: Long,
        @Query(Constants.HttpRequestKeys.ID_TRIP) idTrip: Long
    ): BaseResponse<CourierInfoModel>

    @GET("trips")
    suspend fun getTrips(
        @Query(Constants.HttpRequestKeys.ID_FACTORY) idFactory: Long
    ): BaseResponse<List<TripModel>>

    @GET("trips/paginated")
    suspend fun gePaginatedTrips(
        @Query(Constants.HttpRequestKeys.ID_FACTORY) idFactory: Long,
        @Query(Constants.HttpRequestKeys.LIMIT) limit: Int = 10,
        @Query(Constants.HttpRequestKeys.LAST_DATE) lastDate: Long?,
        @Query(Constants.HttpRequestKeys.ID) lastId: Long?
    ): BaseResponse<List<TripModel>>
}

