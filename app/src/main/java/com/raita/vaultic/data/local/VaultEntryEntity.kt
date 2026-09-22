package com.raita.vaultic.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vault_entries")
data class VaultEntryEntity(
    @PrimaryKey val id: String,
    val title: String,
    val username: String,
    val password: String,
    val note: String = ""
)
