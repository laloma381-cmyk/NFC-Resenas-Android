package com.nfcresenas.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val ReviewQrColors = lightColorScheme(
    primary = Color(0xFF245BDB),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEAF0FF),
    onPrimaryContainer = Color(0xFF142B62),
    secondary = Color(0xFF4D6FAE),
    onSecondary = Color.White,
    background = Color(0xFFF4F6FB),
    onBackground = Color(0xFF172746),
    surface = Color.White,
    onSurface = Color(0xFF172746),
    surfaceVariant = Color(0xFFEDF1F7),
    onSurfaceVariant = Color(0xFF718096),
    error = Color(0xFFB3261E),
    onError = Color.White
)

private val ReviewQrTypography = Typography().run {
    copy(
        headlineLarge = headlineLarge.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp),
        headlineMedium = headlineMedium.copy(fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.35).sp),
        headlineSmall = headlineSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = (-0.2).sp),
        titleLarge = titleLarge.copy(fontWeight = FontWeight.Bold),
        titleMedium = titleMedium.copy(fontWeight = FontWeight.SemiBold)
    )
}

@Composable
fun NFCResenasTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ReviewQrColors,
        typography = ReviewQrTypography,
        content = content
    )
}
