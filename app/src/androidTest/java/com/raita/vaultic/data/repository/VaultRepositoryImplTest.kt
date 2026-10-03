package com.raita.vaultic.data.repository

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.raita.vaultic.data.crypto.SecureStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeFalse
import org.junit.Test
import org.junit.runner.RunWith
import javax.crypto.Cipher
import javax.crypto.KeyGenerator

@RunWith(AndroidJUnit4::class)
class VaultRepositoryImplTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val secureStore = SecureStore(context)

    // 模擬行程被回收後的新行程：全新的 repository，尚未解鎖
    private val repository = VaultRepositoryImpl(context, secureStore)

    // 只有保險箱是本測試建立的，才在結束時清除；
    // assume 失敗時 @After 仍會執行，不能無條件 resetVault()
    private var createdVaultInTest = false

    @After
    fun tearDown() = runBlocking {
        if (!createdVaultInTest) return@runBlocking
        repository.resetVault()
        // resetVault() 用 apply() 非同步清除 salt，測試結束後行程會立刻被結束；
        // 空的 commit() 會等前面的寫入落地，否則殘留的 salt 會讓下一輪測試被 assume 跳過
        context.getSharedPreferences(SECURE_PREFS_NAME, Context.MODE_PRIVATE).edit().commit()
    }

    @Test
    fun `未解鎖時呼叫observeEntries不丟例外，收集時才失敗`() {
        val flow = repository.observeEntries()

        val error = runBlocking { runCatching { flow.first() }.exceptionOrNull() }
        assertTrue("未解鎖時收集應該丟 IllegalStateException，實際：$error", error is IllegalStateException)
    }

    @Test
    fun `主密碼解鎖成功後，可以啟用生物辨識`() = runBlocking {
        createVaultForTest()

        assertTrue(repository.enableBiometricUnlock(plainEncryptCipher()).isSuccess)
    }

    @Test
    fun `鎖定後，無法用殘留的金鑰副本啟用生物辨識`() = runBlocking {
        createVaultForTest()

        repository.lock()

        assertTrue(repository.enableBiometricUnlock(plainEncryptCipher()).isFailure)
    }

    @Test
    fun `重設保險箱後，無法用殘留的金鑰副本啟用生物辨識`() = runBlocking {
        createVaultForTest()

        repository.resetVault()

        assertTrue(repository.enableBiometricUnlock(plainEncryptCipher()).isFailure)
    }

    @Test
    fun `密碼錯誤解鎖失敗時，不會留下金鑰副本`() = runBlocking {
        createVaultForTest()
        repository.lock()

        val wrongUnlock = repository.unlock(WRONG_PASSWORD.toCharArray())

        assertTrue("錯誤密碼應該解鎖失敗", wrongUnlock.isFailure)
        assertTrue(repository.enableBiometricUnlock(plainEncryptCipher()).isFailure)
    }

    // 測試會建立並刪除真正的保險箱（檔名不可更改，無法另用測試檔）；
    // 裝置上已有保險箱時跳過，避免刪掉使用者資料
    private suspend fun createVaultForTest() {
        assumeFalse(
            "裝置上已有保險箱，跳過以免刪除資料",
            secureStore.hasVault() || context.getDatabasePath(DB_NAME).exists()
        )
        createdVaultInTest = true
        assertTrue("建立保險箱失敗", repository.unlock(TEST_PASSWORD.toCharArray()).isSuccess)
    }

    // 一般 AES Cipher，不經過 Keystore 與生物辨識，只用來觀察 repository 是否還留有金鑰副本
    private fun plainEncryptCipher(): Cipher {
        val key = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()
        return Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, key) }
    }

    private companion object {
        const val DB_NAME = "vault.db"
        const val SECURE_PREFS_NAME = "vault_secure_prefs"
        const val TEST_PASSWORD = "instrumented-test-password"
        const val WRONG_PASSWORD = "wrong-password"
    }
}
