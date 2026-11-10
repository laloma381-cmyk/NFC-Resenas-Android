package com.nfcresenas.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NFCResenasColors = darkColorScheme(
    primary = Color(0xFF0A5EDB),
    secondary = Color(0xFFFFC107),
    background = Color(0xFFF5F8FF),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onBackground = Color(0xFF0B1F33),
    onSurface = Color(0xFF0B1F33)
)

@Composable
fun NFCResenasTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NFCResenasColors,
        content = content
    )
}
