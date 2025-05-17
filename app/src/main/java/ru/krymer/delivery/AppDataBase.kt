package ru.krymer.delivery

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.krymer.delivery.data.dao.FactoryDao
import ru.krymer.delivery.data.dao.RequestDao
import ru.krymer.delivery.data.dao.ShopDao
import ru.krymer.delivery.data.dao.TripDao
import ru.krymer.delivery.data.dao.UserDao
import ru.krymer.delivery.data.model.FactoryModel
import ru.krymer.delivery.data.model.RequestModel
import ru.krymer.delivery.data.model.ShopLocalModel
import ru.krymer.delivery.data.model.ShopModel
import ru.krymer.delivery.data.model.TripModel
import ru.krymer.delivery.data.model.user.UserModel

@Database(entities = [UserModel::class, TripModel::class, FactoryModel::class, ShopLocalModel::class, RequestModel::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun tripDao(): TripDao
    abstract fun factoryDao(): FactoryDao
    abstract fun shopDao(): ShopDao
    abstract fun requestDao(): RequestDao
}