package com.raita.vaultic.domain.usecase

import com.raita.vaultic.domain.repository.VaultRepository

class AddEntryUseCase(private val repository: VaultRepository) {
    suspend operator fun invoke(title: String, username: String, password: String): Result<Unit> {
        if (title.isBlank()) return Result.failure(IllegalArgumentException("標題不可為空"))
        if (password.isBlank()) return Result.failure(IllegalArgumentException("密碼不可為空"))
        return runCatching { repository.addEntry(title.trim(), username.trim(), password) }
    }
}