package com.raita.vaultic.domain.repository

import com.raita.vaultic.domain.model.VaultEntry
import kotlinx.coroutines.flow.Flow

interface VaultRepository {
    fun observeEntries(): Flow<List<VaultEntry>>
    suspend fun addEntry(title: String, username: String, password: String)
    suspend fun deleteEntry(id: String)
    suspend fun unlock(masterPassword: CharArray): Result<Unit>
    suspend fun lock()
    fun isUnlocked(): Boolean
}