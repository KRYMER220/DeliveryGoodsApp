package ru.krymer.delivery.di

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class EncryptedPref

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class StandardPref

@Module
@InstallIn(SingletonComponent::class)
object SharedPrefModule {
    private const val ENCRYPTED_PREFS_NAME = "encrypted_token_prefs"
    private const val STANDARD_PREFS_NAME = "standard_app_prefs"

    @Provides
    @Singleton
    @EncryptedPref
    fun provideEncryptedSharedPref(@ApplicationContext context: Context): SharedPreferences {
        val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        return EncryptedSharedPreferences.create(
            ENCRYPTED_PREFS_NAME,
            masterKey,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    @Provides
    @Singleton
    @StandardPref
    fun provideStandardSharedPref(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(
            STANDARD_PREFS_NAME,
            Context.MODE_PRIVATE
        )
    }
}