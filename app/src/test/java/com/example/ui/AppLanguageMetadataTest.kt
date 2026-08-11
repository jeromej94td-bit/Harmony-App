package com.example.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppLanguageMetadataTest {

    @Test
    fun `every selectable locale has a unique code and a flag emoji`() {
        val languages = AppLanguage.entries.filter(TranslationCatalog::hasCompletePack)
        assertEquals(languages.size, languages.map { it.code.lowercase() }.distinct().size)
        assertTrue(languages.all { it.flagEmoji.isNotBlank() })
        assertEquals("🇩🇪", AppLanguage.GERMAN.flagEmoji)
        assertEquals("🇬🇧", AppLanguage.ENGLISH.flagEmoji)
        assertEquals("🇮🇹", AppLanguage.ITALIAN.flagEmoji)
        assertEquals("🇵🇹", AppLanguage.PORTUGUESE.flagEmoji)
    }
}
