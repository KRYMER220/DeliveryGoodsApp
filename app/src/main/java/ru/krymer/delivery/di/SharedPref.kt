package ru.krymer.delivery.di

import EncryptedSharedPreferences
import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
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
    private const val KEYSTORE_ALIAS = "encrypted_prefs_key"
    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"

    @Provides
    @Singleton
    @EncryptedPref
    fun provideEncryptedSharedPref(@ApplicationContext context: Context): SharedPreferences {
        try {
            val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply {
                load(null)
            }
            val secretKey = if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
                val keyGenerator = KeyGenerator.getInstance("AES", KEYSTORE_PROVIDER)
                keyGenerator.init(256)
                keyGenerator.generateKey()
            } else {
                keyStore.getKey(KEYSTORE_ALIAS, null) as? SecretKey
                    ?: throw IllegalStateException("Failed to retrieve key from KeyStore")
            }
            val sharedPrefs = context.getSharedPreferences(ENCRYPTED_PREFS_NAME, Context.MODE_PRIVATE)
            return EncryptedSharedPreferences(sharedPrefs, secretKey)
        } catch (e: Exception) {
            return context.getSharedPreferences(ENCRYPTED_PREFS_NAME, Context.MODE_PRIVATE)
        }
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