package com.raita.vaultic.presentation.unlock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raita.vaultic.domain.usecase.ResetVaultUseCase
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

class UnlockViewModel(
    private val unlockVaultUseCase: UnlockVaultUseCase,
    private val resetVaultUseCase: ResetVaultUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<UnlockState>(UnlockState.Idle)
    val state: StateFlow<UnlockState> = _state.asStateFlow()

    private val _showResetConfirmation = MutableStateFlow(false)
    val showResetConfirmation: StateFlow<Boolean> = _showResetConfirmation.asStateFlow()

    private val _resetCompleted = MutableStateFlow(false)
    val resetCompleted: StateFlow<Boolean> = _resetCompleted.asStateFlow()

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

    fun onForgotPasswordClicked() {
        _showResetConfirmation.value = true
    }

    fun onCancelReset() {
        _showResetConfirmation.value = false
    }

    fun onConfirmReset() {
        viewModelScope.launch {
            _showResetConfirmation.value = false
            resetVaultUseCase()
            _state.value = UnlockState.Idle
            _resetCompleted.value = true
        }
    }

    fun acknowledgeResetCompleted() {
        _resetCompleted.value = false
    }
}