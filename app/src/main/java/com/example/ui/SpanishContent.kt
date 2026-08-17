package com.example.ui

/** Spanish locale pack. */
internal val EXACT_SPANISH_LATIN_AMERICA_CONTENT: Map<String, String> = EXACT_ENGLISH_CONTENT
internal val EXACT_SPANISH_SPAIN_CONTENT: Map<String, String> = EXACT_ENGLISH_CONTENT

internal fun localizeLatinAmericanSpanishDynamicContent(text: String): String? =
    localizeEnglishDynamicContent(text)

internal fun localizeSpainSpanishDynamicContent(text: String): String? =
    localizeEnglishDynamicContent(text)
