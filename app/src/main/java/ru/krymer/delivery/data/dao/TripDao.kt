package ru.krymer.delivery.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ru.krymer.delivery.data.model.TripModel

@Dao
interface TripDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripModel)

    @Delete
    suspend fun deleteTrip(trip: TripModel)

    @Update
    suspend fun updateTrip(trip: TripModel)

    @Query("SELECT * FROM trips WHERE id = :tripId LIMIT 1")
    suspend fun getTripById(tripId: Long): TripModel?

    @Query("SELECT * FROM trips")
    suspend fun getTrips(): List<TripModel>
}