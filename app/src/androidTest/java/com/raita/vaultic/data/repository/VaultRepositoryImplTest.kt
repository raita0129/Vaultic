package com.raita.vaultic.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.raita.vaultic.data.crypto.SecureStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VaultRepositoryImplTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    // 模擬行程被回收後的新行程：全新的 repository，尚未解鎖
    private fun newLockedRepository() = VaultRepositoryImpl(context, SecureStore(context))

    @Test
    fun `未解鎖時呼叫observeEntries不丟例外，收集時才失敗`() {
        val repository = newLockedRepository()

        val flow = repository.observeEntries()

        val error = runBlocking { runCatching { flow.first() }.exceptionOrNull() }
        assertTrue("未解鎖時收集應該丟 IllegalStateException，實際：$error", error is IllegalStateException)
    }
}
