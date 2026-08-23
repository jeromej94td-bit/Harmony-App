package com.example.ui.screens

internal enum class HarmonyImageChoiceKind {
    EGG,
    STEAK,
    TRAVEL
}

internal fun harmonyImageChoiceKind(packId: String, questionIndex: Int): HarmonyImageChoiceKind? =
    when {
        packId == "essenreden" && questionIndex == 3 -> HarmonyImageChoiceKind.EGG
        packId == "essenreden" && questionIndex == 4 -> HarmonyImageChoiceKind.STEAK
        packId == "reisevor" && questionIndex == 4 -> HarmonyImageChoiceKind.TRAVEL
        else -> null
    }

internal fun harmonyImageChoiceRevealDelayMillis(index: Int): Long {
    val row = index / 3
    val column = index % 3
    return row * 420L + column * 110L
}
