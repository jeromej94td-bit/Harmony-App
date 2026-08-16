package com.example.ui

/** Complete Portuguese (Portugal) locale pack. */
internal val EXACT_PORTUGUESE_PORTUGAL_CONTENT: Map<String, String> = EXACT_PORTUGUESE_CONTENT

private fun exactPortuguesePortugal(text: String): String =
    TranslationCatalog.exact(text, AppLanguage.PORTUGUESE_PORTUGAL) ?: text

internal fun localizePortuguesePortugalDynamicContent(text: String): String? =
    localizePortugueseDynamicContent(text)
