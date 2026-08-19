package com.example.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.HarmonyDatabase
import com.example.data.model.MemoryCategoryEntity
import com.example.data.model.MemoryDefaults
import com.example.data.model.MemoryEntryEntity
import com.example.data.model.MemoryEntryKind
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoryRepositoryTest {
    private lateinit var database: HarmonyDatabase
    private lateinit var repository: MemoryRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            HarmonyDatabase::class.java
        ).allowMainThreadQueries().build()
        repository = RoomMemoryRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `ensure defaults repairs a partial set and remains idempotent in stable order`() = runTest {
        database.memoryDao().insertCategory(
            MemoryCategoryEntity(
                id = MemoryDefaults.FILMS_ID,
                systemKey = "Filme",
                colorKey = "violet",
                iconKey = "movie",
                sortOrder = 0,
                createdAt = 50L,
                updatedAt = 50L
            )
        )

        repository.ensureDefaultCategories(nowMillis = 100L)
        val firstSeed = repository.categories.first()
        repository.ensureDefaultCategories(nowMillis = 200L)
        val secondSeed = repository.categories.first()

        assertEquals(MemoryDefaults.orderedIds, firstSeed.map { it.id })
        assertEquals(firstSeed, secondSeed)
        assertEquals(listOf("Filme", "Serien", "Ideen", "Orte", "Sonstiges"), firstSeed.map { it.systemKey })
        assertEquals(listOf(0, 1, 2, 3, 4), firstSeed.map { it.sortOrder })
        assertEquals(50L, firstSeed.first().createdAt)
        assertEquals(50L, firstSeed.first().updatedAt)
        assertEquals(List(4) { 100L }, firstSeed.drop(1).map { it.createdAt })
        assertEquals(List(4) { 100L }, firstSeed.drop(1).map { it.updatedAt })
        firstSeed.forEach { category ->
            assertNotNull(category.colorKey)
            assertNotNull(category.iconKey)
        }
    }

    @Test
    fun `custom category can be created and updated while defaults are protected`() = runTest {
        repository.ensureDefaultCategories(nowMillis = 100L)

        val customId = repository.createCategory("Date-Ideen", "pink", "heart", nowMillis = 110L)
        repository.updateCategory(customId, "Unsere Date-Ideen", "purple", "sparkles", nowMillis = 120L)
        val custom = repository.categories.first().single { it.id == customId }

        assertEquals("Unsere Date-Ideen", custom.customName)
        assertNull(custom.systemKey)
        assertEquals("purple", custom.colorKey)
        assertEquals("sparkles", custom.iconKey)
        assertEquals(110L, custom.createdAt)
        assertEquals(120L, custom.updatedAt)

        assertIllegalArgument {
            repository.updateCategory(
                MemoryDefaults.FILMS_ID,
                "Umbenannt",
                "red",
                "close",
                nowMillis = 130L
            )
        }
        assertEquals("Filme", repository.categories.first().first().systemKey)
    }

    @Test
    fun `delete custom category moves entries before deleting category`() = runTest {
        repository.ensureDefaultCategories(nowMillis = 100L)
        val customId = repository.createCategory("Später", "blue", "star", nowMillis = 110L)
        repository.insertEntries(listOf(entry(id = "entry-1", categoryId = customId, updatedAt = 120L)))

        repository.deleteCustomCategory(customId, MemoryDefaults.OTHER_ID, nowMillis = 300L)

        assertEquals(MemoryDefaults.OTHER_ID, repository.entries.first().single().categoryId)
        assertEquals(300L, repository.entries.first().single().updatedAt)
        assertNull(repository.categories.first().find { it.id == customId })
    }

    @Test
    fun `delete custom category rejects default categories`() = runTest {
        repository.ensureDefaultCategories(nowMillis = 100L)

        assertIllegalArgument {
            repository.deleteCustomCategory(
                MemoryDefaults.FILMS_ID,
                MemoryDefaults.OTHER_ID,
                nowMillis = 200L
            )
        }

        assertEquals(MemoryDefaults.orderedIds, repository.categories.first().map { it.id })
    }

    @Test
    fun `entry mutations complete restore update and delete explicit entry`() = runTest {
        repository.ensureDefaultCategories(nowMillis = 100L)
        val original = entry(id = "entry-1", categoryId = MemoryDefaults.IDEAS_ID, updatedAt = 110L)
        repository.insertEntries(listOf(original))

        repository.setCompleted(original.id, completedAt = 200L, updatedAt = 200L)
        assertEquals(original.copy(completedAt = 200L, updatedAt = 200L), repository.getEntry(original.id))

        repository.setCompleted(original.id, completedAt = null, updatedAt = 210L)
        assertEquals(original.copy(updatedAt = 210L), repository.getEntry(original.id))

        val edited = original.copy(title = "Überarbeitete Idee", body = "Details", updatedAt = 220L)
        repository.updateEntry(edited)
        assertEquals(edited, repository.getEntry(original.id))

        repository.deleteEntry(original.id)
        assertNull(repository.getEntry(original.id))
        assertEquals(emptyList<MemoryEntryEntity>(), repository.entries.first())
    }

    private fun entry(id: String, categoryId: String, updatedAt: Long) = MemoryEntryEntity(
        id = id,
        categoryId = categoryId,
        kind = MemoryEntryKind.NOTE,
        title = "Idee",
        createdAt = 100L,
        updatedAt = updatedAt
    )

    private suspend fun assertIllegalArgument(block: suspend () -> Unit) {
        try {
            block()
            fail("Expected IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
            // Expected: system categories are immutable at the repository boundary.
        }
    }
}
