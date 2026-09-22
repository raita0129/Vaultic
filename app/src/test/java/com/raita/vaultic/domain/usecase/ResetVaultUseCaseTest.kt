package com.raita.vaultic.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ResetVaultUseCaseTest {

    private lateinit var repository: FakeVaultRepository
    private lateinit var addEntryUseCase: AddEntryUseCase
    private lateinit var resetVaultUseCase: ResetVaultUseCase

    @Before
    fun setup() {
        repository = FakeVaultRepository()
        addEntryUseCase = AddEntryUseCase(repository)
        resetVaultUseCase = ResetVaultUseCase(repository)
    }

    @Test
    fun `執行後 repository 的 resetVault 會被呼叫一次，且清單被清空`() = runTest {
        addEntryUseCase(title = "Gmail", username = "raita", password = "pw123456")
        addEntryUseCase(title = "Outlook", username = "raita", password = "pw654321")

        val result = resetVaultUseCase()

        assertTrue(result.isSuccess)
        assertEquals(1, repository.resetVaultCallCount)
        assertTrue(repository.observeEntries().first().isEmpty())
    }

    @Test
    fun `不需要輸入任何參數就能執行`() = runTest {
        val result = resetVaultUseCase()
        assertTrue(result.isSuccess)
    }
}