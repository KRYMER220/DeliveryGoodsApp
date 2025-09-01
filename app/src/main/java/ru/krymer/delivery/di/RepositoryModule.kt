package ru.krymer.delivery.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.krymer.delivery.data.repository.ClientRepository
import ru.krymer.delivery.data.repository.RouteRepository
import ru.krymer.delivery.data.repositoryImpl.ClientRepositoryImpl
import ru.krymer.delivery.data.repositoryImpl.RouteRepositoryImpl
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
}