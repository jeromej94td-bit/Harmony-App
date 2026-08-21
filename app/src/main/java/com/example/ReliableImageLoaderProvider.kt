package com.example

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import androidx.core.content.ContextCompat
import coil.Coil
import coil.ImageLoader
import com.example.ui.components.TotImageProvider

/**
 * Installs Coil before the first Activity is created.
 *
 * A number of Harmony game cards intentionally use remote artwork. If a remote image is
 * unavailable, rate-limited, or the device is offline, Coil used to render an empty surface.
 * The singleton loader now always has a bundled local placeholder/error/fallback drawable.
 *
 * Marken & Alltag is intentionally registered here as local generated overrides. This keeps
 * the large TotImageProvider map stable while guaranteeing that all 20 options use the
 * matching bundled drawable before any remote or heuristic fallback can win.
 */
class ReliableImageLoaderProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        val appContext = context?.applicationContext ?: return false

        registerMarkenAlltagImages()

        val fallback = ContextCompat.getDrawable(appContext, R.drawable.tot_image_fallback)
        Coil.setImageLoader(
            ImageLoader.Builder(appContext)
                .placeholder(fallback)
                .error(fallback)
                .fallback(fallback)
                .crossfade(true)
                .build()
        )
        return true
    }

    private fun registerMarkenAlltagImages() {
        val images = linkedMapOf(
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
        images.forEach { (option, drawable) ->
            TotImageProvider.setGeneratedImage(option, drawable)
        }
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0
}
