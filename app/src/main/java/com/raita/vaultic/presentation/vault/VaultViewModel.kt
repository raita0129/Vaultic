package com.raita.vaultic.presentation.vault

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.PersistableBundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raita.vaultic.domain.model.VaultEntry
import com.raita.vaultic.domain.repository.VaultRepository
import com.raita.vaultic.domain.usecase.AddEntryUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class VaultViewModel(
    private val repository: VaultRepository,
    private val addEntryUseCase: AddEntryUseCase = AddEntryUseCase(repository)
) : ViewModel() {

    val entries: Flow<List<VaultEntry>> = repository.observeEntries().catch { emit(emptyList()) }

    fun addEntry(title: String, username: String, password: String) {
        viewModelScope.launch { addEntryUseCase(title, username, password) }
    }

    fun deleteEntry(id: String) {
        viewModelScope.launch { repository.deleteEntry(id) }
    }

    fun lock() {
        viewModelScope.launch { repository.lock() }
    }

    fun copyToClipboard(context: Context, entry: VaultEntry) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("password", entry.password).apply {
            description.extras = PersistableBundle().apply {
                putBoolean("android.content.extra.IS_SENSITIVE", true)
            }
        }
        clipboard.setPrimaryClip(clip)
    }
}