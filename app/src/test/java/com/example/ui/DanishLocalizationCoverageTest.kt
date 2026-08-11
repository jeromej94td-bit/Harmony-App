package com.example.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Release guard for the Danish locale pack. */
class DanishLocalizationCoverageTest {

    @Test
    fun `Danish catalog is complete and registered`() {
        assertEquals(898, EXACT_DANISH_CONTENT.size)
        assertTrue(TranslationCatalog.hasCompletePack(AppLanguage.DANISH))
        assertEquals("Dansk", AppLanguage.DANISH.nativeName)
        assertEquals("da", AppLanguage.DANISH.code)
        assertEquals("🇩🇰", AppLanguage.DANISH.flagEmoji)
    }

    @Test
    fun `Danish menu and common content resolve`() {
        mapOf(
            "Fragen & Spiele" to "Spørgsmål og spil",
            "Wer würde eher?" to "Hvem ville mest sandsynligt?",
            "Zeichnen" to "Tegning",
            "Das oder das?" to "Det eller det?",
            "🔥 Tägliche Aktivität" to "🔥 Daglig aktivitet",
            "Der perfekte Heiratsantrag" to "Det perfekte frieri",
            "Themen" to "Emner",
            "Aufwärmen" to "Opvarmning",
            "Beziehung" to "Forhold",
            "Sex & Liebe" to "Sex og kærlighed",
            "Reiseziele" to "Rejsemål",
            "Großer Garten" to "Stor have"
        ).forEach { (source, danish) ->
            assertEquals(danish, localizedContent(source, AppLanguage.DANISH))
        }

        assertEquals(
            "1. Hvem ville mest sandsynligt?",
            localizedContent("1. Wer würde eher?", AppLanguage.DANISH)
        )
        assertEquals(
            "Der er ikke noget her lige nu.\nSkift filteret, eller vælg et andet emne.",
            localizedContent(
                "Hier ist gerade nichts.\nWechsle den Filter oder wähle ein anderes Thema.",
                AppLanguage.DANISH
            )
        )
    }

    @Test
    fun `Danish placeholders and Unicode remain technically safe`() {
        val placeholder = Regex("\\$\\{[^}]+}|\\$[A-Za-z_]\\w*|\\{[^{}]+}")
        EXACT_DANISH_CONTENT.forEach { (source, target) ->
            assertEquals(
                "Danish placeholder mismatch for $source",
                placeholder.findAll(source).map { it.value }.sorted().toList(),
                placeholder.findAll(target).map { it.value }.sorted().toList()
            )
            assertFalse("Danish contains a damaged character for $source", '�' in target)
        }
    }
}
