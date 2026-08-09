package com.example.ui

import com.example.data.GeneratedHarmonyContent
import com.example.data.model.HarmonyPacksData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Gives Italian the same full-content release guard as the established English
 * locale. A partial translation must fail before it reaches AI Studio or Play.
 */
class ItalianLocalizationCoverageTest {

    @Test
    fun `every built-in and generated display string has reviewed Italian copy`() {
        val displayStrings = buildList {
            addAll(HarmonyPacksData.CATEGORIES.map { it.name })
            addAll(HarmonyPacksData.TOPICS.map { it.name })

            HarmonyPacksData.DEFAULT_PACKS.forEach { pack ->
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

        val missing = displayStrings.filterNot(EXACT_ITALIAN_CONTENT::containsKey)
        assertTrue(
            "Missing reviewed Italian copy for: ${missing.joinToString(" | ")}",
            missing.isEmpty()
        )
    }

    @Test
    fun `games menu and reported mixed-language regressions resolve in both locales`() {
        val expected = mapOf(
            "Wer würde eher?" to ("Who's more likely?" to "Chi preferirebbe?"),
            "Zeichnen" to ("Drawing" to "Disegno"),
            "Das oder das?" to ("This or That?" to "Questo o quello?"),
            "🔥 Tägliche Aktivität" to ("🔥 Daily Activity" to "🔥 Attività quotidiana"),
            "Der perfekte Heiratsantrag" to ("The Perfect Proposal" to "La proposta di matrimonio perfetta"),
            "Themen" to ("Topics" to "Argomenti"),
            "Aufwärmen" to ("Warm-up" to "Riscaldarsi"),
            "Beziehung" to ("Relationship" to "Relazione"),
            "Sex & Liebe" to ("Sex & Love" to "Sesso e amore"),
            "Reiseziele" to ("Travel Destinations" to "Destinazioni")
        )

        expected.forEach { (source, translations) ->
            assertEquals(translations.first, localizedContent(source, AppLanguage.ENGLISH))
            assertEquals(translations.second, localizedContent(source, AppLanguage.ITALIAN))
        }
    }
}
