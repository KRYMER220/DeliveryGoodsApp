package ru.krymer.delivery.di

interface SecureDataStore {
    suspend fun getString(key: String): String?
    suspend fun putString(key: String, value: String)
    suspend fun clear()
}