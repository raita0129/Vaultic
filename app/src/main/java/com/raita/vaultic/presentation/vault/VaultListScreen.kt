package com.raita.vaultic.presentation.vault

import android.graphics.drawable.Icon
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raita.vaultic.R
import com.raita.vaultic.data.crypto.BiometricCryptoManager
import com.raita.vaultic.domain.model.VaultEntry

@Composable
fun VaultListScreen(
    viewModel: VaultViewModel,
    onAddEntry: () -> Unit,
    onEditEntry: (VaultEntry) -> Unit,
    onLocked: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as FragmentActivity
    val entries by viewModel.entries.collectAsStateWithLifecycle(initialValue = emptyList())
    var query by rememberSaveable { mutableStateOf("") }

    val biometricEnabled by viewModel.isBiometricEnabled.collectAsStateWithLifecycle()
    val biometricAvailable = remember { BiometricCryptoManager.isBiometricAvailable(context) }
    val biometricError by viewModel.biometricError.collectAsStateWithLifecycle()
    val entryError by viewModel.entryError.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(biometricError) {
        biometricError?.let {
            snackbarHostState.showSnackbar(context.getString(it))
            viewModel.consumeBiometricError()
        }
    }

    LaunchedEffect(entryError) {
        entryError?.let {
            snackbarHostState.showSnackbar(context.getString(it))
            viewModel.consumeEntryError()
        }
    }

    val biometricPrompt = remember {
        BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    val cipher = result.cryptoObject?.cipher ?: return
                    viewModel.onBiometricEnableSuccess(cipher)
                }
            }
        )
    }

    val promptInfo = remember {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle(context.getString(R.string.vault_biometric_enable_prompt_title))
            .setNegativeButtonText(context.getString(R.string.cancel))
            .build()
    }

    val filtered = remember(entries, query) {
        if (query.isBlank()) entries
        else entries.filter { it.title.contains(query, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    if (biometricAvailable) {
                        IconButton(onClick = {
                            if (biometricEnabled) {
                                viewModel.disableBiometricUnlock()
                            } else {
                                viewModel.getBiometricEnableCipher()?.let { cipher ->
                                    biometricPrompt.authenticate(
                                        promptInfo,
                                        BiometricPrompt.CryptoObject(cipher)
                                    )
                                }
                            }
                        }) {
                            Icon(
                                Icons.Outlined.Fingerprint,
                                contentDescription = stringResource(
                                    if (biometricEnabled) R.string.vault_cd_disable_biometric
                                    else R.string.vault_cd_enable_biometric
                                ),
                                tint = if (biometricEnabled) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    LocalContentColor.current
                                }
                            )
                        }
                    }
                    IconButton(onClick = {
                        viewModel.lock()
                        onLocked()
                    }) {
                        Icon(Icons.Outlined.Lock, contentDescription = stringResource(R.string.vault_cd_lock))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddEntry) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.vault_cd_add_entry))
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text(stringResource(R.string.vault_search)) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            if (filtered.isEmpty()) {
                EmptyVaultState(hasQuery = query.isNotBlank())
            } else {
                LazyColumn {
                    items(filtered, key = { it.id }) { entry ->
                        VaultEntryRow(
                            entry = entry,
                            onClick = { onEditEntry(entry) },
                            onCopyPassword = { viewModel.copyToClipboard(context, entry) },
                            onDelete = { viewModel.deleteEntry(entry.id) }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun VaultEntryRow(
    entry: VaultEntry,
    onClick: () -> Unit,
    onCopyPassword: () -> Unit,
    onDelete: () -> Unit
) {
    ListItem(
        headlineContent = { Text(entry.title) },
        supportingContent = { Text(entry.username, style = MaterialTheme.typography.bodySmall) },
        trailingContent = {
            Row {
                IconButton(onClick = onCopyPassword) {
                    Icon(Icons.Outlined.ContentCopy, contentDescription = stringResource(R.string.vault_cd_copy_password))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.vault_cd_delete))
                }
            }
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
private fun EmptyVaultState(hasQuery: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(if (hasQuery) R.string.vault_empty_no_results else R.string.vault_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (!hasQuery) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.vault_empty_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}