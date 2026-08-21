package com.example.ui.components

import com.example.R

/**
 * Lokale Standardbilder für das "Marken & Alltag"-Das-oder-das-Pack.
 *
 * Die Map ist zentral, damit Renderer und AI-Studio-Exporter exakt dieselben
 * Android-Drawables verwenden. Dev-Studio-Nutzerbilder können diese Defaults
 * weiterhin überschreiben, weil sie später mit setCustomImage() gesetzt werden.
 */
object MarkenAlltagImages {
    val bundled: Map<String, Int> = linkedMapOf(
        "McDonald’s" to R.drawable.marken_mcdonalds,
        "Burger King" to R.drawable.marken_burger_king,
        "iPhone" to R.drawable.marken_iphone,
        "Android" to R.drawable.marken_android,
        "Netflix" to R.drawable.marken_netflix,
        "Kino" to R.drawable.marken_kino,
        "Nike" to R.drawable.marken_nike,
        "Adidas" to R.drawable.marken_adidas,
        "Spotify" to R.drawable.marken_spotify,
        "YouTube Music" to R.drawable.marken_youtube_music,
        "PlayStation" to R.drawable.marken_playstation,
        "Xbox" to R.drawable.marken_xbox,
        "Coca-Cola" to R.drawable.marken_coca_cola,
        "Pepsi" to R.drawable.marken_pepsi,
        "IKEA" to R.drawable.marken_ikea,
        "Möbelhaus" to R.drawable.marken_moebelhaus,
        "Amazon" to R.drawable.marken_amazon,
        "Lokal einkaufen" to R.drawable.marken_lokal_einkaufen,
        "Disney" to R.drawable.marken_disney,
        "Studio Ghibli" to R.drawable.marken_studio_ghibli,
        "Instagram" to R.drawable.marken_instagram,
        "YouTube" to R.drawable.marken_youtube
    )

    fun get(text: String): Int? {
        val trimmed = text.trim()
        return bundled[trimmed]
            ?: bundled.entries.firstOrNull { it.key.equals(trimmed, ignoreCase = true) }?.value
    }

    /**
     * Installiert die gebündelten Motive als lokale Defaults.
     * Persistierte Dev-Studio-Overrides werden anschließend geladen und gewinnen.
     */
    fun installAsDefaults() {
        bundled.forEach { (text, drawable) ->
            TotImageProvider.setCustomImage(text, drawable)
        }
    }
}
