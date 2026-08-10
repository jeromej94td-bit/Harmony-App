package com.example.ui

import com.example.data.GeneratedHarmonyContent
import com.example.data.model.HarmonyPacksData
import com.example.data.model.isAvailableIn
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Prevents a release from silently shipping German titles, questions, answers or
 * visual-choice labels while English is selected.
 */
class EnglishLocalizationCoverageTest {

    @Test
    fun `every built-in and generated display string has reviewed English copy`() {
        val displayStrings = buildList {
            addAll(HarmonyPacksData.CATEGORIES.map { it.name })
            addAll(HarmonyPacksData.TOPICS.map { it.name })

            HarmonyPacksData.DEFAULT_PACKS.filter { it.isAvailableIn(AppLanguage.ENGLISH.code) }.forEach { pack ->
                add(pack.title)
                pack.questions.forEach { question ->
                    add(question.q)
                    addAll(question.options)
                    question.defaultMine?.let(::add)
                }
                pack.pairs.forEach { pair ->
                    add(pair.first)
                    add(pair.second)
                }
            }

            GeneratedHarmonyContent.CATEGORIES.forEach { add(it.name) }
            GeneratedHarmonyContent.PACKS.forEach { pack ->
                add(pack.title)
                pack.questions.forEach { question ->
                    add(question.q)
                    addAll(question.options)
                }
                pack.pairs.forEach { pair ->
                    add(pair.first)
                    add(pair.second)
                }
            }
        }.filter(String::isNotBlank).distinct()

        val missing = displayStrings.filterNot(EXACT_ENGLISH_CONTENT::containsKey)
        assertTrue(
            "Missing reviewed English copy for: ${missing.joinToString(" | ")}",
            missing.isEmpty()
        )
    }

    @Test
    fun `reported mixed-language regressions resolve to English`() {
        val expected = mapOf(
            "Wer vergisst eher einen Jahrestag?" to "Who's more likely to forget an anniversary?",
            "Ich" to "Me",
            "Mein Partner" to "My partner",
            "Keiner von uns" to "Neither of us",
            "Dass alles meinen Stil hat" to "That everything reflects my style",
            "Schlafzimmer" to "Bedroom",
            "Essensvorlieben" to "Food Preferences",
            "Süß" to "Sweet",
            "Herzhaft" to "Savory",
            "Gourmet Eis-Sorten" to "Gourmet Ice Cream Flavors",
            "Himbeere" to "Raspberry"
        )

        expected.forEach { (source, english) ->
            assertEquals(english, localizedContent(source, AppLanguage.ENGLISH))
        }
        assertEquals(
            "🍦 Ice Cream",
            localizedContent("🍦 Eis", AppLanguage.ENGLISH)
        )
        assertEquals(
            "1. Who's more likely to forget an anniversary?",
            localizedContent("1. Wer vergisst eher einen Jahrestag?", AppLanguage.ENGLISH)
        )
    }
}
