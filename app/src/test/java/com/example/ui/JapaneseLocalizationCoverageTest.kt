package com.example.ui

import com.example.data.GeneratedHarmonyContent
import com.example.data.model.HarmonyPacksData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Full release guard for the Japanese locale. */
class JapaneseLocalizationCoverageTest {

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
                pack.pairs.forEach { pair -> add(pair.first); add(pair.second) }
            }
            GeneratedHarmonyContent.CATEGORIES.forEach { add(it.name) }
            GeneratedHarmonyContent.PACKS.forEach { pack ->
                add(pack.title)
                pack.questions.forEach { question -> add(question.q); addAll(question.options) }
                pack.pairs.forEach { pair -> add(pair.first); add(pair.second) }
            }
        }.filter(String::isNotBlank).distinct()
    }

    @Test
    fun `Japanese pack covers every built-in and generated display string`() {
        val missing = displayStrings.filterNot(EXACT_JAPANESE_CONTENT::containsKey)
        assertTrue("Missing Japanese copy: ${missing.joinToString(" | ")}", missing.isEmpty())
        assertEquals(898, EXACT_JAPANESE_CONTENT.size)
        assertTrue(TranslationCatalog.hasCompletePack(AppLanguage.JAPANESE))
    }

    @Test
    fun `Japanese menu and proposal context resolve completely`() {
        mapOf(
            "Wer würde eher?" to "どちらがやりそう？",
            "Zeichnen" to "お絵描き",
            "Das oder das?" to "これかあれか？",
            "🔥 Tägliche Aktivität" to "🔥 今日のアクティビティ",
            "Der perfekte Heiratsantrag" to "理想のプロポーズ",
            "Zusammenziehen" to "一緒に住む",
            "Herzhaft" to "塩味",
            "Eigenes Bild" to "カスタム画像",
            "🔗 Ketten-Bauer" to "🔗 チェーン作成者"
        ).forEach { (source, japanese) -> assertEquals(japanese, localizedContent(source, AppLanguage.JAPANESE)) }
        assertEquals("1. どちらがやりそう？", localizedContent("1. Wer würde eher?", AppLanguage.JAPANESE))
        val emptyState = "Hier ist gerade nichts.\nWechsle den Filter oder wähle ein anderes Thema."
        assertEquals(
            "ここにはまだ何もありません。\nフィルターを変えるか、別のテーマを選んでください。",
            localizedContent(emptyState, AppLanguage.JAPANESE)
        )
    }

    @Test
    fun `Japanese placeholders and Unicode remain technically safe`() {
        val placeholder = Regex("\\$\\{[^}]+}|\\$[A-Za-z_]\\w*|\\{[^{}]+}")
        EXACT_JAPANESE_CONTENT.forEach { (source, target) ->
            assertEquals(
                "Japanese placeholder mismatch for $source",
                placeholder.findAll(source).map { it.value }.sorted().toList(),
                placeholder.findAll(target).map { it.value }.sorted().toList()
            )
            assertFalse("Japanese contains a damaged character for $source", '�' in target)
        }
    }
}
