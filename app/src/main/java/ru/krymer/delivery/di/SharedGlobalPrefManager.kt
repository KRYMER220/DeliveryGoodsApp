package ru.krymer.delivery.di

import android.content.SharedPreferences
import androidx.core.content.edit
import javax.inject.Inject

class AppPreferencesManager @Inject constructor(
    @StandardPref private val sharedPreferences: SharedPreferences
) {
    fun getBooleanData(key: String): Boolean? {
        return sharedPreferences.getBoolean(key, false)
    }

    fun saveBoolean(key: String, data: Boolean) {
        sharedPreferences.edit { putBoolean(key, data) }
    }

    fun getIntData(key: String): Int? {
        return sharedPreferences.getInt(key, 2)
    }

    fun saveInt(key: String, data: Int) {
        sharedPreferences.edit { putInt(key, data) }
    }

    fun getLongData(key: String): Long? {
        return sharedPreferences.getLong(key, 0)
    }

    fun saveLong(key: String, data: Long) {
        sharedPreferences.edit { putLong(key, data) }
    }

    fun delete(key: String) {
        sharedPreferences.edit { remove(key) }
    }

    fun getStringData(key: String): String? {
        return sharedPreferences.getString(key, "")
    }

    fun saveString(key: String, data: String) {
        sharedPreferences.edit { putString(key, data) }
    }
}
