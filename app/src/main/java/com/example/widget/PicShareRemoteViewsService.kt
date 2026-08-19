package com.example.widget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.R
import com.example.data.db.HarmonyDatabase
import com.example.data.model.SharedPicEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlin.math.roundToInt

class PicShareRemoteViewsService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory = PicSharePictureFactory(applicationContext)
}

private class PicSharePictureFactory(
    private val context: Context
) : RemoteViewsService.RemoteViewsFactory {
    private var pictures: List<SharedPicEntity> = emptyList()

    override fun onCreate() = Unit

    override fun onDataSetChanged() {
        pictures = runBlocking(Dispatchers.IO) {
            HarmonyDatabase.getInstance(context).sharedPicDao().getWidgetPics()
        }.let { loaded ->
            if (PicShareWidgetPreferences.load(context).shufflePictures) {
                loaded.shuffled(kotlin.random.Random(loaded.size))
            } else {
                loaded
            }
        }
    }

    override fun onDestroy() {
        pictures = emptyList()
    }

    override fun getCount(): Int = pictures.size

    override fun getViewAt(position: Int): RemoteViews? {
        val picture = pictures.getOrNull(position) ?: return null
        val bitmap = decodeWidgetBitmap(picture.filePath) ?: return null
        return RemoteViews(context.packageName, R.layout.widget_picshare_slide).apply {
            setImageViewBitmap(R.id.picshare_widget_slide_image, bitmap)
        }
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = pictures.getOrNull(position)?.id ?: position.toLong()

    override fun hasStableIds(): Boolean = true

    private fun decodeWidgetBitmap(path: String): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        var sample = 1
        while (bounds.outWidth / sample > 1040 || bounds.outHeight / sample > 760) sample *= 2
        val decoded = BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample }) ?: return null
        val scale = minOf(1f, 520f / decoded.width, 360f / decoded.height)
        if (scale >= 1f) return decoded
        val scaled = Bitmap.createScaledBitmap(
            decoded,
            (decoded.width * scale).roundToInt().coerceAtLeast(1),
            (decoded.height * scale).roundToInt().coerceAtLeast(1),
            true
        )
        if (scaled !== decoded) decoded.recycle()
        return scaled
    }
}
