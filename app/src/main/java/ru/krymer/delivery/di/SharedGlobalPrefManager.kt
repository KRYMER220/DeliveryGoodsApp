package ru.krymer.delivery.di

import android.content.SharedPreferences
import javax.inject.Inject

class AppPreferencesManager @Inject constructor(
    @StandardPref private val sharedPreferences: SharedPreferences
) {

    fun getBooleanData(key: String): Boolean? {
        return sharedPreferences.getBoolean(key, false)
    }

    fun saveBoolean(key: String, data: Boolean) {
        sharedPreferences.edit().putBoolean(key, data).apply()
    }

    fun getIntData(key: String): Int? {
        return sharedPreferences.getInt(key, 0)
    }

    fun saveInt(key: String, data: Int) {
        sharedPreferences.edit().putInt(key, data).apply()
    }

    fun getStringData(key: String): String? {
        return sharedPreferences.getString(key, "")
    }

    fun saveString(key: String, data: String) {
        sharedPreferences.edit().putString(key, data).apply()
    }
}
