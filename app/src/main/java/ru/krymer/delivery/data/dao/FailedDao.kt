package ru.krymer.delivery.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import ru.krymer.delivery.data.model.FailedRequest

@Dao
interface FailedDao {
    @Insert
    suspend fun insert(failedRequest: FailedRequest)

    @Update
    suspend fun update(failedRequest: FailedRequest)

    @Query("SELECT * FROM failed ORDER BY timestamp DESC")
    suspend fun getAllFailedRequests(): List<FailedRequest>

    @Query("DELETE FROM failed WHERE id = :id")
    suspend fun deleteById(id: Long)
}