package com.raita.vaultic.presentation.unlock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raita.vaultic.domain.usecase.UnlockVaultUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface UnlockState {
    data object Idle : UnlockState
    data object Loading : UnlockState
    data object Unlocked : UnlockState
    data class Error(val message: String) : UnlockState
}

class UnlockViewModel(private val unlockVaultUseCase: UnlockVaultUseCase) : ViewModel() {

    private val _state = MutableStateFlow<UnlockState>(UnlockState.Idle)
    val state: StateFlow<UnlockState> = _state.asStateFlow()

    fun onUnlock(password: CharArray) {
        if (_state.value is UnlockState.Loading) return
        viewModelScope.launch {
            _state.value = UnlockState.Loading
            unlockVaultUseCase(password).fold(
                onSuccess = { _state.value = UnlockState.Unlocked },
                onFailure = { _state.value = UnlockState.Error("密碼錯誤,請再試一次") }
            )
        }
    }

    fun resetError() {
        if (_state.value is UnlockState.Error) _state.value = UnlockState.Idle
    }
}