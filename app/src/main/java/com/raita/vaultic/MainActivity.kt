package com.raita.vaultic

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.raita.vaultic.domain.repository.VaultRepository
import com.raita.vaultic.navigation.VaulticNavHost
import com.raita.vaultic.ui.theme.VaulticTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 保險箱內容包含明文密碼,擋掉截圖/螢幕錄影/多工預覽,避免外洩
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)

        val repository = (application as VaulticApp).repository

        setContent {
            VaulticTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    VaulticRoot(repository = repository)
                }
            }
        }
    }
}

@Composable
private fun VaulticRoot(repository: VaultRepository) {
    VaulticNavHost(repository = repository)
}