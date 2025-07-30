package ru.krymer.delivery.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import ru.krymer.delivery.data.model.user.UserModel

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserModel)

    @Query("DELETE FROM users")
    suspend fun delete()

    @Update
    suspend fun updateUser(user: UserModel)

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Long): UserModel?

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getUser(): UserModel?
}