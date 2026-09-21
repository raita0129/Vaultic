package com.raita.vaultic.domain.usecase

import com.raita.vaultic.domain.repository.VaultRepository
import javax.crypto.Cipher

class UnlockWithBiometricUseCase(private val repository: VaultRepository) {
    suspend operator fun invoke(cipher: Cipher): Result<Unit> =
        repository.unlockWithBiometricCipher(cipher)
}