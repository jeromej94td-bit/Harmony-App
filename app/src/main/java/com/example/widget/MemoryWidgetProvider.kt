package com.example.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import com.example.R
import com.example.data.db.HarmonyDatabase
import com.example.data.model.MemoryEntryEntity
import com.example.data.model.MemoryEntryKind
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MemoryWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        ioScope.launch {
            try {
                appWidgetIds.forEach { renderOne(context.applicationContext, it) }
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        manager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        val pendingResult = goAsync()
        ioScope.launch {
            try {
                renderOne(context.applicationContext, appWidgetId)
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_COMPLETE_MEMORY_ENTRY) {
            val entryId = intent.getStringExtra(EXTRA_MEMORY_ENTRY_ID)
            if (entryId.isNullOrBlank()) return
            val pendingResult = goAsync()
            ioScope.launch {
                try {
                    completeEntry(context.applicationContext, entryId)
                } finally {
                    pendingResult.finish()
                }
            }
            return
        }
        super.onReceive(context, intent)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        appWidgetIds.forEach { MemoryWidgetPreferences.delete(context, it) }
        super.onDeleted(context, appWidgetIds)
    }

    companion object {
        private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        private val imageCache = MemoryWidgetImageCache()

        private val slotContainers = intArrayOf(
            R.id.memory_slot_1,
            R.id.memory_slot_2,
            R.id.memory_slot_3
        )
        private val slotImages = intArrayOf(
            R.id.memory_slot_1_image,
            R.id.memory_slot_2_image,
            R.id.memory_slot_3_image
        )
        private val slotSites = intArrayOf(
            R.id.memory_slot_1_site,
            R.id.memory_slot_2_site,
            R.id.memory_slot_3_site
        )
        private val slotTitles = intArrayOf(
            R.id.memory_slot_1_title,
            R.id.memory_slot_2_title,
            R.id.memory_slot_3_title
        )
        private val slotBodies = intArrayOf(
            R.id.memory_slot_1_body,
            R.id.memory_slot_2_body,
            R.id.memory_slot_3_body
        )
        private val slotChecks = intArrayOf(
            R.id.memory_slot_1_check,
            R.id.memory_slot_2_check,
            R.id.memory_slot_3_check
        )

        fun refreshAll(context: Context) {
            val appContext = context.applicationContext
            val manager = AppWidgetManager.getInstance(appContext)
            val component = ComponentName(appContext, MemoryWidgetProvider::class.java)
            val ids = manager.getAppWidgetIds(component)
            if (ids.isEmpty()) return
            val intent = Intent(appContext, MemoryWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }
            appContext.sendBroadcast(intent)
        }

        fun updateOne(context: Context, appWidgetId: Int) {
            val appContext = context.applicationContext
            ioScope.launch { renderOne(appContext, appWidgetId) }
        }

        internal suspend fun completeEntry(
            context: Context,
            entryId: String,
            nowMillis: Long = System.currentTimeMillis()
        ) {
            val appContext = context.applicationContext
            val dao = HarmonyDatabase.getInstance(appContext).memoryDao()
            val entry = dao.getEntry(entryId) ?: return
            if (entry.completedAt != null) return
            dao.setCompletedAt(entryId, completedAt = nowMillis, updatedAt = nowMillis)
            refreshAll(appContext)
        }

        private suspend fun renderOne(
            context: Context,
            appWidgetId: Int,
            scheduleImages: Boolean = true
        ) {
            val manager = AppWidgetManager.getInstance(context)
            val options = manager.getAppWidgetOptions(appWidgetId)
            val config = MemoryWidgetPreferences.load(context, appWidgetId)
            val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 110)
                .takeIf { it > 0 } ?: 110
            val slotCount = effectiveMemoryWidgetSlots(minHeight, config.maxItems)
            val dao = HarmonyDatabase.getInstance(context).memoryDao()
            val openEntries = dao.getOpenEntriesForWidget()
            dao.getCategoriesForWidget()
            val selected = selectMemoryWidgetEntries(openEntries, config, slotCount)
            val missingImages = linkedSetOf<String>()

            val views = RemoteViews(context.packageName, R.layout.widget_memory)
            val headerPendingIntent = memoryHeaderPendingIntent(context, appWidgetId)
            views.setOnClickPendingIntent(R.id.memory_widget_root, headerPendingIntent)
            views.setOnClickPendingIntent(R.id.memory_widget_header, headerPendingIntent)
            views.setOnClickPendingIntent(R.id.memory_widget_title, headerPendingIntent)

            val empty = selected.isEmpty()
            views.setViewVisibility(R.id.memory_widget_empty, if (empty) View.VISIBLE else View.GONE)
            if (empty) {
                views.setOnClickPendingIntent(R.id.memory_widget_empty, headerPendingIntent)
            }

            for (index in 0..2) {
                val entry = selected.getOrNull(index)
                if (entry == null) {
                    views.setViewVisibility(slotContainers[index], View.GONE)
                } else {
                    views.setViewVisibility(slotContainers[index], View.VISIBLE)
                    bindSlot(context, views, appWidgetId, index, entry, missingImages)
                }
            }

            manager.updateAppWidget(appWidgetId, views)

            if (scheduleImages && missingImages.isNotEmpty()) {
                ioScope.launch {
                    var loadedAny = false
                    for (url in missingImages) {
                        if (imageCache.peek(context, url) == null && imageCache.load(context, url) != null) {
                            loadedAny = true
                        }
                    }
                    if (loadedAny) renderOne(context, appWidgetId, scheduleImages = false)
                }
            }
        }

        private fun bindSlot(
            context: Context,
            views: RemoteViews,
            appWidgetId: Int,
            index: Int,
            entry: MemoryEntryEntity,
            missingImages: MutableSet<String>
        ) {
            val slot = index + 1
            val appPendingIntent = memoryEntryPendingIntent(context, appWidgetId, slot, entry.id)
            val completePendingIntent = memoryCompletePendingIntent(context, appWidgetId, slot, entry.id)
            views.setOnClickPendingIntent(slotContainers[index], appPendingIntent)
            views.setOnClickPendingIntent(slotTitles[index], appPendingIntent)
            views.setOnClickPendingIntent(slotBodies[index], appPendingIntent)
            views.setOnClickPendingIntent(slotChecks[index], completePendingIntent)

            when (entry.kind) {
                MemoryEntryKind.NOTE -> bindNote(views, index, entry)
                MemoryEntryKind.LINK -> bindLink(
                    context,
                    views,
                    appWidgetId,
                    slot,
                    index,
                    entry,
                    appPendingIntent,
                    missingImages
                )
            }
        }

        private fun bindNote(views: RemoteViews, index: Int, entry: MemoryEntryEntity) {
            views.setViewVisibility(slotImages[index], View.GONE)
            views.setViewVisibility(slotSites[index], View.GONE)
            views.setTextViewText(slotTitles[index], entry.title)
            setOptionalText(views, slotBodies[index], entry.body)
        }

        private fun bindLink(
            context: Context,
            views: RemoteViews,
            appWidgetId: Int,
            slot: Int,
            index: Int,
            entry: MemoryEntryEntity,
            appPendingIntent: android.app.PendingIntent,
            missingImages: MutableSet<String>
        ) {
            views.setViewVisibility(slotImages[index], View.VISIBLE)
            views.setViewVisibility(slotSites[index], View.VISIBLE)

            val url = entry.url
            val site = entry.previewSiteName
                ?.takeIf { it.isNotBlank() }
                ?: url?.let { runCatching { Uri.parse(it).host }.getOrNull() }
                ?: "Link"
            val title = entry.previewTitle?.takeIf { it.isNotBlank() } ?: entry.title
            val body = entry.previewDescription?.takeIf { it.isNotBlank() }
                ?: entry.body?.takeIf { it.isNotBlank() }

            views.setTextViewText(slotSites[index], site)
            views.setTextViewText(slotTitles[index], title)
            setOptionalText(views, slotBodies[index], body)

            val previewUrl = entry.previewImageUrl?.takeIf { it.isNotBlank() }
            val cached = previewUrl?.let { imageCache.peek(context, it) }
            if (cached != null) {
                views.setImageViewBitmap(slotImages[index], cached)
            } else {
                views.setImageViewResource(slotImages[index], R.drawable.widget_memory_link_fallback)
                if (previewUrl != null) missingImages += previewUrl
            }

            val browserPendingIntent = memoryBrowserPendingIntent(context, appWidgetId, slot, url)
            views.setOnClickPendingIntent(slotImages[index], browserPendingIntent ?: appPendingIntent)
        }

        private fun setOptionalText(views: RemoteViews, viewId: Int, text: String?) {
            val value = text?.trim().orEmpty()
            views.setTextViewText(viewId, value)
            views.setViewVisibility(viewId, if (value.isEmpty()) View.GONE else View.VISIBLE)
        }
    }
}
