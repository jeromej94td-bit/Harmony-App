package com.example.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Persistiert ausschließlich Metadaten für reproduzierbare Dev-Studio-Exporte.
 * Die eigentlichen Spiele/Bilder bleiben weiterhin im DeveloperDataManager.
 */
object DevExportStateStore {
    private const val PREFS = "dev_studio_export_v2"
    private const val KEY_PACK_ORDER = "pack_order"
    private const val KEY_ORIGINAL_NAMES = "original_file_names"

    private val packOrder = mutableListOf<String>()
    private val originalFileNames = linkedMapOf<String, String>()
    private var loaded = false

    fun init(context: Context) {
        if (loaded) return
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        packOrder.clear()
        originalFileNames.clear()

        try {
            val order = JSONArray(prefs.getString(KEY_PACK_ORDER, "[]") ?: "[]")
            for (i in 0 until order.length()) {
                val id = order.optString(i).trim()
                if (id.isNotEmpty() && id !in packOrder) packOrder += id
            }
        } catch (_: Exception) {
            packOrder.clear()
        }

        try {
            val names = JSONObject(prefs.getString(KEY_ORIGINAL_NAMES, "{}") ?: "{}")
            names.keys().forEach { key ->
                val name = names.optString(key).trim()
                if (name.isNotEmpty()) originalFileNames[key] = DevExportLogic.safeBaseName(name)
            }
        } catch (_: Exception) {
            originalFileNames.clear()
        }
        loaded = true
    }

    fun orderedIds(availableIds: List<String>): List<String> =
        DevExportLogic.reconcileOrder(packOrder, availableIds)

    fun packOrder(): List<String> = packOrder.toList()

    fun reconcileAndPersist(context: Context, availableIds: List<String>): List<String> {
        init(context)
        val reconciled = orderedIds(availableIds)
        if (reconciled != packOrder) {
            packOrder.clear()
            packOrder.addAll(reconciled)
            persist(context)
        }
        return reconciled
    }

    fun registerNewPack(context: Context, packId: String, availableIds: List<String>) {
        init(context)
        val reconciled = DevExportLogic.reconcileOrder(packOrder, availableIds)
        packOrder.clear()
        packOrder.addAll(reconciled.filterNot { it == packId })
        packOrder.add(0, packId)
        persist(context)
    }

    fun ensurePack(context: Context, packId: String, availableIds: List<String>) {
        init(context)
        val reconciled = DevExportLogic.reconcileOrder(packOrder, availableIds)
        packOrder.clear()
        packOrder.addAll(reconciled)
        if (packId !in packOrder) packOrder.add(packId)
        persist(context)
    }

    fun movePack(context: Context, packId: String, delta: Int, availableIds: List<String>): List<String> {
        init(context)
        val current = DevExportLogic.reconcileOrder(packOrder, availableIds)
        val moved = DevExportLogic.move(current, packId, delta)
        packOrder.clear()
        packOrder.addAll(moved)
        persist(context)
        return moved
    }

    fun removePack(context: Context, packId: String) {
        init(context)
        if (packOrder.removeAll { it == packId }) persist(context)
    }

    fun recordOriginalFileName(context: Context, optionKey: String, fileName: String) {
        init(context)
        val key = optionKey.trim()
        if (key.isEmpty()) return
        val clean = DevExportLogic.safeBaseName(fileName)
        if (originalFileNames[key] == clean) return
        originalFileNames[key] = clean
        persist(context)
    }

    fun removeOriginalFileName(context: Context, optionKey: String) {
        init(context)
        if (originalFileNames.remove(optionKey.trim()) != null) persist(context)
    }

    fun originalFileNames(): Map<String, String> = originalFileNames.toMap()

    fun originalFileNameFor(optionKey: String): String? = originalFileNames[optionKey.trim()]

    fun restore(
        context: Context,
        restoredOrder: List<String>,
        restoredOriginalNames: Map<String, String>,
        availableIds: List<String>
    ) {
        init(context)
        packOrder.clear()
        packOrder.addAll(DevExportLogic.reconcileOrder(restoredOrder, availableIds))
        originalFileNames.clear()
        restoredOriginalNames.forEach { (key, value) ->
            if (key.isNotBlank() && value.isNotBlank()) {
                originalFileNames[key.trim()] = DevExportLogic.safeBaseName(value)
            }
        }
        persist(context)
    }

    fun clear(context: Context) {
        init(context)
        packOrder.clear()
        originalFileNames.clear()
        persist(context)
    }

    private fun persist(context: Context) {
        val order = JSONArray()
        packOrder.forEach { order.put(it) }

        val names = JSONObject()
        originalFileNames.forEach { (key, value) -> names.put(key, value) }

        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PACK_ORDER, order.toString())
            .putString(KEY_ORIGINAL_NAMES, names.toString())
            .apply()
    }
}
