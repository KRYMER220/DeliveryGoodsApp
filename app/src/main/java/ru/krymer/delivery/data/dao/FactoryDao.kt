package ru.krymer.delivery.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ru.krymer.delivery.data.model.FactoryModel
import ru.krymer.delivery.data.model.user.UserModel

@Dao
interface FactoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFactory(factory: FactoryModel)

    @Query("DELETE FROM factory")
    suspend fun deleteFactory()

    @Update
    suspend fun updateFactory(factory: FactoryModel)

    @Query("SELECT * FROM factory WHERE id = :factoryId")
    suspend fun getFactoryById(factoryId: Long): FactoryModel?

    @Query("SELECT * FROM factory LIMIT 1")
    suspend fun getFactory(): FactoryModel?
}