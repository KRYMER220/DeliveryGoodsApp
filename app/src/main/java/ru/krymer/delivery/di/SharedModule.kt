package ru.krymer.delivery.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.krymer.delivery.data.TokenManager
import ru.krymer.delivery.data.api.FactoryApi
import ru.krymer.delivery.data.api.LoggerApi
import ru.krymer.delivery.data.api.UserApi
import ru.krymer.delivery.ui.screens.shared.SharedViewModel
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SharedModule {

    @Provides
    @Singleton
    fun provideSharedViewModel(
        userApi: UserApi,
        tokenManager: TokenManager,
        factoryApi: FactoryApi,
        loggerApi: LoggerApi
    ): SharedViewModel {
        return SharedViewModel(
            userApi = userApi,
            tokenManager = tokenManager,
            factoryApi = factoryApi,
            loggerApi = loggerApi
        )
    }
}