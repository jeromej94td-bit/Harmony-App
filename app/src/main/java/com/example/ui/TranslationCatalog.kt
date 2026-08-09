package com.example.ui

/**
 * Registry for locale packs. Adding a language only requires:
 * 1) one AppLanguage entry, 2) one exact translation map, 3) one registry entry here.
 */
internal object TranslationCatalog {
    private data class LocalePack(
        val exact: Map<String, String>,
        val dynamic: (String) -> String?
    )

    private val packs: Map<String, LocalePack> = mapOf(
        "en" to LocalePack(EXACT_ENGLISH_CONTENT, ::localizeEnglishDynamicContent)
    )

    fun exact(source: String, language: AppLanguage): String? =
        packs[language.code]?.exact?.get(source.trim())

    fun translate(source: String, language: AppLanguage): String? {
        val pack = packs[language.code] ?: return null
        return pack.exact[source.trim()] ?: pack.dynamic(source)
    }

    fun hasCompletePack(language: AppLanguage): Boolean =
        language == AppLanguage.GERMAN || packs.containsKey(language.code)
}
