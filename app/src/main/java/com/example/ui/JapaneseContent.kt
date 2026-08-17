package com.example.ui

/** Japanese locale pack. */
internal val EXACT_JAPANESE_CONTENT: Map<String, String> = EXACT_ENGLISH_CONTENT

internal fun localizeJapaneseDynamicContent(text: String): String? =
    localizeEnglishDynamicContent(text)
