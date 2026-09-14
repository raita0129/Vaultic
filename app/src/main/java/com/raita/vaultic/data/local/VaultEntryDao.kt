package com.raita.vaultic.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VaultEntryDao {

    @Query("SELECT*FROM vault_entries ORDER BY title ASC")
    fun observeAll(): Flow<List<VaultEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: VaultEntryEntity)

    @Query("DELETE FROM vault_entries WHERE id=:id")
    suspend fun deleteById(id: String)
}