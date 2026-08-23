package com.example.widget

import com.example.data.model.MemoryDefaults
import com.example.data.model.MemoryEntryEntity
import com.example.data.model.MemoryEntryKind
import org.junit.Assert.assertEquals
import org.junit.Test

class MemoryWidgetLogicTest {
    @Test
    fun `height maps to one two three slots and configured max wins`() {
        assertEquals(1, effectiveMemoryWidgetSlots(179, 3))
        assertEquals(2, effectiveMemoryWidgetSlots(180, 3))
        assertEquals(2, effectiveMemoryWidgetSlots(279, 3))
        assertEquals(3, effectiveMemoryWidgetSlots(280, 3))
        assertEquals(1, effectiveMemoryWidgetSlots(500, 1))
    }

    @Test
    fun `automatic mode keeps newest open order`() {
        val entries = listOf(entry("new", 300), entry("mid", 200), entry("old", 100))
        val result = selectMemoryWidgetEntries(
            entries,
            MemoryWidgetConfig(MemoryWidgetMode.AUTOMATIC, 3, emptyList()),
            2
        )
        assertEquals(listOf("new", "mid"), result.map { it.id })
    }

    @Test
    fun `pinned order is preserved and missing ids are backfilled`() {
        val entries = listOf(entry("new", 300), entry("pin-b", 200), entry("pin-a", 100))
        val result = selectMemoryWidgetEntries(
            entries,
            MemoryWidgetConfig(MemoryWidgetMode.PINNED, 3, listOf("pin-a", "missing", "pin-b")),
            3
        )
        assertEquals(listOf("pin-a", "pin-b", "new"), result.map { it.id })
    }

    private fun entry(id: String, updated: Long) = MemoryEntryEntity(
        id = id,
        categoryId = MemoryDefaults.OTHER_ID,
        kind = MemoryEntryKind.NOTE,
        title = id,
        createdAt = updated,
        updatedAt = updated
    )
}
