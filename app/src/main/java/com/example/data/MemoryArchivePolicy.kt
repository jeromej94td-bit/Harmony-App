package com.example.data

import com.example.data.model.MemoryEntryEntity

enum class MemoryBucket { CURRENT_OPEN, CURRENT_GRACE, ARCHIVED }

object MemoryArchivePolicy {
    const val GRACE_PERIOD_MS = 24L * 60L * 60L * 1_000L

    fun bucketAt(entry: MemoryEntryEntity, nowMillis: Long): MemoryBucket = when {
        entry.completedAt == null -> MemoryBucket.CURRENT_OPEN
        nowMillis < entry.completedAt + GRACE_PERIOD_MS -> MemoryBucket.CURRENT_GRACE
        else -> MemoryBucket.ARCHIVED
    }

    fun nextExpiryAt(entries: List<MemoryEntryEntity>, nowMillis: Long): Long? =
        entries.mapNotNull { it.completedAt?.plus(GRACE_PERIOD_MS) }
            .filter { it > nowMillis }
            .minOrNull()
}
