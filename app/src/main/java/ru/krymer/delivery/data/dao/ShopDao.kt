package ru.krymer.delivery.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ru.krymer.delivery.data.model.ShopLocalModel
import ru.krymer.delivery.data.model.ShopModel
import ru.krymer.delivery.data.model.utilModel.getStringByTypePay

@Dao
interface ShopDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShop(shop: ShopLocalModel)

    @Delete
    suspend fun deleteShop(shop: ShopLocalModel)

    @Update
    suspend fun updateShop(shop: ShopLocalModel)

    @Query("SELECT * FROM shop WHERE idTrip = :idTrip")
    suspend fun getShops(idTrip: Long): List<ShopLocalModel>

    @Query("SELECT * FROM shop WHERE id = :id")
    suspend fun getShopsById(id: Long): List<ShopLocalModel>

    @Query("DELETE FROM shop WHERE idTrip = :idTrip")
    suspend fun deleteShopsByTrip(idTrip: Long)

    @Query("DELETE FROM shop WHERE id IN (:ids)")
    suspend fun deleteShopsByIds(ids: List<Long>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShops(shops: List<ShopLocalModel>)

    @Update
    suspend fun updateShops(shops: List<ShopLocalModel>)

    @Query("SELECT * FROM shop WHERE idTrip = :idTrip AND isSynced = 0")
    suspend fun getUnsyncedShops(idTrip: Long): List<ShopLocalModel>

    @Query("UPDATE shop SET isSynced = 1 WHERE id = :shopId")
    suspend fun markShopAsSynced(shopId: Long)

    @Query("SELECT isSynced FROM shop WHERE id = :shopId")
    suspend fun isShopSynced(shopId: Long): Boolean

}

