package com.raita.vaultic.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AddEntryUseCaseTest {

    private lateinit var repository: FakeVaultRepository
    private lateinit var useCase: AddEntryUseCase

    @Before
    fun setup() {
        repository = FakeVaultRepository()
        useCase = AddEntryUseCase(repository)
    }

    @Test
    fun `標題為空白時回傳失敗，不會寫入 repository`() = runTest {
        val result = useCase(
            title = "   ",
            username = "raita",
            password = "pw123456"
        )

        assertTrue(result.isFailure)
        assertTrue(repository.observeEntries().first().isEmpty())
    }

    @Test
    fun `密碼為空白時回傳失敗，不會寫入repository`() = runTest {
        val result = useCase(
            title = "Gmail",
            username = "raita",
            password = "  "
        )

        assertTrue(result.isFailure)
        assertTrue(repository.observeEntries().first().isEmpty())
    }

    @Test
    fun `輸入合法時成功寫入,標題帳號備註都會被trim`() = runTest {
        val result = useCase(
            title = "  Gmail  ",
            username = "  raita  ",
            password = "pw123456",
            note = "  常用信箱  "
        )

        assertTrue(result.isSuccess)
        val saved = repository.observeEntries().first().single()
        assertEquals("Gmail", saved.title)
        assertEquals("raita", saved.username)
        assertEquals("常用信箱", saved.note)
    }

    @Test
    fun `note 允許留空`() = runTest {
        val result = useCase(
            title = "Gmail",
            username = "raita",
            password = "pw123456"
        )

        assertTrue(result.isSuccess)
        assertEquals("", repository.observeEntries().first().single().note)
    }
}