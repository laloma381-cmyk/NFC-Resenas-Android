package com.nfcresenas.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NFCResenasColors = lightColorScheme(
    primary = Color(0xFF0A5EDB),
    onPrimary = Color.White,
    secondary = Color(0xFF1677E8),
    onSecondary = Color.White,
    background = Color(0xFFF4F7FC),
    onBackground = Color(0xFF10243A),
    surface = Color.White,
    onSurface = Color(0xFF10243A),
    error = Color(0xFFB3261E),
    onError = Color.White
)

@Composable
fun NFCResenasTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NFCResenasColors,
        content = content
    )
}
