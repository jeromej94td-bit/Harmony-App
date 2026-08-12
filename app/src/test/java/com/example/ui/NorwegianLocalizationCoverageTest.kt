package com.example.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Release guard for the complete Norwegian Bokmål locale pack. */
class NorwegianLocalizationCoverageTest {

    @Test
    fun `Norwegian catalog is complete and registered`() {
        assertEquals(900, EXACT_NORWEGIAN_CONTENT.size)
        assertTrue(TranslationCatalog.hasCompletePack(AppLanguage.NORWEGIAN))
        assertEquals("Norsk", AppLanguage.NORWEGIAN.nativeName)
        assertEquals("no", AppLanguage.NORWEGIAN.code)
        assertEquals("🇳🇴", AppLanguage.NORWEGIAN.flagEmoji)
    }

    @Test
    fun `Norwegian menu and common content resolve`() {
        mapOf(
            "Fragen & Spiele" to "Spørsmål og spill",
            "Wer würde eher?" to "Hvem ville mest sannsynlig?",
            "Das oder das?" to "Dette eller det?",
            "Zurück" to "Tilbake",
            "Weiter" to "Neste",
            "Sprache" to "Språk",
            "Italienisch" to "Italiensk",
            "Portugiesisch" to "Portugisisk",
            "Der perfekte Heiratsantrag" to "Det perfekte frieriet",
            "Großer Garten" to "Stor hage"
        ).forEach { (source, norwegian) ->
            assertEquals(norwegian, localizedContent(source, AppLanguage.NORWEGIAN))
        }

        assertEquals(
            "Melding til \$partnerName ...",
            localizedContent("Nachricht an \$partnerName...", AppLanguage.NORWEGIAN)
        )
        assertEquals(
            "1. Hvem ville mest sannsynlig?",
            localizedContent("1. Wer würde eher?", AppLanguage.NORWEGIAN)
        )
    }

    @Test
    fun `Norwegian placeholders and Unicode remain technically safe`() {
        val placeholder = Regex("\\$\\{[^}]+}|\\$[A-Za-z_]\\w*|\\{[^{}]+}")
        EXACT_NORWEGIAN_CONTENT.forEach { (source, target) ->
            assertEquals(
                "Norwegian placeholder mismatch for $source",
                placeholder.findAll(source).map { it.value }.sorted().toList(),
                placeholder.findAll(target).map { it.value }.sorted().toList()
            )
            assertFalse("Norwegian contains a damaged character for $source", '�' in target)
        }
    }
}
