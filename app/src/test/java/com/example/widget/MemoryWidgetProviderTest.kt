package com.example.widget

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.HarmonyDatabase
import com.example.data.model.MemoryCategoryEntity
import com.example.data.model.MemoryEntryEntity
import com.example.data.model.MemoryEntryKind
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoryWidgetProviderTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        MemoryWidgetPreferences.delete(context, 11)
        MemoryWidgetPreferences.delete(context, 12)
    }

    @Test
    fun `completion marks only requested open entry completed`() = runBlocking {
        val dao = HarmonyDatabase.getInstance(context).memoryDao()
        val suffix = System.nanoTime().toString()
        val categoryId = "widget-category-$suffix"
        val firstId = "widget-first-$suffix"
        val secondId = "widget-second-$suffix"
        val category = MemoryCategoryEntity(
            id = categoryId,
            customName = "Widget",
            colorKey = "violet",
            iconKey = "bookmark",
            sortOrder = 99,
            createdAt = 1L,
            updatedAt = 1L
        )
        dao.insertCategory(category)
        dao.insertEntries(
            listOf(
                MemoryEntryEntity(
                    id = firstId,
                    categoryId = categoryId,
                    kind = MemoryEntryKind.NOTE,
                    title = "First",
                    createdAt = 2L,
                    updatedAt = 2L
                ),
                MemoryEntryEntity(
                    id = secondId,
                    categoryId = categoryId,
                    kind = MemoryEntryKind.NOTE,
                    title = "Second",
                    createdAt = 3L,
                    updatedAt = 3L
                )
            )
        )

        MemoryWidgetProvider.completeEntry(context, firstId, nowMillis = 42L)

        assertEquals(42L, dao.getEntry(firstId)?.completedAt)
        assertEquals(42L, dao.getEntry(firstId)?.updatedAt)
        assertEquals(null, dao.getEntry(secondId)?.completedAt)
    }

    @Test
    fun `deleting one widget clears only that widget preferences`() {
        MemoryWidgetPreferences.save(
            context,
            11,
            MemoryWidgetConfig(MemoryWidgetMode.PINNED, 1, listOf("first"))
        )
        MemoryWidgetPreferences.save(
            context,
            12,
            MemoryWidgetConfig(MemoryWidgetMode.PINNED, 2, listOf("second"))
        )

        MemoryWidgetProvider().onDeleted(context, intArrayOf(11))

        assertEquals(MemoryWidgetConfig(), MemoryWidgetPreferences.load(context, 11))
        assertNotNull(MemoryWidgetPreferences.load(context, 12))
        assertEquals(
            MemoryWidgetConfig(MemoryWidgetMode.PINNED, 2, listOf("second")),
            MemoryWidgetPreferences.load(context, 12)
        )
    }
}
