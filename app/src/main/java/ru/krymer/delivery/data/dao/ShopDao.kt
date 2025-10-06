package ru.krymer.delivery.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ru.krymer.delivery.data.model.ShopModel

@Dao
interface ShopDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShop(shop: ShopModel)

    @Delete
    suspend fun deleteShop(shop: ShopModel)

    @Query("SELECT * FROM shop WHERE idTrip = :idTrip")
    suspend fun getShops(idTrip: Long): List<ShopModel>

    @Query("SELECT * FROM shop WHERE id = :id")
    suspend fun getShopsById(id: Long): List<ShopModel>

    @Query("DELETE FROM shop WHERE idTrip = :idTrip")
    suspend fun deleteShopsByTrip(idTrip: Long)

    @Query("DELETE FROM shop WHERE id IN (:ids)")
    suspend fun deleteShopsByIds(ids: List<Long>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShops(shops: List<ShopModel>)

    @Update
    suspend fun updateShops(shops: List<ShopModel>)

    @Query("SELECT * FROM shop WHERE idTrip = :idTrip AND statusServer == :status")
    suspend fun getStatusShops(idTrip: Long, status: String): List<ShopModel>

    @Query("UPDATE shop SET statusServer = :status WHERE id = :shopId")
    suspend fun markShopAsSynced(shopId: Long, status: String)

    @Query("SELECT statusServer FROM shop WHERE id = :shopId")
    suspend fun getShopStatus(shopId: Long): String

    @Query("SELECT COUNT(*) FROM shop WHERE id = :shopId AND statusServer = :status")
    suspend fun isShopWithStatus(shopId: Long, status: String): Int
}

