package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.db.HarmonyDatabase
import com.example.data.model.MemoryCategoryEntity
import com.example.data.model.MemoryDefaults
import com.example.data.model.MemoryEntryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID

interface MemoryRepository {
    val categories: Flow<List<MemoryCategoryEntity>>
    val entries: Flow<List<MemoryEntryEntity>>
    suspend fun ensureDefaultCategories(nowMillis: Long)
    suspend fun createCategory(name: String, colorKey: String, iconKey: String, nowMillis: Long): String
    suspend fun updateCategory(id: String, name: String, colorKey: String, iconKey: String, nowMillis: Long)
    suspend fun deleteCustomCategory(id: String, moveToId: String, nowMillis: Long)
    suspend fun insertEntries(entries: List<MemoryEntryEntity>)
    suspend fun getEntry(id: String): MemoryEntryEntity?
    suspend fun updateEntry(entry: MemoryEntryEntity)
    suspend fun setCompleted(id: String, completedAt: Long?, updatedAt: Long)
    suspend fun deleteEntry(id: String)
    suspend fun deleteEntries(ids: Set<String>)
}

class RoomMemoryRepository(
    private val database: HarmonyDatabase
) : MemoryRepository {
    private val dao = database.memoryDao()

    override val categories: Flow<List<MemoryCategoryEntity>> = dao.observeCategories()
    override val entries: Flow<List<MemoryEntryEntity>> = dao.observeEntries()

    override suspend fun ensureDefaultCategories(nowMillis: Long) {
        database.withTransaction {
            dao.insertCategories(defaultCategories(nowMillis))
            dao.updateSystemCategory(
                id = MemoryDefaults.FILMS_ID,
                systemKey = "Filme & Serien",
                sortOrder = 0,
                updatedAt = nowMillis
            )
            dao.getCategory(MemoryDefaults.SERIES_ID)?.let {
                dao.moveEntries(MemoryDefaults.SERIES_ID, MemoryDefaults.FILMS_ID, nowMillis)
                dao.hideCategory(MemoryDefaults.SERIES_ID, nowMillis)
            }
        }
    }

    override suspend fun createCategory(
        name: String,
        colorKey: String,
        iconKey: String,
        nowMillis: Long
    ): String {
        val id = UUID.randomUUID().toString()
        dao.insertCategory(
            MemoryCategoryEntity(
                id = id,
                customName = name,
                colorKey = colorKey,
                iconKey = iconKey,
                sortOrder = Int.MAX_VALUE,
                createdAt = nowMillis,
                updatedAt = nowMillis
            )
        )
        return id
    }

    override suspend fun updateCategory(
        id: String,
        name: String,
        colorKey: String,
        iconKey: String,
        nowMillis: Long
    ) {
        requireCustomCategory(id)
        val existing = categories.first().firstOrNull { it.id == id } ?: return
        dao.updateCategory(
            existing.copy(
                customName = name,
                colorKey = colorKey,
                iconKey = iconKey,
                updatedAt = nowMillis
            )
        )
    }

    override suspend fun deleteCustomCategory(id: String, moveToId: String, nowMillis: Long) {
        require(id != moveToId) { "A category cannot be moved to itself." }
        database.withTransaction {
            val existing = requireNotNull(dao.getCategory(id)) { "Category does not exist." }
            requireNotNull(dao.getCategory(moveToId)?.takeIf { it.isVisible }) {
                "Move target does not exist."
            }
            dao.moveEntries(id, moveToId, nowMillis)
            if (existing.systemKey != null) {
                dao.hideCategory(id, nowMillis)
            } else {
                dao.deleteCategory(id)
            }
        }
    }

    override suspend fun insertEntries(entries: List<MemoryEntryEntity>) {
        dao.insertEntries(entries)
    }

    override suspend fun getEntry(id: String): MemoryEntryEntity? = dao.getEntry(id)

    override suspend fun updateEntry(entry: MemoryEntryEntity) {
        dao.updateEntry(entry)
    }

    override suspend fun setCompleted(id: String, completedAt: Long?, updatedAt: Long) {
        dao.setCompletedAt(id, completedAt, updatedAt)
    }

    override suspend fun deleteEntry(id: String) {
        dao.deleteEntry(id)
    }

    override suspend fun deleteEntries(ids: Set<String>) {
        if (ids.isNotEmpty()) dao.deleteEntries(ids)
    }

    private fun requireCustomCategory(id: String) {
        require(id !in MemoryDefaults.orderedIds) { "System categories cannot be changed." }
    }

    private fun defaultCategories(nowMillis: Long) = listOf(
        MemoryCategoryEntity(
            id = MemoryDefaults.FILMS_ID,
            systemKey = "Filme & Serien",
            colorKey = "violet",
            iconKey = "movie",
            sortOrder = 0,
            createdAt = nowMillis,
            updatedAt = nowMillis
        ),
        MemoryCategoryEntity(
            id = MemoryDefaults.IDEAS_ID,
            systemKey = "Ideen",
            colorKey = "orange",
            iconKey = "lightbulb",
            sortOrder = 1,
            createdAt = nowMillis,
            updatedAt = nowMillis
        ),
        MemoryCategoryEntity(
            id = MemoryDefaults.PLACES_ID,
            systemKey = "Orte",
            colorKey = "blue",
            iconKey = "place",
            sortOrder = 2,
            createdAt = nowMillis,
            updatedAt = nowMillis
        ),
        MemoryCategoryEntity(
            id = MemoryDefaults.OTHER_ID,
            systemKey = "Sonstiges",
            colorKey = "teal",
            iconKey = "bookmark",
            sortOrder = 3,
            createdAt = nowMillis,
            updatedAt = nowMillis
        )
    )
}
