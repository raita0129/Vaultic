package com.raita.vaultic.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.raita.vaultic.domain.repository.VaultRepository
import com.raita.vaultic.domain.usecase.UnlockVaultUseCase
import com.raita.vaultic.presentation.unlock.UnlockScreen
import com.raita.vaultic.presentation.unlock.UnlockViewModel
import com.raita.vaultic.presentation.vault.AddEntryScreen
import com.raita.vaultic.presentation.vault.VaultListScreen
import com.raita.vaultic.presentation.vault.VaultViewModel

sealed class Screen(val route: String) {
    data object Unlock : Screen("unlock")
    data object VaultList : Screen("vault_list")
    data object AddEntry : Screen("add_entry")
}

@Composable
fun VaulticNavHost(
    repository: VaultRepository,
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = Screen.Unlock.route) {
        composable(Screen.Unlock.route) {
            val viewModel = viewModel { UnlockViewModel(UnlockVaultUseCase(repository)) }
            UnlockScreen(
                viewModel = viewModel,
                onUnlocked = {
                    navController.navigate(Screen.VaultList.route) {
                        popUpTo(Screen.Unlock.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.VaultList.route) {
            val viewModel = viewModel { VaultViewModel(repository) }
            VaultListScreen(
                viewModel = viewModel,
                onAddEntry = { navController.navigate(Screen.AddEntry.route) },
                onLocked = {
                    navController.navigate(Screen.Unlock.route) {
                        popUpTo(Screen.VaultList.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.AddEntry.route) {
            val viewModel = viewModel { VaultViewModel(repository) }
            AddEntryScreen(
                viewModel = viewModel,
                onDone = { navController.popBackStack() }
            )
        }
    }
}