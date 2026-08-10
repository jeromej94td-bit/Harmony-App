package com.example.ui

import com.example.data.GeneratedHarmonyContent
import com.example.data.model.HarmonyPacksData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Full release guard for the French locale. */
class FrenchLocalizationCoverageTest {

    private val displayStrings: List<String> by lazy {
        buildList {
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
    }

    @Test
    fun `French pack covers every built-in and generated display string`() {
        val missing = displayStrings.filterNot(EXACT_FRENCH_CONTENT::containsKey)
        assertTrue("Missing French copy: ${missing.joinToString(" | ")}", missing.isEmpty())
        assertEquals(898, EXACT_FRENCH_CONTENT.size)
        assertTrue(TranslationCatalog.hasCompletePack(AppLanguage.FRENCH))
    }

    @Test
    fun `French menu and reported context strings resolve completely`() {
        mapOf(
            "Wer würde eher?" to "Qui le ferait le plus probablement ?",
            "Zeichnen" to "Dessiner",
            "Das oder das?" to "Ceci ou cela ?",
            "🔥 Tägliche Aktivität" to "🔥 Activité du jour",
            "Der perfekte Heiratsantrag" to "La demande en mariage parfaite",
            "Themen" to "Thèmes",
            "Zusammenziehen" to "Emménager ensemble",
            "Herzhaft" to "Salé",
            "Eigenes Bild" to "Image personnalisée",
            "🔗 Ketten-Bauer" to "🔗 Créateur de chaînes"
        ).forEach { (source, french) ->
            assertEquals(french, localizedContent(source, AppLanguage.FRENCH))
        }
        assertEquals(
            "1. Qui le ferait le plus probablement ?",
            localizedContent("1. Wer würde eher?", AppLanguage.FRENCH)
        )
        val emptyState = "Hier ist gerade nichts.\nWechsle den Filter oder wähle ein anderes Thema."
        assertEquals(
            "Il n'y a rien ici pour le moment.\nChange le filtre ou choisis un autre thème.",
            localizedContent(emptyState, AppLanguage.FRENCH)
        )
    }

    @Test
    fun `French placeholders and Unicode remain technically safe`() {
        val placeholder = Regex("\\$\\{[^}]+}|\\$[A-Za-z_]\\w*|\\{[^{}]+}")
        EXACT_FRENCH_CONTENT.forEach { (source, target) ->
            assertEquals(
                "French placeholder mismatch for $source",
                placeholder.findAll(source).map { it.value }.sorted().toList(),
                placeholder.findAll(target).map { it.value }.sorted().toList()
            )
            assertFalse("French contains a damaged character for $source", '�' in target)
        }
    }
}
