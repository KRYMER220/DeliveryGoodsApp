package ru.krymer.delivery.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class SecureDataStoreImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cryptoManager: CryptoManager
) : SecureDataStore {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "secure_datastore")
    private val mutex = Mutex()

    override suspend fun getString(key: String): String? {
        return context.dataStore.data.map { prefs -> prefs[stringPreferencesKey(key)] }.first()
            ?.let { cryptoManager.decrypt(it) }
    }

    override suspend fun putString(key: String, value: String) {
        mutex.withLock {
            context.dataStore.edit { prefs ->
                prefs[stringPreferencesKey(key)] = cryptoManager.encrypt(value)
            }
        }
    }

    override suspend fun clear() {
        mutex.withLock {
            context.dataStore.edit { it.clear() }
        }
    }
}

suspend fun SecureDataStore.getBoolean(key: String): Boolean? {
    return getString(key)?.toBooleanStrictOrNull()
}

suspend fun SecureDataStore.putBoolean(key: String, value: Boolean) {
    putString(key, value.toString())
}

suspend fun SecureDataStore.getInt(key: String): Int? {
    return getString(key)?.toIntOrNull()
}

suspend fun SecureDataStore.putInt(key: String, value: Int) {
    putString(key, value.toString())
}