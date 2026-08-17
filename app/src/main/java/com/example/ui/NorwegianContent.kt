package com.example.ui

/** Norwegian locale pack. */
internal val EXACT_NORWEGIAN_CONTENT: Map<String, String> = EXACT_ENGLISH_CONTENT

internal fun localizeNorwegianDynamicContent(text: String): String? =
    localizeEnglishDynamicContent(text)
