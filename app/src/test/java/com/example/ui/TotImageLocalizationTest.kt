package com.example.ui

import com.example.R
import com.example.data.model.HarmonyPacksData
import com.example.data.model.TotChoiceSide
import com.example.data.model.isAvailableIn
import com.example.data.model.totChoiceAt
import com.example.ui.components.TotImageProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TotImageLocalizationTest {

    @Test
    fun `translated card label keeps stable asset identity and canonical answer`() {
        val pack = HarmonyPacksData.DEFAULT_PACKS.first { it.id == "traumhaus" }
        val choice = pack.totChoiceAt(0, TotChoiceSide.FIRST)

        assertEquals("tot:traumhaus:0:a", choice.assetKey)
        assertEquals("Altbau mit Charme", choice.answerValue)
        assertEquals("Altbau mit Charme", choice.legacyAssetKey)
        assertEquals(
            "Charming Period Home",
            localizedContent(choice.answerValue, AppLanguage.ENGLISH)
        )
        assertNotEquals(
            localizedContent(choice.answerValue, AppLanguage.ENGLISH),
            choice.assetKey
        )
    }

    @Test
    fun `english label cannot replace the image lookup key`() {
        val pack = HarmonyPacksData.DEFAULT_PACKS.first { it.id == "traumhaus" }
        val first = pack.totChoiceAt(0, TotChoiceSide.FIRST)
        val second = pack.totChoiceAt(0, TotChoiceSide.SECOND)

        assertEquals(
            R.drawable.traumhaus_altbau,
            TotImageProvider.getImageUrl(first.assetKey, first.legacyAssetKey)
        )
        assertEquals(
            R.drawable.traumhaus_smart_home,
            TotImageProvider.getImageUrl(second.assetKey, second.legacyAssetKey)
        )
    }

    @Test
    fun `italian labels keep stable image keys and legacy labels still resolve`() {
        val pack = HarmonyPacksData.DEFAULT_PACKS.first { it.id == "traumhaus" }
        val garden = pack.totChoiceAt(3, TotChoiceSide.FIRST)
        val terrace = pack.totChoiceAt(3, TotChoiceSide.SECOND)

        assertEquals("Grande giardino", localizedContent(garden.answerValue, AppLanguage.ITALIAN))
        assertEquals(
            R.drawable.traumhaus_garten,
            TotImageProvider.getImageUrl(garden.assetKey, garden.legacyAssetKey)
        )
        assertEquals(
            R.drawable.traumhaus_dachterrasse,
            TotImageProvider.getImageUrl(terrace.assetKey, terrace.legacyAssetKey)
        )

        // Protect older renderers and imported content that still pass the visible label.
        assertEquals(
            R.drawable.traumhaus_garten,
            TotImageProvider.getImageUrl("Grande giardino")
        )
        assertEquals(
            R.drawable.traumhaus_dachterrasse,
            TotImageProvider.getImageUrl("Terrazza soleggiata sul tetto")
        )
    }

    @Test
    fun `both Spanish variants keep stable image keys and legacy labels resolve`() {
        val pack = HarmonyPacksData.DEFAULT_PACKS.first { it.id == "traumhaus" }
        val garden = pack.totChoiceAt(3, TotChoiceSide.FIRST)
        val terrace = pack.totChoiceAt(3, TotChoiceSide.SECOND)

        listOf(AppLanguage.SPANISH_LATIN_AMERICA, AppLanguage.SPANISH_SPAIN).forEach { language ->
            assertEquals("Jardín grande", localizedContent(garden.answerValue, language))
            assertEquals(
                "Terraza soleada en la azotea",
                localizedContent(terrace.answerValue, language)
            )
        }
        assertEquals(
            R.drawable.traumhaus_garten,
            TotImageProvider.getImageUrl(garden.assetKey, garden.legacyAssetKey)
        )
        assertEquals(
            R.drawable.traumhaus_dachterrasse,
            TotImageProvider.getImageUrl(terrace.assetKey, terrace.legacyAssetKey)
        )
        assertEquals(R.drawable.traumhaus_garten, TotImageProvider.getImageUrl("Jardín grande"))
        assertEquals(
            R.drawable.traumhaus_dachterrasse,
            TotImageProvider.getImageUrl("Terraza soleada en la azotea")
        )
    }

    @Test
    fun `French labels keep stable image keys and legacy labels still resolve`() {
        val pack = HarmonyPacksData.DEFAULT_PACKS.first { it.id == "traumhaus" }
        val garden = pack.totChoiceAt(3, TotChoiceSide.FIRST)
        val terrace = pack.totChoiceAt(3, TotChoiceSide.SECOND)

        assertEquals("Grand jardin", localizedContent(garden.answerValue, AppLanguage.FRENCH))
        assertEquals(
            "Terrasse ensoleillée sur le toit",
            localizedContent(terrace.answerValue, AppLanguage.FRENCH)
        )
        assertEquals(R.drawable.traumhaus_garten, TotImageProvider.getImageUrl(garden.assetKey, garden.legacyAssetKey))
        assertEquals(R.drawable.traumhaus_dachterrasse, TotImageProvider.getImageUrl(terrace.assetKey, terrace.legacyAssetKey))
        assertEquals(R.drawable.traumhaus_garten, TotImageProvider.getImageUrl("Grand jardin"))
        assertEquals(
            R.drawable.traumhaus_dachterrasse,
            TotImageProvider.getImageUrl("Terrasse ensoleillée sur le toit")
        )
    }

    @Test
    fun `Japanese labels keep stable image keys and legacy labels still resolve`() {
        val pack = HarmonyPacksData.DEFAULT_PACKS.first { it.id == "traumhaus" }
        val garden = pack.totChoiceAt(3, TotChoiceSide.FIRST)
        val terrace = pack.totChoiceAt(3, TotChoiceSide.SECOND)

        assertEquals("広い庭", localizedContent(garden.answerValue, AppLanguage.JAPANESE))
        assertEquals("日当たりのよい屋上テラス", localizedContent(terrace.answerValue, AppLanguage.JAPANESE))
        assertEquals(R.drawable.traumhaus_garten, TotImageProvider.getImageUrl(garden.assetKey, garden.legacyAssetKey))
        assertEquals(R.drawable.traumhaus_dachterrasse, TotImageProvider.getImageUrl(terrace.assetKey, terrace.legacyAssetKey))
        assertEquals(R.drawable.traumhaus_garten, TotImageProvider.getImageUrl("広い庭"))
        assertEquals(R.drawable.traumhaus_dachterrasse, TotImageProvider.getImageUrl("日当たりのよい屋上テラス"))
    }

    @Test
    fun `future images registered under stable keys override legacy text mappings`() {
        val stableKey = "tot:future-pack:3:b"
        val stableImage = "file:///stable/future-image.webp"
        TotImageProvider.setGeneratedImage(stableKey, stableImage)

        assertEquals(
            stableImage,
            TotImageProvider.getImageUrl(stableKey, "Beliebig veränderbarer Quelltext")
        )
    }

    @Test
    fun `Italian cuisine deck is Italian-only and every card resolves local splash art`() {
        val pack = HarmonyPacksData.DEFAULT_PACKS.first { it.id == "tot_italian_cuisine_mixed" }

        assertEquals(30, pack.pairs.size)
        assertEquals("Pizza napoletana", pack.pairs.first().first)
        assertEquals("Calzone", pack.pairs.first().second)
        assertTrue(pack.isAvailableIn(AppLanguage.ITALIAN.code))
        assertFalse(pack.isAvailableIn(AppLanguage.ENGLISH.code))

        pack.pairs.indices.forEach { pairIndex ->
            listOf(TotChoiceSide.FIRST, TotChoiceSide.SECOND).forEach { side ->
                val choice = pack.totChoiceAt(pairIndex, side)
                assertTrue(
                    "Missing local art for ${choice.assetKey}",
                    TotImageProvider.getImageUrl(choice.assetKey, choice.legacyAssetKey) is Int
                )
            }
        }
        assertEquals(
            R.drawable.it_01_pizza_napoletana,
            TotImageProvider.getImageUrl("tot:tot_italian_cuisine_mixed:0:a", "Pizza napoletana")
        )
        assertEquals(
            R.drawable.it_15_semifreddo,
            TotImageProvider.getImageUrl("tot:tot_italian_cuisine_mixed:29:b", "Semifreddo")
        )
    }
}
