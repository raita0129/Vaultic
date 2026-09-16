package com.raita.vaultic.data.repository

import android.content.Context
import com.raita.vaultic.data.crypto.KeyDerivation
import com.raita.vaultic.data.crypto.SecureStore
import com.raita.vaultic.data.local.VaultDatabase
import com.raita.vaultic.data.local.VaultEntryEntity
import com.raita.vaultic.domain.model.VaultEntry
import com.raita.vaultic.domain.repository.VaultRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class VaultRepositoryImpl(private val context: Context, private val secureStore: SecureStore) :
    VaultRepository {

    private var database: VaultDatabase? = null

    override fun observeEntries(): Flow<List<VaultEntry>> =
        requireUnlocked().vaultEntryDao().observeAll().map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun addEntry(
        title: String,
        username: String,
        password: String
    ) {
        requireUnlocked().vaultEntryDao().insert(
            VaultEntryEntity(
                id = UUID.randomUUID().toString(),
                title = title,
                username = username,
                password = password
            )
        )
    }

    override suspend fun updateEntry(
        id: String,
        title: String,
        username: String,
        password: String
    ) {
        requireUnlocked().vaultEntryDao().update(
            VaultEntryEntity(
                id = id,
                title = title,
                username = username,
                password = password
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

    private fun requireUnlocked(): VaultDatabase = database ?: error("Vault 尚未解鎖")

    private fun VaultEntryEntity.toDomain() =
        VaultEntry(id = id, title = title, username = username, password = password)
}