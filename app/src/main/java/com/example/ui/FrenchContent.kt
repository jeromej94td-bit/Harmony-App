package com.example.ui

/** French locale pack. */
internal val EXACT_FRENCH_CONTENT: Map<String, String> = EXACT_ENGLISH_CONTENT

internal fun localizeFrenchDynamicContent(text: String): String? =
    localizeEnglishDynamicContent(text)
