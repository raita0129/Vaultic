package com.raita.vaultic.domain.usecase

import com.raita.vaultic.domain.repository.VaultRepository

class UnlockVaultUseCase(private val repository: VaultRepository) {
    suspend operator fun invoke(masterPassword: CharArray): Result<Unit> {
        if (masterPassword.isEmpty()) {
            return Result.failure(IllegalArgumentException("主密碼不可為空"))
        }
        return repository.unlock(masterPassword)
    }
}