package com.raita.vaultic.presentation.unlock

import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun UnlockScreen(viewModel: UnlockViewModel, onUnlocked: () -> Unit) {

    val state by viewModel.state.collectAsStateWithLifecycle()
    val showResetConfirmation by viewModel.showResetConfirmation.collectAsStateWithLifecycle()
    val resetCompleted by viewModel.resetCompleted.collectAsStateWithLifecycle()
    var password by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val activity = LocalContext.current as FragmentActivity

    LaunchedEffect(state) {
        if (state is UnlockState.Unlocked) onUnlocked()
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    val biometricPrompt = remember {
        BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    val cipher = result.cryptoObject?.cipher ?: return
                    viewModel.onBiometricUnlockSuccess(cipher)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    viewModel.onBiometricUnlockCancelled()
                }
            }
        )
    }

    val promptInfo = remember {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("使用生物辨識解鎖 Vaultic")
            .setNegativeButtonText("改用主密碼")
            .build()
    }

    LaunchedEffect(Unit) {
        if (viewModel.isBiometricEnabled) {
            viewModel.getBiometricDecryptCipher()?.let { cipher ->
                biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Icon(
            imageVector = Icons.Outlined.Lock,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(16.dp))

        Text(text = "輸入主密碼解鎖", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                viewModel.resetError()
            },
            label = { Text("主密碼") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = state is UnlockState.Error,
            supportingText = {
                val current = state
                if (current is UnlockState.Error) Text(current.message)
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { viewModel.onUnlock(password.toCharArray()) },
            enabled = password.isNotBlank() && state !is UnlockState.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state is UnlockState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("解鎖")
            }
        }

        if (viewModel.isBiometricEnabled) {
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = {
                viewModel.getBiometricDecryptCipher()?.let { cipher ->
                    biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
                }
            }) {
                Text("使用生物辨識解鎖")
            }
        }

        Spacer(Modifier.height(12.dp))

        TextButton(onClick = { viewModel.onForgotPasswordClicked() }) {
            Text("忘記密碼?")
        }
    }

    if (showResetConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.onCancelReset() },
            title = { Text("清空保險箱?") },
            text = {
                Text(
                    "忘記主密碼無法找回原本的資料,只能清空所有已儲存的密碼,\n" +
                            "重新設定一組新的主密碼。此動作無法復原,確定要繼續嗎?"
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onConfirmReset() }) {
                    Text("確定清空", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onCancelReset() }) {
                    Text("取消")
                }
            }
        )
    }

    LaunchedEffect(resetCompleted) {
        if (resetCompleted) {
            password = ""
            viewModel.acknowledgeResetCompleted()
        }
    }
}