package com.raita.vaultic.data.repository

import android.content.Context
import com.raita.vaultic.data.crypto.BiometricCryptoManager
import com.raita.vaultic.data.crypto.KeyDerivation
import com.raita.vaultic.data.crypto.SecureStore
import com.raita.vaultic.data.local.VaultDatabase
import com.raita.vaultic.data.local.VaultEntryEntity
import com.raita.vaultic.domain.model.VaultEntry
import com.raita.vaultic.domain.repository.VaultRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.crypto.Cipher

class VaultRepositoryImpl(private val context: Context, private val secureStore: SecureStore) :
    VaultRepository {

    private var database: VaultDatabase? = null

    private var pendingDeriveKeyForBiometric: ByteArray? = null

    override fun observeEntries(): Flow<List<VaultEntry>> =
        requireUnlocked().vaultEntryDao().observeAll().map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun addEntry(
        title: String,
        username: String,
        password: String,
        note: String
    ) {
        requireUnlocked().vaultEntryDao().insert(
            VaultEntryEntity(
                id = UUID.randomUUID().toString(),
                title = title,
                username = username,
                password = password,
                note = note
            )
        )
    }

    override suspend fun updateEntry(
        id: String,
        title: String,
        username: String,
        password: String,
        note: String
    ) {
        requireUnlocked().vaultEntryDao().update(
            VaultEntryEntity(
                id = id,
                title = title,
                username = username,
                password = password,
                note = note
            )
        )
    }

    override suspend fun deleteEntry(id: String) {
        requireUnlocked().vaultEntryDao().deleteById(id)
    }

    override suspend fun unlock(masterPassword: CharArray): Result<Unit> = runCatching {
        val salt = secureStore.getSalt() ?: KeyDerivation.generateSalt().also {
            secureStore.saveSalt(it)
        }
        val key = KeyDerivation.deriveKey(masterPassword, salt)
        try {
            pendingDeriveKeyForBiometric = key.copyOf()
            val db = VaultDatabase.create(context, key)
            db.openHelper.writableDatabase
            database = db
        } finally {
            key.fill(0)
            masterPassword.fill('0')
        }
    }

    override suspend fun lock() {
        database?.close()
        database = null
    }

    override fun isUnlocked(): Boolean = database != null

    override suspend fun resetVault() {
        lock()
        context.deleteDatabase(DB_NAME)
        secureStore.clearSalt()
    }

    override suspend fun enableBiometricUnlock(cipher: Cipher): Result<Unit> = runCatching {
        val rawKey =
            pendingDeriveKeyForBiometric ?: error("必須先用主密碼成功解鎖一次，才能啟用生物辨識")
        try {
            val encrypted = cipher.doFinal(rawKey)
            secureStore.saveBiometricKeyBlob(encrypted, cipher.iv)
        } finally {
            rawKey.fill(0)
            pendingDeriveKeyForBiometric = null
        }
    }

    override suspend fun unlockWithBiometricCipher(cipher: Cipher): Result<Unit> = runCatching {
        val (encryptedKEy, _) = secureStore.getBiometricKeyBlob() ?: error("尚未啟用生物辨識")
        val key = cipher.doFinal(encryptedKEy)
        try {
            pendingDeriveKeyForBiometric = key.copyOf()
            val db = VaultDatabase.create(context, key)
            db.openHelper.writableDatabase
            database = db
        } finally {
            key.fill(0)
        }
    }

    override fun isBiometricEnabled(): Boolean = secureStore.isBiometricEnabled()

    override fun getBiometricEncryptCipher(): Cipher? =
        if (isUnlocked()) BiometricCryptoManager.getEncryptCipher() else null

    override fun getBiometricDecryptCipher(): Cipher? {
        val (_, iv) = secureStore.getBiometricKeyBlob() ?: return null
        return BiometricCryptoManager.getDecryptCipher(iv)
    }

    override suspend fun disableBiometricUnlock() {
        secureStore.clearBiometricKeyBlob()
        BiometricCryptoManager.deleteKey()
    }

    private fun requireUnlocked(): VaultDatabase = database ?: error("Vault 尚未解鎖")

    private fun VaultEntryEntity.toDomain() =
        VaultEntry(id = id, title = title, username = username, password = password, note = note)

    companion object {
        private const val DB_NAME = "vault.db"
    }
}