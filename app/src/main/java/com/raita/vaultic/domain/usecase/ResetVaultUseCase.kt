package com.raita.vaultic.domain.usecase

import com.raita.vaultic.domain.repository.VaultRepository

class ResetVaultUseCase(private val repository: VaultRepository) {
    suspend operator fun invoke(): Result<Unit> = runCatching {
        repository.resetVault()
    }
}