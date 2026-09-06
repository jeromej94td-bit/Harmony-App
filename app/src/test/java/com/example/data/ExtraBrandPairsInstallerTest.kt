package com.example.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.HarmonyPacksData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExtraBrandPairsInstallerTest {

    private val expectedPairs = listOf(
        "Starbucks" to "Dunkin’",
        "Red Bull" to "Monster Energy",
        "Nutella" to "Lotus Biscoff",
        "Haribo" to "Trolli",
        "Pringles" to "Doritos",
        "KFC" to "Subway",
        "Domino’s" to "Pizza Hut",
        "Nespresso" to "Senseo",
        "Nintendo Switch" to "Steam Deck",
        "TikTok" to "Instagram",
        "Booking.com" to "Airbnb",
        "Aldi" to "Lidl",
        "REWE" to "EDEKA",
        "dm" to "Rossmann",
        "Zalando" to "ABOUT YOU",
        "H&M" to "Zara",
        "BMW" to "Mercedes-Benz",
        "LEGO" to "Playmobil",
        "Converse" to "Vans",
        "Dyson" to "Miele"
    )

    private fun pairKey(pair: Pair<String, String>): String =
        listOf(pair.first.trim().lowercase(), pair.second.trim().lowercase())
            .sorted()
            .joinToString("||")

    @Test
    fun installAddsAllExtraBrandPairsWithoutDuplicates() {
        HarmonyPacksData.setDynamicPacks(emptyList())
        val context = ApplicationProvider.getApplicationContext<Context>()

        DriveTotAssetInstaller.install(context)

        val pack = HarmonyPacksData.PACKS.first { it.id == "markenalltag" }
        val keys = pack.pairs.map(::pairKey)
        val expectedKeys = expectedPairs.map(::pairKey)

        expectedKeys.forEach { expected ->
            assertTrue("Missing Marken & Alltag pair: $expected", expected in keys)
        }
        assertEquals(30, pack.pairs.size)
        assertEquals(keys.size, keys.toSet().size)
    }
}
