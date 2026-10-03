package com.raita.vaultic.domain.repository

import com.raita.vaultic.domain.model.VaultEntry
import kotlinx.coroutines.flow.Flow
import javax.crypto.Cipher

interface VaultRepository {
    /** 未解鎖時，呼叫本身不得丟例外；改在收集 Flow 時失敗，讓呼叫端可用 .catch 處理。 */
    fun observeEntries(): Flow<List<VaultEntry>>
    suspend fun addEntry(title: String, username: String, password: String, note: String = "")
    suspend fun updateEntry(
        id: String,
        title: String,
        username: String,
        password: String,
        note: String = ""
    )

    suspend fun deleteEntry(id: String)
    suspend fun unlock(masterPassword: CharArray): Result<Unit>
    suspend fun lock()
    fun isUnlocked(): Boolean
    suspend fun resetVault()

    suspend fun enableBiometricUnlock(cipher: Cipher): Result<Unit>
    suspend fun unlockWithBiometricCipher(cipher: Cipher): Result<Unit>
    fun isBiometricEnabled(): Boolean
    fun getBiometricEncryptCipher(): Cipher?
    fun getBiometricDecryptCipher(): Cipher?
    suspend fun disableBiometricUnlock()
}