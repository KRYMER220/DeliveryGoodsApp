package ru.krymer.delivery.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import ru.krymer.delivery.data.api.AnaliticApi
import ru.krymer.delivery.data.api.ClientApi
import ru.krymer.delivery.data.api.FactoryApi
import ru.krymer.delivery.data.api.LoggerApi
import ru.krymer.delivery.data.api.ProductApi
import ru.krymer.delivery.data.api.RequestApi
import ru.krymer.delivery.data.api.RouteApi
import ru.krymer.delivery.data.api.ShopApi
import ru.krymer.delivery.data.api.TripApi
import ru.krymer.delivery.data.api.UserApi
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    @Singleton
    @Provides
    fun provideApiService(retrofit: Retrofit): UserApi {
        return retrofit.create(UserApi::class.java)
    }

    @Singleton
    @Provides
    fun provideFactoryApiService(retrofit: Retrofit): FactoryApi {
        return retrofit.create(FactoryApi::class.java)
    }

    @Singleton
    @Provides
    fun provideRouteApiService(retrofit: Retrofit): RouteApi {
        return retrofit.create(RouteApi::class.java)
    }

    @Singleton
    @Provides
    fun provideClientApiService(retrofit: Retrofit): ClientApi {
        return retrofit.create(ClientApi::class.java)
    }

    @Singleton
    @Provides
    fun provideProductApiService(retrofit: Retrofit): ProductApi {
        return retrofit.create(ProductApi::class.java)
    }

    @Singleton
    @Provides
    fun provideTripApiService(retrofit: Retrofit): TripApi {
        return retrofit.create(TripApi::class.java)
    }

    @Singleton
    @Provides
    fun provideShopApiService(retrofit: Retrofit): ShopApi {
        return retrofit.create(ShopApi::class.java)
    }

    @Singleton
    @Provides
    fun provideRequestApiService(retrofit: Retrofit): RequestApi {
        return retrofit.create(RequestApi::class.java)
    }

    @Singleton
    @Provides
    fun provideLoggerApiService(retrofit: Retrofit): LoggerApi {
        return retrofit.create(LoggerApi::class.java)
    }

    @Singleton
    @Provides
    fun provideAnaliticApiService(retrofit: Retrofit): AnaliticApi {
        return retrofit.create(AnaliticApi::class.java)
    }
}