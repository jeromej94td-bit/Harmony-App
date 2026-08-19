package com.example.data

import com.example.data.model.MemoryDefaults
import com.example.data.model.MemoryEntryEntity
import com.example.data.model.MemoryEntryKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MemoryArchivePolicyTest {
    private val completedAt = 1_000_000L
    private val day = 24L * 60L * 60L * 1_000L
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
    fun `completed entry stays current immediately before 24 hours`() {
        assertEquals(
            MemoryBucket.CURRENT_GRACE,
            MemoryArchivePolicy.bucketAt(entry, completedAt + day - 1L)
        )
    }

    @Test
    fun `completed entry is archived exactly at 24 hours`() {
        assertEquals(
            MemoryBucket.ARCHIVED,
            MemoryArchivePolicy.bucketAt(entry, completedAt + day)
        )
    }

    @Test
    fun `open entry never has an expiry`() {
        assertNull(
            MemoryArchivePolicy.nextExpiryAt(
                listOf(entry.copy(completedAt = null)),
                9_000_000L
            )
        )
    }
}
