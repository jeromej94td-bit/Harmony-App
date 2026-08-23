package com.example.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.MainActivity

const val EXTRA_OPEN_MEMORY = "com.example.widget.extra.OPEN_MEMORY"
const val EXTRA_MEMORY_ENTRY_ID = "com.example.widget.extra.MEMORY_ENTRY_ID"
const val EXTRA_APP_WIDGET_ID = "com.example.widget.extra.APP_WIDGET_ID"
const val ACTION_COMPLETE_MEMORY_ENTRY = "com.example.widget.action.COMPLETE_MEMORY_ENTRY"

private const val ACTION_CODE_OPEN_ENTRY = 1
private const val ACTION_CODE_OPEN_LINK = 2
private const val ACTION_CODE_COMPLETE = 3
private const val ACTION_CODE_OPEN_HEADER = 4

private const val MEMORY_WIDGET_PROVIDER_CLASS = "com.example.widget.MemoryWidgetProvider"

data class MemoryWidgetOpenRequest(val entryId: String?)

fun memoryWidgetRequestCode(widgetId: Int, slot: Int, action: Int): Int =
    (widgetId * 100 + slot * 10 + action) and 0x7fffffff

fun memoryHeaderActivityIntent(context: Context, widgetId: Int): Intent =
    Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        data = Uri.parse("harmony://memory-widget/$widgetId/header")
        putExtra(EXTRA_OPEN_MEMORY, true)
        putExtra(EXTRA_APP_WIDGET_ID, widgetId)
    }

fun memoryEntryActivityIntent(
    context: Context,
    widgetId: Int,
    slot: Int,
    entryId: String
): Intent = Intent(context, MainActivity::class.java).apply {
    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
    data = Uri.parse("harmony://memory-widget/$widgetId/slot/$slot/entry/${Uri.encode(entryId)}")
    putExtra(EXTRA_OPEN_MEMORY, true)
    putExtra(EXTRA_MEMORY_ENTRY_ID, entryId)
    putExtra(EXTRA_APP_WIDGET_ID, widgetId)
}

fun memoryBrowserIntent(url: String?): Intent? {
    val raw = url?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    val uri = runCatching { Uri.parse(raw) }.getOrNull() ?: return null
    val scheme = uri.scheme?.lowercase()
    if (scheme != "http" && scheme != "https") return null
    if (uri.host.isNullOrBlank()) return null
    return Intent(Intent.ACTION_VIEW, uri)
}

fun memoryCompleteBroadcastIntent(
    context: Context,
    widgetId: Int,
    slot: Int,
    entryId: String
): Intent = Intent(ACTION_COMPLETE_MEMORY_ENTRY).apply {
    setClassName(context, MEMORY_WIDGET_PROVIDER_CLASS)
    data = Uri.parse("harmony://memory-widget/$widgetId/slot/$slot/complete/${Uri.encode(entryId)}")
    putExtra(EXTRA_MEMORY_ENTRY_ID, entryId)
    putExtra(EXTRA_APP_WIDGET_ID, widgetId)
}

fun memoryHeaderPendingIntent(context: Context, widgetId: Int): PendingIntent =
    PendingIntent.getActivity(
        context,
        memoryWidgetRequestCode(widgetId, 0, ACTION_CODE_OPEN_HEADER),
        memoryHeaderActivityIntent(context, widgetId),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

fun memoryEntryPendingIntent(
    context: Context,
    widgetId: Int,
    slot: Int,
    entryId: String
): PendingIntent = PendingIntent.getActivity(
    context,
    memoryWidgetRequestCode(widgetId, slot, ACTION_CODE_OPEN_ENTRY),
    memoryEntryActivityIntent(context, widgetId, slot, entryId),
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
)

fun memoryBrowserPendingIntent(
    context: Context,
    widgetId: Int,
    slot: Int,
    url: String?
): PendingIntent? {
    val intent = memoryBrowserIntent(url) ?: return null
    intent.data = intent.data?.buildUpon()
        ?.appendQueryParameter("harmony_widget", "$widgetId-$slot")
        ?.build()
    return PendingIntent.getActivity(
        context,
        memoryWidgetRequestCode(widgetId, slot, ACTION_CODE_OPEN_LINK),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}

fun memoryCompletePendingIntent(
    context: Context,
    widgetId: Int,
    slot: Int,
    entryId: String
): PendingIntent = PendingIntent.getBroadcast(
    context,
    memoryWidgetRequestCode(widgetId, slot, ACTION_CODE_COMPLETE),
    memoryCompleteBroadcastIntent(context, widgetId, slot, entryId),
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
)

fun parseMemoryWidgetOpenRequest(intent: Intent?): MemoryWidgetOpenRequest? {
    if (intent?.getBooleanExtra(EXTRA_OPEN_MEMORY, false) != true) return null
    return MemoryWidgetOpenRequest(intent.getStringExtra(EXTRA_MEMORY_ENTRY_ID))
}
