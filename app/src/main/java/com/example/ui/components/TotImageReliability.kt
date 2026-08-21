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

fun TotImageProvider.getBundledImageResId(text: String): Int? =
    MarkenAlltagImages.get(text) ?: (getImageUrl(text) as? Int)

fun TotImageProvider.getBundledImageResId(assetKey: String, legacyAssetKey: String): Int? =
    MarkenAlltagImages.get(assetKey)
        ?: MarkenAlltagImages.get(legacyAssetKey)
        ?: (getImageUrl(assetKey = assetKey, legacyAssetKey = legacyAssetKey) as? Int)
