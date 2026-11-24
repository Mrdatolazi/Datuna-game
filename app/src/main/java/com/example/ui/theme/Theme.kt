package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DatunaColorScheme =
  darkColorScheme(
    primary = DatunaGold,
    onPrimary = Color(0xFF1C1302),
    primaryContainer = DatunaGoldDeep,
    onPrimaryContainer = DatunaInk,
    secondary = DatunaCrimson,
    onSecondary = Color.White,
    tertiary = DatunaGoldLight,
    background = DatunaBg,
    onBackground = DatunaInk,
    surface = DatunaSurface,
    onSurface = DatunaInk,
    surfaceVariant = DatunaSurfaceElevated,
    onSurfaceVariant = DatunaMutedInk,
    outline = DatunaBorder,
  )

@Composable
fun DatunaTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = DatunaColorScheme,
    typography = Typography,
    content = content,
  )
}
