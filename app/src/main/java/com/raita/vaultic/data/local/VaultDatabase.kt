package com.raita.vaultic.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import net.sqlcipher.database.SupportFactory

@Database(entities = [VaultEntryEntity::class], version = 1, exportSchema = false)
abstract class VaultDatabase : RoomDatabase() {
    abstract fun vaultEntryDao(): VaultEntryDao

    companion object {
        private const val DB_NAME = "vault.db"

        fun create(context: Context, derivedKey: ByteArray): VaultDatabase {
            val factory = SupportFactory(derivedKey)
            return Room.databaseBuilder(
                context.applicationContext,
                VaultDatabase::class.java,
                DB_NAME
            )
                .openHelperFactory(factory)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}