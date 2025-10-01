package ru.krymer.delivery.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.krymer.delivery.data.repository.ClientRepository
import ru.krymer.delivery.data.repository.CourierRepository
import ru.krymer.delivery.data.repository.ProductRepository
import ru.krymer.delivery.data.repository.RouteRepository
import ru.krymer.delivery.data.repository.TripRepository
import ru.krymer.delivery.data.repositoryImpl.ClientRepositoryImpl
import ru.krymer.delivery.data.repositoryImpl.CourierRepositoryImpl
import ru.krymer.delivery.data.repositoryImpl.ProductRepositoryImpl
import ru.krymer.delivery.data.repositoryImpl.RouteRepositoryImpl
import ru.krymer.delivery.data.repositoryImpl.TripRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindRouteRepository(
        impl: RouteRepositoryImpl
    ): RouteRepository

    @Binds
    @Singleton
    abstract fun bindClientRepository(
        impl: ClientRepositoryImpl
    ): ClientRepository

    @Binds
    @Singleton
    abstract fun bindProductRepository(
        impl: ProductRepositoryImpl
    ): ProductRepository

    @Binds
    @Singleton
    abstract fun bindCourierRepository(
        impl: CourierRepositoryImpl
    ): CourierRepository

    @Binds
    @Singleton
    abstract fun bindTripRepository(
        impl: TripRepositoryImpl
    ): TripRepository
}