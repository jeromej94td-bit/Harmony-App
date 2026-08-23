package com.example.data

import com.example.data.model.MemoryDefaults
import com.example.data.model.MemoryEntryEntity
import com.example.data.model.MemoryEntryKind
import org.junit.Assert.assertEquals
import org.junit.Test

class MemoryArchivePolicyTest {
    private val completedAt = 1_000_000L
    private val entry = MemoryEntryEntity(
        id = "entry-1",
        categoryId = MemoryDefaults.FILMS_ID,
        kind = MemoryEntryKind.NOTE,
        title = "Arrival",
        createdAt = 10L,
        updatedAt = completedAt,
        completedAt = completedAt
    )

    @Test
    fun `completed entry is archived immediately`() {
        assertEquals(
            MemoryBucket.ARCHIVED,
            MemoryArchivePolicy.bucketAt(entry, completedAt)
        )
    }

    @Test
    fun `open entry remains current`() {
        assertEquals(
            MemoryBucket.CURRENT_OPEN,
            MemoryArchivePolicy.bucketAt(entry.copy(completedAt = null), completedAt)
        )
    }
}
