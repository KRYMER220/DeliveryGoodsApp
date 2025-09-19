package ru.krymer.delivery.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import org.jetbrains.annotations.ApiStatus
import ru.krymer.delivery.data.model.RequestModel

@Dao
interface RequestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRequest(request: RequestModel)

    @Delete
    suspend fun deleteRequest(request: RequestModel)

    @Query("SELECT * FROM request WHERE idShop = :idShop AND idTrip = :idTrip")
    suspend fun getRequests(idShop: Long, idTrip: Long): List<RequestModel>

    @Query("SELECT * FROM request WHERE idTrip = :idTrip")
    suspend fun getRequestsByTrip(idTrip: Long): List<RequestModel>

    @Query("DELETE FROM request WHERE idTrip = :idTrip ")
    suspend fun deleteRequestsByTrip(idTrip: Long)

    @Query("DELETE FROM request WHERE idShop IN (:shopIds)")
    suspend fun deleteRequestsByShopIds(shopIds: List<Long>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequests(requests: List<RequestModel>)

    @Update
    suspend fun updateRequests(requests: List<RequestModel>)

    @Query("SELECT * FROM request WHERE idShop = :idShop AND idTrip = :idTrip AND statusServer = :status")
    suspend fun getStatusRequests(idShop: Long, idTrip: Long, status: String): List<RequestModel>

    @Query("UPDATE request SET statusServer = :status WHERE id = :requestId")
    suspend fun markRequestAsSynced(requestId: Long, status: String)

    @Query("SELECT statusServer FROM request WHERE id = :requestId")
    suspend fun getRequestStatus(requestId: Long): String

    @Query("DELETE FROM request WHERE id IN (:requestIds)")
    suspend fun deleteRequestsByIds(requestIds: List<Long>)
}