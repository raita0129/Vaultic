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

    companion object {
        private const val KEY_SALT = "vault_salt"
    }
}