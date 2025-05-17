package ru.krymer.delivery.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ru.krymer.delivery.data.model.RequestModel

@Dao
interface RequestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: RequestModel)

    @Delete
    suspend fun deleteRequest(request: RequestModel)

    @Update
    suspend fun updateRequest(request: RequestModel)

    @Query("SELECT * FROM request WHERE idShop = :idShop AND idTrip = :idTrip")
    suspend fun getRequests(idShop: Long, idTrip: Long): List<RequestModel>
}