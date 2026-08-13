package com.example.ui

import java.text.Normalizer

/**
 * Registry for locale packs. Adding a language only requires:
 * 1) one AppLanguage entry, 2) one exact translation map, 3) one registry entry here.
 */
internal object TranslationCatalog {
    private data class LocalePack(
        val exact: Map<String, String>,
        val normalizedExact: Map<String, String>,
        val dynamic: (String) -> String?
    )

    private val packs: Map<String, LocalePack> = mapOf(
        "en" to localePack(EXACT_ENGLISH_CONTENT, ::localizeEnglishDynamicContent),
        "it" to localePack(EXACT_ITALIAN_CONTENT, ::localizeItalianDynamicContent),
        "pt" to localePack(EXACT_PORTUGUESE_CONTENT, ::localizePortugueseDynamicContent),
        "no" to localePack(EXACT_NORWEGIAN_CONTENT, ::localizeNorwegianDynamicContent)
    )

    private fun localePack(
        exact: Map<String, String>,
        dynamic: (String) -> String?
    ): LocalePack = LocalePack(
        exact = exact,
        normalizedExact = exact.entries.associate { (source, translation) ->
            normalizeKey(source) to translation
        },
        dynamic = dynamic
    )

    private fun normalizeKey(source: String): String =
        Normalizer.normalize(source, Normalizer.Form.NFC)
            .trim()
            .replace(Regex("\\s+"), " ")

    fun exact(source: String, language: AppLanguage): String? =
        packs[language.code]?.let { pack ->
            pack.exact[source.trim()] ?: pack.normalizedExact[normalizeKey(source)]
        }

    fun translate(source: String, language: AppLanguage): String? {
        val pack = packs[language.code] ?: return null
        return pack.exact[source.trim()]
            ?: pack.normalizedExact[normalizeKey(source)]
            ?: pack.dynamic(source)
    }

    fun hasCompletePack(language: AppLanguage): Boolean =
        language == AppLanguage.GERMAN || packs.containsKey(language.code)
}
