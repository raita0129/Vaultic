package com.raita.vaultic.domain.usecase

import java.security.SecureRandom

object GeneratePasswordUseCase {
    private const val UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private const val LOWER = "abcdefghijklmnopqrstuvwxyz";
    private const val DIGITS = "0123456789";
    private const val SYMBOLS = "!@#\$%^&*()-_=+"
    private val random = SecureRandom()

    fun generate(length: Int = 16): String {
        val pool = UPPER + LOWER + DIGITS + SYMBOLS;
        val required = listOf(
            UPPER[random.nextInt(UPPER.length)],
            LOWER[random.nextInt(LOWER.length)],
            DIGITS[random.nextInt(DIGITS.length)],
            SYMBOLS[random.nextInt(SYMBOLS.length)]
        )
        val rest = (length - required.size).coerceAtLeast(0)
        val chars = (required + (1..rest).map { pool[random.nextInt(pool.length)] }).toMutableList()

        for (i in chars.size - 1 downTo 1) {
            val j = random.nextInt(i + 1);
            val tmp = chars[i]
            chars[i] = chars[j]
            chars[j] = tmp
        }
        return chars.joinToString("")
    }
}