package com.example.ui.components

import android.content.Context
import coil.request.ImageRequest

/**
 * Builds every This-or-That image request with a local placeholder/error/fallback.
 * Remote artwork can therefore never leave an empty card when a URL is unavailable,
 * rate-limited or temporarily offline.
 */
fun buildReliableTotImageRequest(context: Context, data: Any?): ImageRequest =
    ImageRequest.Builder(context)
        .data(data)
        .placeholder(TotImageProvider.fallbackDrawableResId)
        .error(TotImageProvider.fallbackDrawableResId)
        .fallback(TotImageProvider.fallbackDrawableResId)
        .crossfade(true)
        .build()
