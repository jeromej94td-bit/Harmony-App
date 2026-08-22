package com.example.ui

import com.example.data.model.HarmonyPacksData
import com.example.util.LanguageManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LanguageAndTranslationsTest {

    @Test
    fun testLanguageRegistration() {
        val ja = AppLanguage.fromCode("ja")
        assertEquals(AppLanguage.JAPANESE, ja)
        assertEquals("ja", ja.code)
        assertEquals("日本語", ja.nativeName)
        assertEquals("🇯🇵", ja.flagEmoji)

        val pl = AppLanguage.fromCode("pl")
        assertEquals(AppLanguage.POLISH, pl)
        assertEquals("pl", pl.code)
        assertEquals("Polski", pl.nativeName)
        assertEquals("🇵🇱", pl.flagEmoji)
    }

    @Test
    fun testJapaneseTranslationsPresence() {
        assertTrue("Japanese catalog should have entries", EXACT_JAPANESE_CONTENT.size > 1000)
        
        // Topic translations
        val essenTopic = TranslationCatalog.exact("Essen & Genuss", AppLanguage.JAPANESE)
        assertNotNull(essenTopic)
        assertNotEquals("Essen & Genuss", essenTopic)
        assertEquals("食と楽しみ", essenTopic)

        // Pack translations
        val markenPack = TranslationCatalog.exact("Marken & Alltag", AppLanguage.JAPANESE)
        assertNotNull(markenPack)
        assertEquals("ブランドと日常", markenPack)

        // Dynamic content
        val dynamicPair = TranslationCatalog.translate("3 Paare", AppLanguage.JAPANESE)
        assertEquals("3 組のペア", dynamicPair)
    }

    @Test
    fun testPolishTranslationsPresence() {
        assertTrue("Polish catalog should have entries", EXACT_POLISH_CONTENT.size > 1000)
        
        // Topic translations
        val essenTopic = TranslationCatalog.exact("Essen & Genuss", AppLanguage.POLISH)
        assertNotNull(essenTopic)
        assertNotEquals("Essen & Genuss", essenTopic)
        assertEquals("Jedzenie i przyjemność", essenTopic)

        // Pack translations
        val markenPack = TranslationCatalog.exact("Marken & Alltag", AppLanguage.POLISH)
        assertNotNull(markenPack)
        assertEquals("Marki i codzienność", markenPack)

        // Dynamic content
        val dynamicPair = TranslationCatalog.translate("3 Paare", AppLanguage.POLISH)
        assertEquals("3 par", dynamicPair)
    }

    @Test
    fun testLanguageManagerPackTranslation() {
        val defaultPacks = HarmonyPacksData.DEFAULT_PACKS
        assertTrue(defaultPacks.isNotEmpty())

        for (pack in defaultPacks) {
            val jaPack = LanguageManager.translatePack(pack, "ja")
            assertNotNull(jaPack.title)

            val plPack = LanguageManager.translatePack(pack, "pl")
            assertNotNull(plPack.title)
        }
    }
}
