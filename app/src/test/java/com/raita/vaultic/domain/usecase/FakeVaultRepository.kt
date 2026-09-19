package com.raita.vaultic.domain.usecase

import com.raita.vaultic.domain.model.VaultEntry
import com.raita.vaultic.domain.repository.VaultRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.UUID

class FakeVaultRepository : VaultRepository {

    private val state = MutableStateFlow<List<VaultEntry>>(emptyList())
    private var unlocked = true

    var resetVaultCallCount = 0
        private set

    override fun observeEntries(): Flow<List<VaultEntry>> = state

    override suspend fun addEntry(
        title: String,
        username: String,
        password: String,
        note: String
    ) {
        state.value = state.value + VaultEntry(
            id = UUID.randomUUID().toString(),
            title = title,
            username = username,
            password = password,
            note = note
        )
    }

    override suspend fun updateEntry(
        id: String,
        title: String,
        username: String,
        password: String,
        note: String
    ) {
        state.value = state.value.map { entry ->
            if (entry.id == id) {
                entry.copy(title = title, username = username, password = password, note = note)
            } else {
                entry
            }
        }
    }

    override suspend fun deleteEntry(id: String) {
        state.value = state.value.filterNot { it.id == id }
    }

    override suspend fun unlock(masterPassword: CharArray): Result<Unit> {
        unlocked = true
        return Result.success(Unit)
    }

    override suspend fun lock() {
        unlocked = false
    }

    override fun isUnlocked(): Boolean = unlocked

    override suspend fun resetVault() {
        resetVaultCallCount++
        state.value = emptyList()
    }
}
