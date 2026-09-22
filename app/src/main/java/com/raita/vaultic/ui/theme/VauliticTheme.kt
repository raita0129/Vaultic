package com.raita.vaultic.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val VaulticColorScheme = darkColorScheme(
    primary = Color(0xFF4FA3D1),
    onPrimary = Color(0xFF00263A),
    background = Color(0xFF121212),
    onBackground = Color(0xFFE0E0E0),
    surface = Color(0xFF1E1E1E),
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0xFFB0B0B0),
    error = Color(0xFFCF6679)
)

@Composable
fun VaulticTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VaulticColorScheme,
        typography = Typography(),
        content = content
    )
}