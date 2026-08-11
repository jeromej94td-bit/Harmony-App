package com.example.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class PortuguesePortugalLocalizationCoverageTest {
    @Test
    fun `European Portuguese catalog is complete and translates representative UI`() {
        assertEquals(898, EXACT_PORTUGUESE_PORTUGAL_CONTENT.size)
        assertFalse(EXACT_PORTUGUESE_PORTUGAL_CONTENT.getValue("Fragen & Spiele").contains("Fragen"))
        assertEquals("Português (Portugal)", AppLanguage.PORTUGUESE_PORTUGAL.nativeName)
        assertEquals("pt-PT", AppLanguage.PORTUGUESE_PORTUGAL.code)
    }
}
