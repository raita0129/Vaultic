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
import com.raita.vaultic.domain.usecase.EnableBiometricUnlockUseCase
import com.raita.vaultic.domain.usecase.UpdateEntryUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.crypto.Cipher

class VaultViewModel(
    private val repository: VaultRepository,
    private val addEntryUseCase: AddEntryUseCase = AddEntryUseCase(repository),
    private val updateEntryUseCase: UpdateEntryUseCase = UpdateEntryUseCase(repository),
    private val enableBiometricUnlockUseCase: EnableBiometricUnlockUseCase = EnableBiometricUnlockUseCase(
        repository
    )
) : ViewModel() {

    val entries: Flow<List<VaultEntry>> = repository.observeEntries().catch { emit(emptyList()) }

    private val _isBiometricEnabled = MutableStateFlow(repository.isBiometricEnabled())
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    private val _biometricError = MutableStateFlow<String?>(null)
    val biometricError: StateFlow<String?> = _biometricError.asStateFlow()

    fun getBiometricEnableCipher(): Cipher? = repository.getBiometricEncryptCipher()

    fun onBiometricEnableSuccess(cipher: Cipher) {
        viewModelScope.launch {
            enableBiometricUnlockUseCase(cipher)
                .onSuccess { _isBiometricEnabled.value = true }
                .onFailure { _biometricError.value = it.message ?: "啟用生物辨識失敗" }
        }
    }

    fun consumeBiometricError() {
        _biometricError.value = null
    }

    fun disableBiometricUnlock() {
        viewModelScope.launch {
            repository.disableBiometricUnlock()
            _isBiometricEnabled.value = false
        }
    }

    fun addEntry(title: String, username: String, password: String, note: String) {
        viewModelScope.launch { addEntryUseCase(title, username, password, note) }
    }

    fun updateEntry(id: String, title: String, username: String, password: String, note: String) {
        viewModelScope.launch { updateEntryUseCase(id, title, username, password, note) }
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