package ru.krymer.delivery.data.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import ru.krymer.delivery.data.model.TripModel
import ru.krymer.delivery.data.request.CreateTripRequest
import ru.krymer.delivery.data.request.UpdateTripRequest
import ru.krymer.delivery.data.response.BaseResponse
import ru.krymer.delivery.utills.Constants

interface TripApi {
    @POST("create-trip")
    suspend fun addTrip(@Body trip: CreateTripRequest): BaseResponse<TripModel>

    @GET("get-trips")
    suspend fun getCurrentTrips(@Query(Constants.ID.ID_FACTORY) idFactory: Long): BaseResponse<List<TripModel>>

    @GET("get-route-for-trip")
    suspend fun getRoutesForTrip(@Query(Constants.ID.ID_FACTORY) idFactory: Long): BaseResponse<List<TripModel>>

    @POST("update-trip")
    suspend fun updateTrip(@Body trip: UpdateTripRequest): BaseResponse<TripModel>

    @DELETE("delete-trip")
    suspend fun deleteTrip(@Query(Constants.ID.ID) idTrip: Long): BaseResponse<TripModel>
}

