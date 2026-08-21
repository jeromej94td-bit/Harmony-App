package com.example.ui.components

import com.example.R

/**
 * Reliability helpers shared by rendering and the AI-Studio exporter.
 *
 * TotImageProvider may return URLs, files or Android drawable IDs. These helpers keep
 * the local drawable path discoverable without exposing the provider's internal map.
 */
val TotImageProvider.fallbackDrawableResId: Int
    get() = R.drawable.tot_image_fallback

private val markenAlltagBundledImages: Map<String, Int> = mapOf(
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
    "Studio Ghibli" to R.drawable.marken_studio_ghibli
)

private fun bundledMarkenAlltagImage(text: String): Int? {
    val trimmed = text.trim()
    return markenAlltagBundledImages[trimmed]
        ?: markenAlltagBundledImages.entries.firstOrNull {
            it.key.equals(trimmed, ignoreCase = true)
        }?.value
}

fun TotImageProvider.getBundledImageResId(text: String): Int? =
    bundledMarkenAlltagImage(text) ?: (getImageUrl(text) as? Int)

fun TotImageProvider.getBundledImageResId(assetKey: String, legacyAssetKey: String): Int? =
    bundledMarkenAlltagImage(assetKey)
        ?: bundledMarkenAlltagImage(legacyAssetKey)
        ?: (getImageUrl(assetKey = assetKey, legacyAssetKey = legacyAssetKey) as? Int)
