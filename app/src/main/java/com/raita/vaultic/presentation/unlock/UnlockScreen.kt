package com.raita.vaultic.presentation.unlock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun UnlockScreen(viewModel: UnlockViewModel, onUnlocked: () -> Unit) {

    val state by viewModel.state.collectAsStateWithLifecycle()
    val showResetConfirmation by viewModel.showResetConfirmation.collectAsStateWithLifecycle()
    val resetCompleted by viewModel.resetCompleted.collectAsStateWithLifecycle()
    var password by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(state) {
        if (state is UnlockState.Unlocked) onUnlocked()
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Column(
        modifier = Modifier
            .fillMaxSize()
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