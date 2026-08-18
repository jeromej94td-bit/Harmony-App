package com.example.ui.introspection

import com.example.ui.AppLanguage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class IntrospectionStringsTest {

    @Test
    fun `all string keys have non-empty translations in German, English, and Italian`() {
        val keys = IntrospectionStringKey.values()
        val languages = listOf(AppLanguage.GERMAN, AppLanguage.ENGLISH, AppLanguage.ITALIAN)

        for (lang in languages) {
            for (key in keys) {
                val translation = IntrospectionStrings.tr(key, lang)
                assertNotNull("Missing translation for key $key in $lang", translation)
                assertFalse("Empty translation for key $key in $lang", translation.isBlank())
                assertFalse("Translation for $key in $lang contains forbidden music title", translation.contains("Merlin", ignoreCase = true))
            }
        }
    }

    @Test
    fun `psychological meanings are present for all three questions in all languages`() {
        for (lang in listOf(AppLanguage.GERMAN, AppLanguage.ENGLISH, AppLanguage.ITALIAN)) {
            val color = IntrospectionStrings.tr(IntrospectionStringKey.RESULTS_COLOR_MEANING, lang)
            val animal = IntrospectionStrings.tr(IntrospectionStringKey.RESULTS_ANIMAL_MEANING, lang)
            val water = IntrospectionStrings.tr(IntrospectionStringKey.RESULTS_WATER_MEANING, lang)

            assertFalse(color.isBlank())
            assertFalse(animal.isBlank())
            assertFalse(water.isBlank())
        }
    }

    @Test
    fun `eyebrow capsule string has sparkles emoji across all languages`() {
        val de = IntrospectionStrings.tr(IntrospectionStringKey.ENTRY_EYEBROW, AppLanguage.GERMAN)
        val en = IntrospectionStrings.tr(IntrospectionStringKey.ENTRY_EYEBROW, AppLanguage.ENGLISH)
        val it = IntrospectionStrings.tr(IntrospectionStringKey.ENTRY_EYEBROW, AppLanguage.ITALIAN)

        assertTrue(de.contains("✨️"))
        assertTrue(en.contains("✨️"))
        assertTrue(it.contains("✨️"))
    }
}
