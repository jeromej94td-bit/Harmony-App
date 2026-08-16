package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val HarmonyBg: Color @Composable get() = LocalHarmonyColors.current.bg
val HarmonyBgGradientStart: Color @Composable get() = LocalHarmonyColors.current.bgGradientStart
val HarmonyBgGradientMid: Color @Composable get() = LocalHarmonyColors.current.bgGradientMid
val HarmonyBgGradientEnd: Color @Composable get() = LocalHarmonyColors.current.bgGradientEnd

val HarmonySurface: Color @Composable get() = LocalHarmonyColors.current.surface
val HarmonySurface2: Color @Composable get() = LocalHarmonyColors.current.surface2
val HarmonyLine: Color @Composable get() = LocalHarmonyColors.current.line

val HarmonyPink = Color(0xFFFF2E63)
val HarmonyPinkSoft = Color(0xFFFF6B8F)
val HarmonyPurple = Color(0xFF9E59BD)
val HarmonyPurpleLight = Color(0xFFC89BE0)
val HarmonyGold = Color(0xFFFFC46B)
val HarmonyTeal = Color(0xFF7BD8CB)
val HarmonyBlue = Color(0xFF9DB2FF)

val HarmonyText: Color @Composable get() = LocalHarmonyColors.current.text
val HarmonyMuted: Color @Composable get() = LocalHarmonyColors.current.muted
val HarmonyNavActive = Color(0xFFFF2E63)
val HarmonyNavInactive: Color @Composable get() = LocalHarmonyColors.current.navInactive


