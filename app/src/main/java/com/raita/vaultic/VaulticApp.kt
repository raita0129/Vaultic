package com.raita.vaultic

import android.app.Application
import com.raita.vaultic.data.crypto.SecureStore
import com.raita.vaultic.data.repository.VaultRepositoryImpl
import com.raita.vaultic.domain.repository.VaultRepository

class VaulticApp : Application() {

    lateinit var repository: VaultRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = VaultRepositoryImpl(
            context = this,
            secureStore = SecureStore(this)
        )
    }
}