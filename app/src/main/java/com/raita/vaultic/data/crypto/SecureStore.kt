package com.raita.vaultic.data.crypto

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecureStore(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "vault_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun getSalt(): ByteArray? =
        prefs.getString(KEY_SALT, null)?.let { Base64.decode(it, Base64.NO_WRAP) }

    fun saveSalt(salt: ByteArray) {
        prefs.edit()
            .putString(KEY_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
            .apply()
    }

    fun hasVault(): Boolean = prefs.contains(KEY_SALT)

    fun clearSalt() {
        prefs.edit().remove(KEY_SALT).apply()
    }

    fun saveBiometricKeyBlob(encryptedKey: ByteArray, iv: ByteArray) {
        prefs.edit()
            .putString(KEY_BIOMETRIC_KEY, Base64.encodeToString(encryptedKey, Base64.NO_WRAP))
            .putString(KEY_BIOMETRIC_IV, Base64.encodeToString(iv, Base64.NO_WRAP))
            .apply()
    }

    fun getBiometricKeyBlob(): Pair<ByteArray, ByteArray>? {
        val key =
            prefs.getString(KEY_BIOMETRIC_KEY, null)?.let { Base64.decode(it, Base64.NO_WRAP) }
        val iv = prefs.getString(KEY_BIOMETRIC_IV, null)?.let { Base64.decode(it, Base64.NO_WRAP) }
        return if (key != null && iv != null) key to iv else null;
    }

    fun isBiometricEnabled(): Boolean = prefs.contains(KEY_BIOMETRIC_KEY)

    fun clearBiometricKeyBlob() {
        prefs.edit().remove(KEY_BIOMETRIC_KEY).remove(KEY_BIOMETRIC_IV).apply()
    }

    companion object {
        private const val KEY_SALT = "vault_salt"
        private const val KEY_BIOMETRIC_KEY = "vault_biometric_key"
        private const val KEY_BIOMETRIC_IV = "vault_biometric_iv"
    }
}