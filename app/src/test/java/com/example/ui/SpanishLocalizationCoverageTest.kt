package com.example.ui

import com.example.data.GeneratedHarmonyContent
import com.example.data.model.HarmonyPacksData
import com.example.data.model.isAvailableIn
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Full release guard for both explicitly requested Spanish market variants. */
class SpanishLocalizationCoverageTest {

    private val displayStrings: List<String> by lazy {
        buildList {
            addAll(HarmonyPacksData.CATEGORIES.map { it.name })
            addAll(HarmonyPacksData.TOPICS.map { it.name })
            HarmonyPacksData.DEFAULT_PACKS.filter { it.isAvailableIn(AppLanguage.SPANISH_LATIN_AMERICA.code) }.forEach { pack ->
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
    fun `both Spanish packs cover every built-in and generated display string`() {
        val missingLatinAmerica = displayStrings.filterNot(
            EXACT_SPANISH_LATIN_AMERICA_CONTENT::containsKey
        )
        val missingSpain = displayStrings.filterNot(EXACT_SPANISH_SPAIN_CONTENT::containsKey)

        assertTrue("Missing es-419 copy: ${missingLatinAmerica.joinToString(" | ")}", missingLatinAmerica.isEmpty())
        assertTrue("Missing es-ES copy: ${missingSpain.joinToString(" | ")}", missingSpain.isEmpty())
        assertEquals(898, EXACT_SPANISH_LATIN_AMERICA_CONTENT.size)
        assertEquals(898, EXACT_SPANISH_SPAIN_CONTENT.size)
    }

    @Test
    fun `Spanish locale codes are regional and unique without generic duplicate`() {
        val codes = AppLanguage.entries.map { it.code.lowercase() }
        assertEquals(codes.size, codes.distinct().size)
        assertTrue("es-419" in codes)
        assertTrue("es-es" in codes)
        assertFalse("es" in codes)
        assertTrue(TranslationCatalog.hasCompletePack(AppLanguage.SPANISH_LATIN_AMERICA))
        assertTrue(TranslationCatalog.hasCompletePack(AppLanguage.SPANISH_SPAIN))
    }

    @Test
    fun `reported menu strings resolve completely in both Spanish variants`() {
        val expected = mapOf(
            "Wer würde eher?" to "¿Quién lo haría?",
            "Zeichnen" to "Dibujar",
            "Das oder das?" to "¿Esto o aquello?",
            "🔥 Tägliche Aktivität" to "🔥 Actividad diaria",
            "Der perfekte Heiratsantrag" to "La propuesta de matrimonio perfecta",
            "Themen" to "Temas",
            "Aufwärmen" to "Para entrar en calor",
            "Beziehung" to "Relación",
            "Sex & Liebe" to "Sexo y amor",
            "Reiseziele" to "Destinos de viaje"
        )

        expected.forEach { (source, spanish) ->
            assertEquals(spanish, localizedContent(source, AppLanguage.SPANISH_LATIN_AMERICA))
            assertEquals(spanish, localizedContent(source, AppLanguage.SPANISH_SPAIN))
        }
        assertEquals(
            "1. ¿Quién lo haría?",
            localizedContent("1. Wer würde eher?", AppLanguage.SPANISH_LATIN_AMERICA)
        )
        val emptyState = "Hier ist gerade nichts.\nWechsle den Filter oder wähle ein anderes Thema."
        assertEquals(
            "No hay nada aquí ahora mismo.\nCambia el filtro o elige otro tema.",
            localizedContent(emptyState, AppLanguage.SPANISH_LATIN_AMERICA)
        )
        assertEquals(
            "No hay nada aquí ahora mismo.\nCambia el filtro o elige otro tema.",
            localizedContent(emptyState, AppLanguage.SPANISH_SPAIN)
        )
    }

    @Test
    fun `regional wording stays intentionally different`() {
        assertEquals(
            "Te extraño",
            localizedContent("Du fehlst mir", AppLanguage.SPANISH_LATIN_AMERICA)
        )
        assertEquals(
            "Te echo de menos",
            localizedContent("Du fehlst mir", AppLanguage.SPANISH_SPAIN)
        )
        assertEquals(
            "Con unas cuantas fotos del celular basta",
            localizedContent("Ein paar Handyfotos reichen", AppLanguage.SPANISH_LATIN_AMERICA)
        )
        assertEquals(
            "Con unas cuantas fotos del móvil basta",
            localizedContent("Ein paar Handyfotos reichen", AppLanguage.SPANISH_SPAIN)
        )
    }

    @Test
    fun `placeholders and Unicode remain technically safe in both packs`() {
        val placeholder = Regex("\\$\\{[^}]+}|\\$[A-Za-z_]\\w*|\\{[^{}]+}")
        listOf(
            "es-419" to EXACT_SPANISH_LATIN_AMERICA_CONTENT,
            "es-ES" to EXACT_SPANISH_SPAIN_CONTENT
        ).forEach { (code, pack) ->
            pack.forEach { (source, target) ->
                assertEquals(
                    "$code placeholder mismatch for $source",
                    placeholder.findAll(source).map { it.value }.sorted().toList(),
                    placeholder.findAll(target).map { it.value }.sorted().toList()
                )
                assertFalse("$code contains a damaged character for $source", '�' in target)
            }
        }
    }
}
