package com.example.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppLanguageMetadataTest {

    @Test
    fun `every selectable locale has a unique code and a flag or regional emoji`() {
        val languages = AppLanguage.entries.filter(TranslationCatalog::hasCompletePack)
        assertEquals(languages.size, languages.map { it.code.lowercase() }.distinct().size)
        assertTrue(languages.all { it.flagEmoji.isNotBlank() })
        assertEquals("🇩🇪", AppLanguage.GERMAN.flagEmoji)
        assertEquals("🇯🇵", AppLanguage.JAPANESE.flagEmoji)
        assertEquals("🇪🇸", AppLanguage.SPANISH_SPAIN.flagEmoji)
        assertEquals("🌎", AppLanguage.SPANISH_LATIN_AMERICA.flagEmoji)
    }
}
