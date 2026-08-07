package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val HarmonyColorScheme = darkColorScheme(
  primary = HarmonyPink,
  onPrimary = Color.White,
  primaryContainer = HarmonySurface2,
  onPrimaryContainer = HarmonyText,
  secondary = HarmonyPurple,
  onSecondary = Color.White,
  tertiary = HarmonyGold,
  onTertiary = Color.Black,
  background = HarmonyBg,
  onBackground = HarmonyText,
  surface = HarmonySurface,
  onSurface = HarmonyText,
  surfaceVariant = HarmonySurface2,
  onSurfaceVariant = HarmonyMuted,
  outline = HarmonyLine
)

@Composable
fun HarmonyTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = HarmonyColorScheme,
    typography = Typography,
    content = content
  )
}

