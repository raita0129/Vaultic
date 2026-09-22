package com.raita.vaultic.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UpdateEntryUseCaseTest {

    private lateinit var repository: FakeVaultRepository
    private lateinit var addEntryUseCase: AddEntryUseCase
    private lateinit var updateEntryUseCase: UpdateEntryUseCase

    @Before
    fun setup() {
        repository = FakeVaultRepository()
        addEntryUseCase = AddEntryUseCase(repository)
        updateEntryUseCase = UpdateEntryUseCase(repository)
    }

    @Test
    fun `標題為空白時回傳失敗，原本的資料不會被覆蓋`() = runTest {
        addEntryUseCase(title = "Gmail", username = "raita", password = "pw123456")
        val existing = repository.observeEntries().first().single()

        val result = updateEntryUseCase(
            id = existing.id,
            title = "   ",
            username = "raita2",
            password = "pw999999"
        )

        assertTrue(result.isFailure)
        val unchanged = repository.observeEntries().first().single()
        assertEquals("Gmail", unchanged.title)
        assertEquals("raita", unchanged.username)
    }

    @Test
    fun `輸入合法時會用新的內容覆蓋既有項目`() = runTest {
        addEntryUseCase(title = "Gmail", username = "raita", password = "pw123456", note = "舊備註")
        val existing = repository.observeEntries().first().single()

        val result = updateEntryUseCase(
            id = existing.id,
            title = "Outlook",
            username = "raita2",
            password = "pw999999",
            note = "新備註"
        )

        assertTrue(result.isSuccess)
        val updated = repository.observeEntries().first().single()
        assertEquals(existing.id, updated.id)
        assertEquals("Outlook", updated.title)
        assertEquals("raita2", updated.username)
        assertEquals("新備註", updated.note)
    }
}