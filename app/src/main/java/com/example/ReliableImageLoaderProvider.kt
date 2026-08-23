package com.example

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import androidx.core.content.ContextCompat
import coil.Coil
import coil.ImageLoader

/**
 * Installs Coil before the first Activity is created.
 *
 * A number of Harmony game cards intentionally use remote artwork. If a remote image is
 * unavailable, rate-limited, or the device is offline, Coil used to render an empty surface.
 * The singleton loader now always has a bundled local placeholder/error/fallback drawable.
 */
class ReliableImageLoaderProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        val appContext = context?.applicationContext ?: return false
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
