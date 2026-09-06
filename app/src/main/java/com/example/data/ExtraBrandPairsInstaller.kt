package com.example.data

import com.example.data.model.HarmonyPacksData

internal object ExtraBrandPairsInstaller {
    private val extraBrandPairs = listOf(
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

    fun apply() {
        val current = HarmonyPacksData.PACKS
        val pack = current.firstOrNull { it.id == "markenalltag" } ?: return
        val seen = pack.pairs.map(::pairKey).toMutableSet()
        val additions = extraBrandPairs.filter { seen.add(pairKey(it)) }
        if (additions.isEmpty()) return

        val updated = pack.copy(pairs = pack.pairs + additions)
        HarmonyPacksData.setDynamicPacks(
            current.map { existing ->
                if (existing.id == "markenalltag") updated else existing
            }
        )
    }
}
