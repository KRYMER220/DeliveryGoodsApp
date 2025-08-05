import android.content.SharedPreferences
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class EncryptedSharedPreferences(
    private val delegate: SharedPreferences,
    private val secretKey: SecretKey
) : SharedPreferences {
    override fun getAll(): Map<String, *> = delegate.all.mapValues { entry ->
        if (entry.value is String) {
            try {
                decrypt(entry.key, entry.value as String)
            } catch (e: Exception) {
                null
            }
        } else {
            entry.value
        }
    }

    override fun getString(key: String, defValue: String?): String? {
        val encryptedValue = delegate.getString(key, null)
        return if (encryptedValue != null) {
            try {
                decrypt(key, encryptedValue)
            } catch (e: Exception) {
                defValue
            }
        } else {
            defValue
        }
    }

    override fun getStringSet(key: String, defValues: Set<String>?): Set<String>? {
        val encryptedValues = delegate.getStringSet(key, null)
        return if (encryptedValues != null) {
            try {
                encryptedValues.map { decrypt(key, it) }.toSet()
            } catch (e: Exception) {
                defValues
            }
        } else {
            defValues
        }
    }

    override fun getInt(key: String, defValue: Int): Int = throw UnsupportedOperationException("Only strings are supported")
    override fun getLong(key: String, defValue: Long): Long = throw UnsupportedOperationException("Only strings are supported")
    override fun getFloat(key: String, defValue: Float): Float = throw UnsupportedOperationException("Only strings are supported")
    override fun getBoolean(key: String, defValue: Boolean): Boolean = throw UnsupportedOperationException("Only strings are supported")

    override fun contains(key: String): Boolean = delegate.contains(key)

    override fun edit(): SharedPreferences.Editor = EncryptedEditor(delegate.edit(), secretKey)

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        delegate.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        delegate.unregisterOnSharedPreferenceChangeListener(listener)
    }

    private fun encrypt(key: String, value: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        val combined = iv + ciphertext
        return Base64.getEncoder().encodeToString(combined)
    }

    private fun decrypt(key: String, encryptedValue: String): String {
        val combined = Base64.getDecoder().decode(encryptedValue)
        val iv = combined.copyOfRange(0, 12)
        val ciphertext = combined.copyOfRange(12, combined.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
        val plaintext = cipher.doFinal(ciphertext)
        return String(plaintext, Charsets.UTF_8)
    }
}

class EncryptedEditor(
    private val delegate: SharedPreferences.Editor,
    private val secretKey: SecretKey
) : SharedPreferences.Editor {
    override fun putString(key: String, value: String?): SharedPreferences.Editor {
        if (value != null) {
            val encryptedValue = encrypt(key, value)
            delegate.putString(key, encryptedValue)
        } else {
            delegate.putString(key, null)
        }
        return this
    }

    override fun putStringSet(key: String, values: Set<String>?): SharedPreferences.Editor {
        if (values != null) {
            val encryptedValues = values.map { encrypt(key, it) }.toSet()
            delegate.putStringSet(key, encryptedValues)
        } else {
            delegate.putStringSet(key, null)
        }
        return this
    }

    override fun putInt(key: String, value: Int): SharedPreferences.Editor = throw UnsupportedOperationException("Only strings are supported")
    override fun putLong(key: String, value: Long): SharedPreferences.Editor = throw UnsupportedOperationException("Only strings are supported")
    override fun putFloat(key: String, value: Float): SharedPreferences.Editor = throw UnsupportedOperationException("Only strings are supported")
    override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor = throw UnsupportedOperationException("Only strings are supported")

    override fun remove(key: String): SharedPreferences.Editor {
        delegate.remove(key)
        return this
    }

    override fun clear(): SharedPreferences.Editor {
        delegate.clear()
        return this
    }

    override fun commit(): Boolean = delegate.commit()
    override fun apply() = delegate.apply()

    private fun encrypt(key: String, value: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        val combined = iv + ciphertext
        return Base64.getEncoder().encodeToString(combined)
    }
}