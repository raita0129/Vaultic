package com.raita.vaultic.presentation.vault

import android.R
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
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raita.vaultic.domain.model.VaultEntry

@Composable
fun VaultListScreen(
    viewModel: VaultViewModel,
    onAddEntry: () -> Unit,
    onLocked: () -> Unit
) {
    val context = LocalContext.current
    val entries by viewModel.entries.collectAsStateWithLifecycle(initialValue = emptyList())
    var query by rememberSaveable { mutableStateOf("") }

    val filtered = remember(entries, query) {
        if (query.isBlank()) entries
        else entries.filter { it.title.contains(query, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vaultic") },
                actions = {
                    IconButton(onClick = {
                        viewModel.lock()
                        onLocked()
                    }) {
                        Icon(Icons.Outlined.Lock, contentDescription = "鎖定")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddEntry) {
                Icon(Icons.Filled.Add, contentDescription = "新增項目")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("搜尋") },
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
    onCopyPassword: () -> Unit,
    onDelete: () -> Unit
) {
    ListItem(
        headlineContent = { Text(entry.title) },
        supportingContent = { Text(entry.username, style = MaterialTheme.typography.bodySmall) },
        trailingContent = {
            Row {
                IconButton(onClick = onCopyPassword) {
                    Icon(Icons.Outlined.ContentCopy, contentDescription = "複製密碼")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Outlined.Delete, contentDescription = "刪除")
                }
            }
        }
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
            text = if (hasQuery) "找不到符合的項目" else "還沒有任何密碼",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (!hasQuery) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "點右下角[+]新增第一筆",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}