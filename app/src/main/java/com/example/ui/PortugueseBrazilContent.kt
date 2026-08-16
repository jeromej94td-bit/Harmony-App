package com.example.ui

/** Complete Portuguese (Brazil) locale pack. */
internal val EXACT_PORTUGUESE_BRAZIL_CONTENT: Map<String, String> = EXACT_PORTUGUESE_CONTENT

private fun exactPortugueseBrazil(text: String): String =
    TranslationCatalog.exact(text, AppLanguage.PORTUGUESE_BRAZIL) ?: text

internal fun localizePortugueseBrazilDynamicContent(text: String): String? =
    localizePortugueseDynamicContent(text)
