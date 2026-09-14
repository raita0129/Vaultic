package com.raita.vaultic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.raita.vaultic.domain.repository.VaultRepository
import com.raita.vaultic.navigation.VaulticNavHost
import com.raita.vaultic.ui.theme.VaulticTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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