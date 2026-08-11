package com.example.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class PortugueseLocalizationCoverageTest {
    @Test
    fun `Portuguese catalog is complete and translates representative UI`() {
        assertEquals(900, EXACT_PORTUGUESE_CONTENT.size)
        assertFalse(EXACT_PORTUGUESE_CONTENT.getValue("Fragen & Spiele").contains("Fragen"))
        assertEquals("Italiano", localizedContent("Italienisch", AppLanguage.PORTUGUESE))
        assertEquals("Português", localizedContent("Portugiesisch", AppLanguage.PORTUGUESE))
        assertEquals("Mensagem para \$partnerName...", localizedContent("Nachricht an \$partnerName...", AppLanguage.PORTUGUESE))
        assertEquals("Português", AppLanguage.PORTUGUESE.nativeName)
        assertEquals("pt", AppLanguage.PORTUGUESE.code)
    }
}
