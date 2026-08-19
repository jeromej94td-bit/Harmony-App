package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.View
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
                val settings = PicShareWidgetPreferences.load(context)
                appWidgetIds.forEach { widgetId ->
                    val views = RemoteViews(context.packageName, R.layout.widget_picshare)
                    val serviceIntent = Intent(context, PicShareRemoteViewsService::class.java).apply {
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                        data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
                    }
                    views.setRemoteAdapter(R.id.picshare_widget_flipper, serviceIntent)
                    views.setEmptyView(R.id.picshare_widget_flipper, R.id.picshare_widget_empty_view)
                    views.setInt(R.id.picshare_widget_flipper, "setFlipInterval", ROTATION_INTERVAL_MS)
                    views.setBoolean(R.id.picshare_widget_flipper, "setAutoStart", pictures.size > 1)

                    if (pictures.isNotEmpty()) {
                        val fallbackCaption = pictures.firstNotNullOfOrNull { it.caption.takeIf(String::isNotBlank) }
                        views.setTextViewText(
                            R.id.picshare_widget_caption,
                            settings.caption.ifBlank { fallbackCaption ?: "Ein Bild nur für euch 💕" }
                        )
                        views.setTextViewText(
                            R.id.picshare_widget_status,
                            if (pictures.size > 1) "Harmony PicShare · Wechsel alle 6 Sek." else "Harmony PicShare · bereit"
                        )
                    } else {
                        views.setTextViewText(R.id.picshare_widget_caption, "Öffne Harmony und füge euer erstes Bild hinzu")
                        views.setTextViewText(R.id.picshare_widget_status, "Harmony PicShare")
                    }
                    views.setViewVisibility(R.id.picshare_widget_caption, if (settings.showCaption) View.VISIBLE else View.GONE)
                    views.setViewVisibility(R.id.picshare_widget_status, if (settings.showStatus) View.VISIBLE else View.GONE)
                    views.setViewVisibility(
                        R.id.picshare_widget_text_panel,
                        if (settings.showCaption || settings.showStatus) View.VISIBLE else View.GONE
                    )
                    val openIntent = Intent(context, MainActivity::class.java)
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        widgetId,
                        openIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.picshare_widget_root, pendingIntent)
                    manager.updateAppWidget(widgetId, views)
                    manager.notifyAppWidgetViewDataChanged(widgetId, R.id.picshare_widget_flipper)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val ROTATION_INTERVAL_MS = 6_000

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
