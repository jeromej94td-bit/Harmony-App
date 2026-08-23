package com.example.widget

import android.content.Context

object MemoryWidgetPreferences {
    private const val PREFS_NAME = "memory_widget_preferences"

    fun load(context: Context, appWidgetId: Int): MemoryWidgetConfig {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val prefix = keyPrefix(appWidgetId)
        val mode = runCatching {
            MemoryWidgetMode.valueOf(
                prefs.getString("${prefix}mode", MemoryWidgetMode.AUTOMATIC.name)
                    ?: MemoryWidgetMode.AUTOMATIC.name
            )
        }.getOrDefault(MemoryWidgetMode.AUTOMATIC)
        val maxItems = prefs.getInt("${prefix}max_items", 3)
        val pinnedIds = prefs.getString("${prefix}pinned_ids", "")
            .orEmpty()
            .lineSequence()
            .filter(String::isNotEmpty)
            .toList()

        return MemoryWidgetConfig(mode, maxItems, pinnedIds).normalized()
    }

    fun save(context: Context, appWidgetId: Int, config: MemoryWidgetConfig) {
        val normalized = config.normalized()
        val prefix = keyPrefix(appWidgetId)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString("${prefix}mode", normalized.mode.name)
            .putInt("${prefix}max_items", normalized.maxItems)
            .putString("${prefix}pinned_ids", normalized.pinnedIds.joinToString("\n"))
            .apply()
    }

    fun delete(context: Context, appWidgetId: Int) {
        val prefix = keyPrefix(appWidgetId)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove("${prefix}mode")
            .remove("${prefix}max_items")
            .remove("${prefix}pinned_ids")
            .apply()
    }

    private fun keyPrefix(appWidgetId: Int): String = "widget_${appWidgetId}_"
}
