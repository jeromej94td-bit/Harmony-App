package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class HarmonyColors(
  val bg: Color,
  val bgGradientStart: Color,
  val bgGradientMid: Color,
  val bgGradientEnd: Color,
  val surface: Color,
  val surface2: Color,
  val line: Color,
  val text: Color,
  val muted: Color,
  val navInactive: Color
)

val DarkHarmonyColors = HarmonyColors(
  bg = Color(0xFF08030F),
  bgGradientStart = Color(0xFF160A24),
  bgGradientMid = Color(0xFF0D0618),
  bgGradientEnd = Color(0xFF05020B),
  surface = Color(0xFF171022),
  surface2 = Color(0xFF241536),
  line = Color(0x26E6C8FF),
  text = Color(0xFFF9F4FF),
  muted = Color(0xFFB7A7C8),
  navInactive = Color(0xFF8D7D9D)
)

val LightHarmonyColors = HarmonyColors(
  bg = Color(0xFFFAF2F6),
  bgGradientStart = Color(0xFFFFF7FB),
  bgGradientMid = Color(0xFFF7E6F0),
  bgGradientEnd = Color(0xFFEFE0EB),
  surface = Color(0xFFFFFFFF),
  surface2 = Color(0xFFF7ECF3),
  line = Color(0x1F000000),
  text = Color(0xFF2D1828),
  muted = Color(0xFF8A6C7C),
  navInactive = Color(0xFFA08896)
)

val LocalHarmonyColors = staticCompositionLocalOf { DarkHarmonyColors }

private val DarkColorScheme = darkColorScheme(
  primary = HarmonyPink,
  onPrimary = Color.White,
  primaryContainer = Color(0xFF2E1B2B),
  onPrimaryContainer = Color(0xFFF7EAF1),
  secondary = HarmonyPurple,
  onSecondary = Color.White,
  tertiary = HarmonyGold,
  onTertiary = Color.Black,
  background = Color(0xFF0B050A),
  onBackground = Color(0xFFF7EAF1),
  surface = Color(0xFF241522),
  onSurface = Color(0xFFF7EAF1),
  surfaceVariant = Color(0xFF2E1B2B),
  onSurfaceVariant = Color(0xFFAC8A9B),
  outline = Color(0x12FFFFFF)
)

private val LightColorScheme = lightColorScheme(
  primary = HarmonyPink,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFF7ECF3),
  onPrimaryContainer = Color(0xFF2D1828),
  secondary = HarmonyPurple,
  onSecondary = Color.White,
  tertiary = HarmonyGold,
  onTertiary = Color.Black,
  background = Color(0xFFFAF2F6),
  onBackground = Color(0xFF2D1828),
  surface = Color(0xFFFFFFFF),
  onSurface = Color(0xFF2D1828),
  surfaceVariant = Color(0xFFF7ECF3),
  onSurfaceVariant = Color(0xFF8A6C7C),
  outline = Color(0x1F000000)
)

@Composable
fun HarmonyTheme(
  darkTheme: Boolean = true,
  content: @Composable () -> Unit
) {
  val colors = if (darkTheme) DarkHarmonyColors else LightHarmonyColors
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  CompositionLocalProvider(LocalHarmonyColors provides colors) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = Typography,
      content = content
    )
  }
}


