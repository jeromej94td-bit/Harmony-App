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
        AppLanguage.DUTCH -> EXACT_DUTCH_CONTENT[german]
        AppLanguage.SWEDISH -> EXACT_SWEDISH_CONTENT[german]
        AppLanguage.ICELANDIC -> EXACT_ICELANDIC_CONTENT[german]
        AppLanguage.KOREAN -> EXACT_KOREAN_CONTENT[german]
        AppLanguage.CHINESE_SIMPLIFIED -> EXACT_CHINESE_SIMPLIFIED_CONTENT[german]
        AppLanguage.CHINESE_TRADITIONAL -> EXACT_CHINESE_TRADITIONAL_CONTENT[german]
        AppLanguage.HUNGARIAN -> EXACT_HUNGARIAN_CONTENT[german]
        AppLanguage.ROMANIAN -> EXACT_ROMANIAN_CONTENT[german]
        AppLanguage.BULGARIAN -> EXACT_BULGARIAN_CONTENT[german]
        AppLanguage.UKRAINIAN -> EXACT_UKRAINIAN_CONTENT[german]
        AppLanguage.RUSSIAN -> EXACT_RUSSIAN_CONTENT[german]
        AppLanguage.GREEK -> EXACT_GREEK_CONTENT[german]
        AppLanguage.TURKISH -> EXACT_TURKISH_CONTENT[german]
        AppLanguage.ARABIC -> EXACT_ARABIC_CONTENT[german]
        AppLanguage.HEBREW -> EXACT_HEBREW_CONTENT[german]
        AppLanguage.PERSIAN -> EXACT_PERSIAN_CONTENT[german]
        AppLanguage.HINDI -> EXACT_HINDI_CONTENT[german]
        AppLanguage.BENGALI -> EXACT_BENGALI_CONTENT[german]
        AppLanguage.URDU -> EXACT_URDU_CONTENT[german]
        AppLanguage.TAMIL -> EXACT_TAMIL_CONTENT[german]
        AppLanguage.TELUGU -> EXACT_TELUGU_CONTENT[german]
        AppLanguage.MARATHI -> EXACT_MARATHI_CONTENT[german]
        AppLanguage.GUJARATI -> EXACT_GUJARATI_CONTENT[german]
        AppLanguage.KANNADA -> EXACT_KANNADA_CONTENT[german]
        AppLanguage.MALAYALAM -> EXACT_MALAYALAM_CONTENT[german]
        AppLanguage.THAI -> EXACT_THAI_CONTENT[german]
        AppLanguage.VIETNAMESE -> EXACT_VIETNAMESE_CONTENT[german]
        AppLanguage.INDONESIAN -> EXACT_INDONESIAN_CONTENT[german]
        AppLanguage.MALAY -> EXACT_MALAY_CONTENT[german]
        AppLanguage.FILIPINO -> EXACT_FILIPINO_CONTENT[german]
        AppLanguage.BURMESE -> EXACT_BURMESE_CONTENT[german]
        AppLanguage.KHMER -> EXACT_KHMER_CONTENT[german]
        AppLanguage.LAO -> EXACT_LAO_CONTENT[german]
        AppLanguage.SWAHILI -> EXACT_SWAHILI_CONTENT[german]
        AppLanguage.AFRIKAANS -> EXACT_AFRIKAANS_CONTENT[german]
        AppLanguage.AMHARIC -> EXACT_AMHARIC_CONTENT[german]
        AppLanguage.YORUBA -> EXACT_YORUBA_CONTENT[german]
        AppLanguage.IGBO -> EXACT_IGBO_CONTENT[german]
        AppLanguage.HAUSA -> EXACT_HAUSA_CONTENT[german]
        AppLanguage.ZULU -> EXACT_ZULU_CONTENT[german]
        AppLanguage.XHOSA -> EXACT_XHOSA_CONTENT[german]
        AppLanguage.SOMALI -> EXACT_SOMALI_CONTENT[german]
        AppLanguage.ESTONIAN -> EXACT_ESTONIAN_CONTENT[german]
        AppLanguage.LATVIAN -> EXACT_LATVIAN_CONTENT[german]
        AppLanguage.LITHUANIAN -> EXACT_LITHUANIAN_CONTENT[german]
        AppLanguage.SLOVENIAN -> EXACT_SLOVENIAN_CONTENT[german]
        AppLanguage.SERBIAN -> EXACT_SERBIAN_CONTENT[german]
    }

    fun exact(german: String, language: AppLanguage): String? {
        if (language == AppLanguage.GERMAN) return german

        if (language == AppLanguage.PORTUGUESE_BRAZIL) {
            PT_BR_REVIEWED_OVERRIDES[german]?.let { return it }
            LOCALIZATION_UPDATES_PORTUGUESE_BRAZIL[german]?.let { return it }
        }

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
            AppLanguage.PORTUGUESE_BRAZIL -> localizeBrazilianPortugueseDynamicContent(text) ?: localizePortugueseDynamicContent(text)
            AppLanguage.PORTUGUESE_PORTUGAL -> localizePortuguesePortugalDynamicContent(text) ?: localizePortugueseDynamicContent(text)
            AppLanguage.DANISH -> localizeDanishDynamicContent(text)
            AppLanguage.NORWEGIAN -> localizeNorwegianDynamicContent(text)
            AppLanguage.DUTCH -> localizeDutchDynamicContent(text)
            AppLanguage.SWEDISH -> localizeSwedishDynamicContent(text)
            AppLanguage.ICELANDIC -> localizeIcelandicDynamicContent(text)
            AppLanguage.KOREAN -> localizeKoreanDynamicContent(text)
            AppLanguage.CHINESE_SIMPLIFIED -> localizeChineseSimplifiedDynamicContent(text)
            AppLanguage.CHINESE_TRADITIONAL -> localizeChineseTraditionalDynamicContent(text)
            AppLanguage.HUNGARIAN -> localizeHungarianDynamicContent(text)
            AppLanguage.ROMANIAN -> localizeRomanianDynamicContent(text)
            AppLanguage.BULGARIAN -> localizeBulgarianDynamicContent(text)
            AppLanguage.UKRAINIAN -> localizeUkrainianDynamicContent(text)
            AppLanguage.RUSSIAN -> localizeRussianDynamicContent(text)
            AppLanguage.GREEK -> localizeGreekDynamicContent(text)
            AppLanguage.TURKISH -> localizeTurkishDynamicContent(text)
            AppLanguage.ARABIC -> localizeArabicDynamicContent(text)
            AppLanguage.HEBREW -> localizeHebrewDynamicContent(text)
            AppLanguage.PERSIAN -> localizePersianDynamicContent(text)
            AppLanguage.HINDI -> localizeHindiDynamicContent(text)
            AppLanguage.BENGALI -> localizeBengaliDynamicContent(text)
            AppLanguage.URDU -> localizeUrduDynamicContent(text)
            AppLanguage.TAMIL -> localizeTamilDynamicContent(text)
            AppLanguage.TELUGU -> localizeTeluguDynamicContent(text)
            AppLanguage.MARATHI -> localizeMarathiDynamicContent(text)
            AppLanguage.GUJARATI -> localizeGujaratiDynamicContent(text)
            AppLanguage.KANNADA -> localizeKannadaDynamicContent(text)
            AppLanguage.MALAYALAM -> localizeMalayalamDynamicContent(text)
            AppLanguage.THAI -> localizeThaiDynamicContent(text)
            AppLanguage.VIETNAMESE -> localizeVietnameseDynamicContent(text)
            AppLanguage.INDONESIAN -> localizeIndonesianDynamicContent(text)
            AppLanguage.MALAY -> localizeMalayDynamicContent(text)
            AppLanguage.FILIPINO -> localizeFilipinoDynamicContent(text)
            AppLanguage.BURMESE -> localizeBurmeseDynamicContent(text)
            AppLanguage.KHMER -> localizeKhmerDynamicContent(text)
            AppLanguage.LAO -> localizeLaoDynamicContent(text)
            AppLanguage.SWAHILI -> localizeSwahiliDynamicContent(text)
            AppLanguage.AFRIKAANS -> localizeAfrikaansDynamicContent(text)
            AppLanguage.AMHARIC -> localizeAmharicDynamicContent(text)
            AppLanguage.YORUBA -> localizeYorubaDynamicContent(text)
            AppLanguage.IGBO -> localizeIgboDynamicContent(text)
            AppLanguage.HAUSA -> localizeHausaDynamicContent(text)
            AppLanguage.ZULU -> localizeZuluDynamicContent(text)
            AppLanguage.XHOSA -> localizeXhosaDynamicContent(text)
            AppLanguage.SOMALI -> localizeSomaliDynamicContent(text)
            AppLanguage.ESTONIAN -> localizeEstonianDynamicContent(text)
            AppLanguage.LATVIAN -> localizeLatvianDynamicContent(text)
            AppLanguage.LITHUANIAN -> localizeLithuanianDynamicContent(text)
            AppLanguage.SLOVENIAN -> localizeSlovenianDynamicContent(text)
            AppLanguage.SERBIAN -> localizeSerbianDynamicContent(text)
        }
    }
}
