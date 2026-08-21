package com.example.data

/**
 * Aktualisierte Standarddefinition für "Marken & Alltag".
 *
 * Gleiche Pack-ID wie in Models.kt: HarmonyPacksData ersetzt den Default dadurch
 * an derselben Position, statt ein zweites Spiel anzulegen.
 */
object GeneratedHarmonyMarkenAlltag {
    const val VERSION: Long = 1787351460000L

    val PACKS: List<GenPack> = listOf(
        GenPack(
            id = "markenalltag",
            title = "Marken & Alltag",
            cat = "tot",
            topic = "aufwaermen",
            type = "tot",
            tags = listOf("dasoderdas"),
            pairs = listOf(
                "McDonald’s" to "Burger King",
                "iPhone" to "Android",
                "Netflix" to "Kino",
                "Nike" to "Adidas",
                "Spotify" to "YouTube Music",
                "PlayStation" to "Xbox",
                "Coca-Cola" to "Pepsi",
                "IKEA" to "Möbelhaus",
                "Amazon" to "Lokal einkaufen",
                "Disney" to "Studio Ghibli",
                "Instagram" to "YouTube"
            ),
            questions = emptyList()
        )
    )
}
