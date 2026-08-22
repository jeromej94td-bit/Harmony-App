package com.example.data

import com.example.data.model.MemoryEntryEntity

enum class MemoryBucket { CURRENT_OPEN, ARCHIVED }

object MemoryArchivePolicy {
    fun bucketAt(entry: MemoryEntryEntity, nowMillis: Long): MemoryBucket = when {
        entry.completedAt == null -> MemoryBucket.CURRENT_OPEN
        else -> MemoryBucket.ARCHIVED
    }
}
