package com.raita.vaultic.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeneratePasswordUseCaseTest {

    @Test
    fun `預設長度是16`() {
        val password = GeneratePasswordUseCase.generate()
        assertEquals(16, password.length)
    }

    @Test
    fun `可以自訂長度`() {
        val password = GeneratePasswordUseCase.generate(length = 24)
        assertEquals(24, password.length)
    }

    @Test
    fun `一定同時包含大寫、小寫、數字、符號`() {
        val password = GeneratePasswordUseCase.generate()
        assertTrue("缺少大寫字母:$password", password.any { it.isUpperCase() })
        assertTrue("缺少小寫字母:$password", password.any { it.isLowerCase() })
        assertTrue("缺少數字:$password", password.any { it.isDigit() })
        assertTrue("缺少符號:$password", password.any { !it.isLetterOrDigit() })
    }

    @Test
    fun `連續產生兩次結果不同`() {
        val first = GeneratePasswordUseCase.generate()
        val second = GeneratePasswordUseCase.generate()
        assertNotEquals(first, second)
    }
}
