package ru.krymer.delivery.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ru.krymer.delivery.data.model.ShopLocalModel

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
}

