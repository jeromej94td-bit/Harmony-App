package com.example.ui

/**
 * Central catalog providing lookups and dynamic localization for all supported app languages.
 */
object TranslationCatalog {

    fun hasCompletePack(language: AppLanguage): Boolean = true

    fun getTranslation(text: String, language: AppLanguage): String =
        translate(text, language) ?: ""

    fun exact(german: String, language: AppLanguage): String? {
        if (language == AppLanguage.GERMAN) return german
        return when (language) {
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
