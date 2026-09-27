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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.raita.vaultic.R

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
            .setTitle(activity.getString(R.string.unlock_biometric_prompt_title))
            .setNegativeButtonText(activity.getString(R.string.unlock_biometric_prompt_negative))
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

        Text(text = stringResource(R.string.unlock_title), style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                viewModel.resetError()
            },
            label = { Text(stringResource(R.string.unlock_master_password)) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = state is UnlockState.Error,
            supportingText = {
                val current = state
                if (current is UnlockState.Error) Text(stringResource(current.messageRes))
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
                Text(stringResource(R.string.unlock_button))
            }
        }

        if (viewModel.isBiometricEnabled) {
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = {
                viewModel.getBiometricDecryptCipher()?.let { cipher ->
                    biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
                }
            }) {
                Text(stringResource(R.string.unlock_with_biometric))
            }
        }

        Spacer(Modifier.height(12.dp))

        TextButton(onClick = { viewModel.onForgotPasswordClicked() }) {
            Text(stringResource(R.string.unlock_forgot_password))
        }
    }

    if (showResetConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.onCancelReset() },
            title = { Text(stringResource(R.string.reset_dialog_title)) },
            text = { Text(stringResource(R.string.reset_dialog_message)) },
            confirmButton = {
                TextButton(onClick = { viewModel.onConfirmReset() }) {
                    Text(stringResource(R.string.reset_dialog_confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onCancelReset() }) {
                    Text(stringResource(R.string.cancel))
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