package com.raita.vaultic.data.crypto

import com.lambdapioneer.argon2kt.Argon2Kt
import com.lambdapioneer.argon2kt.Argon2Mode
import java.security.SecureRandom

object KeyDerivation {
    private val argon2 = Argon2Kt()

    private const val T_COST_ITERATIONS = 3
    private const val M_COST_KIBIBYTE = 65536
    private const val PARALLELISM = 4

    fun deriveKey(masterPassword: CharArray, salt: ByteArray): ByteArray {
        val passwordBytes = String(masterPassword).toByteArray(Charsets.UTF_8)
        try {
            val result = argon2.hash(
                mode = Argon2Mode.ARGON2_ID,
                password = passwordBytes,
                salt = salt,
                tCostInIterations = T_COST_ITERATIONS,
                mCostInKibibyte = M_COST_KIBIBYTE,
                parallelism = PARALLELISM
            )
            return result.rawHashAsByteArray()
        } finally {
            passwordBytes.fill(0)
        }
    }

    fun generateSalt(): ByteArray {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        return salt
    }
}