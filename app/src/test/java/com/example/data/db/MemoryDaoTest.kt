package com.example.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.MemoryCategoryEntity
import com.example.data.model.MemoryDefaults
import com.example.data.model.MemoryEntryEntity
import com.example.data.model.MemoryEntryKind
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MemoryDaoTest {
    private lateinit var database: HarmonyDatabase
    private lateinit var dao: MemoryDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(),
            HarmonyDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.memoryDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `memory DAO orders rows and updates only the requested entries`() = runBlocking {
        val defaultCategory = MemoryCategoryEntity(
            id = MemoryDefaults.FILMS_ID,
            systemKey = "films",
            colorKey = "violet",
            iconKey = "movie",
            sortOrder = 1,
            createdAt = 100L,
            updatedAt = 100L
        )
        val destinationCategory = MemoryCategoryEntity(
            id = "custom-ideas",
            customName = "Ideas",
            colorKey = "blue",
            iconKey = "bulb",
            sortOrder = 0,
            createdAt = 101L,
            updatedAt = 101L
        )
        val firstEntry = MemoryEntryEntity(
            id = "entry-1",
            categoryId = defaultCategory.id,
            kind = MemoryEntryKind.NOTE,
            title = "Arrival",
            createdAt = 110L,
            updatedAt = 110L
        )
        val secondEntry = MemoryEntryEntity(
            id = "entry-2",
            categoryId = defaultCategory.id,
            kind = MemoryEntryKind.LINK,
            title = "A useful link",
            url = "https://example.com",
            createdAt = 120L,
            updatedAt = 120L
        )

        dao.insertCategory(defaultCategory)
        dao.insertCategory(destinationCategory)
        dao.insertEntries(listOf(firstEntry, secondEntry))

        assertEquals(
            listOf(destinationCategory.id, defaultCategory.id),
            dao.observeCategories().first().map { it.id }
        )
        assertEquals(
            listOf(secondEntry.id, firstEntry.id),
            dao.observeEntries().first().map { it.id }
        )

        dao.setCompletedAt(firstEntry.id, completedAt = 500L, updatedAt = 500L)
        assertEquals(
            firstEntry.copy(completedAt = 500L, updatedAt = 500L),
            dao.getEntry(firstEntry.id)
        )
        assertEquals(secondEntry, dao.getEntry(secondEntry.id))

        dao.moveEntries(defaultCategory.id, destinationCategory.id, updatedAt = 600L)
        val movedEntries = dao.observeEntries().first()
        assertEquals(2, movedEntries.size)
        assertEquals(setOf(firstEntry.id, secondEntry.id), movedEntries.map { it.id }.toSet())
        assertEquals(setOf(destinationCategory.id), movedEntries.map { it.categoryId }.toSet())
        val movedById = movedEntries.associateBy { it.id }
        assertEquals(
            firstEntry.copy(
                categoryId = destinationCategory.id,
                updatedAt = 600L,
                completedAt = 500L
            ),
            movedById[firstEntry.id]
        )
        assertEquals(
            secondEntry.copy(categoryId = destinationCategory.id, updatedAt = 600L),
            movedById[secondEntry.id]
        )
    }
}
