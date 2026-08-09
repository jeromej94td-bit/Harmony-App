package com.example.ui

import com.example.R
import com.example.data.model.HarmonyPacksData
import com.example.data.model.TotChoiceSide
import com.example.data.model.totChoiceAt
import com.example.ui.components.TotImageProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
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
    fun `future images registered under stable keys override legacy text mappings`() {
        val stableKey = "tot:future-pack:3:b"
        val stableImage = "file:///stable/future-image.webp"
        TotImageProvider.setGeneratedImage(stableKey, stableImage)

        assertEquals(
            stableImage,
            TotImageProvider.getImageUrl(stableKey, "Beliebig veränderbarer Quelltext")
        )
    }
}
