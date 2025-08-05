package ru.krymer.delivery.di

import android.content.SharedPreferences
import ru.krymer.delivery.utills.Constants
import javax.inject.Inject
class TokenManager @Inject constructor(
    @EncryptedPref private val sharedPreferences: SharedPreferences
) {
    fun getAccessToken(): String? {
        return sharedPreferences.getString(Constants.TOKEN.ACCESS, null)
    }

    fun deleteToken() {
        sharedPreferences.edit().remove(Constants.TOKEN.ACCESS).apply()
    }

    fun saveAccessToken(token: String?) {
        sharedPreferences.edit().putString(Constants.TOKEN.ACCESS, token).apply()
    }
}