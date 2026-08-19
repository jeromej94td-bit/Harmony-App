package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.db.HarmonyDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PicShareWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val pictures = HarmonyDatabase.getInstance(context).sharedPicDao().getWidgetPics()
                val rotationSlot = (System.currentTimeMillis() / ROTATION_INTERVAL_MS).toInt()
                val latest = if (pictures.isEmpty()) null else pictures[Math.floorMod(rotationSlot, pictures.size)]
                appWidgetIds.forEach { widgetId ->
                    val views = RemoteViews(context.packageName, R.layout.widget_picshare)
                    if (latest != null) {
                        decodeWidgetBitmap(latest.filePath)?.let { views.setImageViewBitmap(R.id.picshare_widget_image, it) }
                        views.setTextViewText(R.id.picshare_widget_caption, latest.caption.ifBlank { "Ein Bild nur für euch 💕" })
                        views.setTextViewText(
                            R.id.picshare_widget_status,
                            if (pictures.size > 1) "Harmony PicShare · ${pictures.size} Bilder rotieren" else "Harmony PicShare · bereit"
                        )
                    } else {
                        views.setTextViewText(R.id.picshare_widget_caption, "Öffne Harmony und füge euer erstes Bild hinzu")
                        views.setTextViewText(R.id.picshare_widget_status, "Harmony PicShare")
                    }
                    val openIntent = Intent(context, MainActivity::class.java)
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        widgetId,
                        openIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.picshare_widget_root, pendingIntent)
                    manager.updateAppWidget(widgetId, views)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val ROTATION_INTERVAL_MS = 30L * 60L * 1_000L

        private fun decodeWidgetBitmap(path: String): android.graphics.Bitmap? {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(path, bounds)
            var sample = 1
            while (bounds.outWidth / sample > 900 || bounds.outHeight / sample > 700) sample *= 2
            return BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
        }

        fun requestPin(context: Context): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
            val manager = AppWidgetManager.getInstance(context)
            if (!manager.isRequestPinAppWidgetSupported) return false
            return manager.requestPinAppWidget(ComponentName(context, PicShareWidgetProvider::class.java), null, null)
        }

        fun refreshAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, PicShareWidgetProvider::class.java)
            val ids = manager.getAppWidgetIds(component)
            if (ids.isEmpty()) return
            val intent = Intent(context, PicShareWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }
            context.sendBroadcast(intent)
        }
    }
}
