package com.example.widget

import com.example.data.model.MemoryEntryEntity

enum class MemoryWidgetMode { AUTOMATIC, PINNED }

data class MemoryWidgetConfig(
    val mode: MemoryWidgetMode = MemoryWidgetMode.AUTOMATIC,
    val maxItems: Int = 3,
    val pinnedIds: List<String> = emptyList()
) {
    fun normalized(): MemoryWidgetConfig = copy(
        maxItems = maxItems.coerceIn(1, 3),
        pinnedIds = pinnedIds
            .asSequence()
            .filterNot { it.contains('\n') }
            .distinct()
            .take(3)
            .toList()
    )
}

fun effectiveMemoryWidgetSlots(minHeightDp: Int, configuredMax: Int): Int {
    val byHeight = when {
        minHeightDp < 180 -> 1
        minHeightDp < 280 -> 2
        else -> 3
    }
    return minOf(byHeight, configuredMax.coerceIn(1, 3))
}

fun selectMemoryWidgetEntries(
    openEntries: List<MemoryEntryEntity>,
    config: MemoryWidgetConfig,
    slotCount: Int
): List<MemoryEntryEntity> {
    val normalized = config.normalized()
    val limit = minOf(slotCount.coerceIn(1, 3), normalized.maxItems)

    if (normalized.mode == MemoryWidgetMode.AUTOMATIC) {
        return openEntries.take(limit)
    }

    val byId = openEntries.associateBy { it.id }
    val pinned = normalized.pinnedIds.mapNotNull(byId::get)
    val usedIds = pinned.mapTo(mutableSetOf()) { it.id }
    val backfill = openEntries.filter { usedIds.add(it.id) }

    return (pinned + backfill).take(limit)
}
