package ru.krymer.delivery.di

import android.content.Context
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.krymer.delivery.AppDatabase
import ru.krymer.delivery.data.api.AnaliticApi
import ru.krymer.delivery.data.api.ClientApi
import ru.krymer.delivery.data.api.FactoryApi
import ru.krymer.delivery.data.api.LoggerApi
import ru.krymer.delivery.data.api.MessageApi
import ru.krymer.delivery.data.api.ProductApi
import ru.krymer.delivery.data.api.RequestApi
import ru.krymer.delivery.data.api.RouteApi
import ru.krymer.delivery.data.api.ShopApi
import ru.krymer.delivery.data.api.TripApi
import ru.krymer.delivery.data.api.UserApi
import ru.krymer.delivery.data.dao.FailedDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideContext(@ApplicationContext context: Context): Context {
        return context
    }

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideRetryManager(
        appDatabase: AppDatabase,
        requestApi: RequestApi,
        shopApi: ShopApi,
        gson: Gson
    ): RetryManager {
        return RetryManager(
            appDatabase, requestApi, shopApi, gson
        )
    }
}