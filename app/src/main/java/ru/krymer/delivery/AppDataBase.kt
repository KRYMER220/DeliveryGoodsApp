package ru.krymer.delivery

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.krymer.delivery.data.dao.FactoryDao
import ru.krymer.delivery.data.dao.TripDao
import ru.krymer.delivery.data.dao.UserDao
import ru.krymer.delivery.data.model.FactoryModel
import ru.krymer.delivery.data.model.TripModel
import ru.krymer.delivery.data.model.user.UserModel

@Database(entities = [UserModel::class, TripModel::class, FactoryModel::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun tripDao(): TripDao
    abstract fun factoryDao(): FactoryDao
}