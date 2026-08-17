package com.example.ui

/** Danish locale pack. */
internal val EXACT_DANISH_CONTENT: Map<String, String> = EXACT_ENGLISH_CONTENT

internal fun localizeDanishDynamicContent(text: String): String? =
    localizeEnglishDynamicContent(text)
