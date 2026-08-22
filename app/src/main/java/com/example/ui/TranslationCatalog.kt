package com.example.ui

/**
 * Central catalog providing lookups and dynamic localization for all supported app languages.
 * Repair overrides are generated at build time and committed; runtime remains fully offline.
 */
object TranslationCatalog {

    private val nonCustomerKeys = setOf(
        "Entwickler Studio Öffnen", "Entwickler-Modus", "🛠️ Entwickler-Modus",
        "Spiele & Städte bearbeiten, Ordner reinladen, Bilder anpassen",
        ", listOf(", "aufwaermen", "custom_gourmet_eissorten", "dasoderdas", "disney",
        "entertainment", "essen", "familie", "games", "harrypotter", "hochzeit", "iPhone",
        "ichhabenochnie", "kinder", "oder", "parks", "party", "reden", "reisen", "tot",
        "universal", "unterhaltung", "wer", "werwuerde", "zuhause", "{partner}", "{user}",
        "☀️", "❤️"
    )

    fun hasCompletePack(language: AppLanguage): Boolean {
        if (language == AppLanguage.GERMAN) return true
        // Brazilian Portuguese is intentionally kept on its pre-repair implementation for now.
        if (language == AppLanguage.PORTUGUESE_BRAZIL) return true
        return EXACT_ENGLISH_CONTENT.keys
            .asSequence()
            .filterNot { it in nonCustomerKeys || "Entwickler" in it }
            .all { exact(it, language) != null }
    }

    fun getTranslation(text: String, language: AppLanguage): String = translate(text, language) ?: ""

    private fun baseExact(german: String, language: AppLanguage): String? = when (language) {
        AppLanguage.GERMAN -> german
        AppLanguage.ENGLISH -> EXACT_ENGLISH_CONTENT[german]
        AppLanguage.ITALIAN -> EXACT_ITALIAN_CONTENT[german]
        AppLanguage.FRENCH -> EXACT_FRENCH_CONTENT[german]
        AppLanguage.JAPANESE -> EXACT_JAPANESE_CONTENT[german]
        AppLanguage.POLISH -> EXACT_POLISH_CONTENT[german]
        AppLanguage.SPANISH_LATIN_AMERICA -> EXACT_SPANISH_LATIN_AMERICA_CONTENT[german]
        AppLanguage.SPANISH_SPAIN -> EXACT_SPANISH_SPAIN_CONTENT[german]
        AppLanguage.PORTUGUESE_BRAZIL -> EXACT_PORTUGUESE_BRAZIL_CONTENT[german] ?: EXACT_PORTUGUESE_CONTENT[german]
        AppLanguage.PORTUGUESE_PORTUGAL -> EXACT_PORTUGUESE_PORTUGAL_CONTENT[german] ?: EXACT_PORTUGUESE_CONTENT[german]
        AppLanguage.DANISH -> EXACT_DANISH_CONTENT[german]
        AppLanguage.NORWEGIAN -> EXACT_NORWEGIAN_CONTENT[german]
    }

    fun exact(german: String, language: AppLanguage): String? {
        if (language == AppLanguage.GERMAN) return german
        // Keep Brazilian Portuguese exactly on the pre-repair catalog until its own pass is ready.
        if (language == AppLanguage.PORTUGUESE_BRAZIL) return baseExact(german, language)
        // Reviewed Japanese video fixes must override stale legacy machine translations.
        if (language == AppLanguage.JAPANESE) {
            LOCALIZATION_UPDATES_JAPANESE[german]?.let { return it }
        }
        baseExact(german, language)?.let { return it }
        return LOCALIZATION_UPDATES[language]?.get(german)
    }

    fun translate(text: String, language: AppLanguage): String? {
        if (language == AppLanguage.GERMAN) return text
        exact(text, language)?.let { return it }
        return when (language) {
            AppLanguage.GERMAN -> text
            AppLanguage.ENGLISH -> localizeEnglishDynamicContent(text)
            AppLanguage.ITALIAN -> localizeItalianDynamicContent(text)
            AppLanguage.FRENCH -> localizeFrenchDynamicContent(text)
            AppLanguage.JAPANESE -> localizeJapaneseDynamicContent(text)
            AppLanguage.POLISH -> localizePolishDynamicContent(text)
            AppLanguage.SPANISH_LATIN_AMERICA -> localizeLatinAmericanSpanishDynamicContent(text)
            AppLanguage.SPANISH_SPAIN -> localizeSpainSpanishDynamicContent(text)
            AppLanguage.PORTUGUESE_BRAZIL -> localizePortugueseBrazilDynamicContent(text) ?: localizePortugueseDynamicContent(text)
            AppLanguage.PORTUGUESE_PORTUGAL -> localizePortuguesePortugalDynamicContent(text) ?: localizePortugueseDynamicContent(text)
            AppLanguage.DANISH -> localizeDanishDynamicContent(text)
            AppLanguage.NORWEGIAN -> localizeNorwegianDynamicContent(text)
        }
    }
}
