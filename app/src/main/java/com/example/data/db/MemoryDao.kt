package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.MemoryCategoryEntity
import com.example.data.model.MemoryEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memory_categories WHERE isVisible = 1 ORDER BY sortOrder, createdAt")
    fun observeCategories(): Flow<List<MemoryCategoryEntity>>

    @Query("SELECT * FROM memory_categories WHERE id = :id LIMIT 1")
    suspend fun getCategory(id: String): MemoryCategoryEntity?

    @Query("SELECT * FROM memory_entries ORDER BY updatedAt DESC")
    fun observeEntries(): Flow<List<MemoryEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCategory(category: MemoryCategoryEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategories(categories: List<MemoryCategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertEntries(entries: List<MemoryEntryEntity>)

    @Update
    suspend fun updateCategory(category: MemoryCategoryEntity)

    @Update
    suspend fun updateEntry(entry: MemoryEntryEntity)

    @Query("UPDATE memory_entries SET completedAt = :completedAt, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setCompletedAt(id: String, completedAt: Long?, updatedAt: Long)

    @Query("UPDATE memory_entries SET categoryId = :toId, updatedAt = :updatedAt WHERE categoryId = :fromId")
    suspend fun moveEntries(fromId: String, toId: String, updatedAt: Long)

    @Query(
        "UPDATE memory_categories SET systemKey = :systemKey, sortOrder = :sortOrder, updatedAt = :updatedAt " +
            "WHERE id = :id AND (systemKey != :systemKey OR sortOrder != :sortOrder)"
    )
    suspend fun updateSystemCategory(id: String, systemKey: String, sortOrder: Int, updatedAt: Long)

    @Query("UPDATE memory_categories SET isVisible = 0, updatedAt = :updatedAt WHERE id = :id")
    suspend fun hideCategory(id: String, updatedAt: Long)

    @Query("SELECT * FROM memory_entries WHERE id = :id LIMIT 1")
    suspend fun getEntry(id: String): MemoryEntryEntity?

    @Query("DELETE FROM memory_entries WHERE id = :id")
    suspend fun deleteEntry(id: String)

    @Query("DELETE FROM memory_entries WHERE id IN (:ids)")
    suspend fun deleteEntries(ids: Set<String>)

    @Query("DELETE FROM memory_categories WHERE id = :id")
    suspend fun deleteCategory(id: String)
}
