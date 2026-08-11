package com.example.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class PortugueseBrazilLocalizationCoverageTest {
    @Test
    fun `Brazilian Portuguese catalog is complete and translates representative UI`() {
        assertEquals(898, EXACT_PORTUGUESE_BRAZIL_CONTENT.size)
        assertFalse(EXACT_PORTUGUESE_BRAZIL_CONTENT.getValue("Fragen & Spiele").contains("Fragen"))
        assertEquals("Português (Brasil)", AppLanguage.PORTUGUESE_BRAZIL.nativeName)
        assertEquals("pt-BR", AppLanguage.PORTUGUESE_BRAZIL.code)
    }
}
